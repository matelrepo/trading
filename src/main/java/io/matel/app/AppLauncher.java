package io.matel.app;

import io.matel.app.config.Global;
import io.matel.app.controller.WsController;
import io.matel.app.domain.ContractBasic;
import io.matel.app.macro.MacroWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;


@Service
public class AppLauncher implements CommandLineRunner {

    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);
    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);
    private AppController appController;
    private WsController wsController;
    private ConcurrentHashMap<Long, LoadingErrorHandler> errors = new ConcurrentHashMap();
    private Semaphore semaphore = new Semaphore(Global.EXECUTOR_THREADS);

    @Autowired
    Global global;

    @Autowired
    MacroWriter macroWriter;

    public AppLauncher(AppController appController, WsController wsController) {
        this.appController = appController;
        this.wsController = wsController;
    }

    @Override
    public void run(String... args) {

        LOGGER.info("Starting with " + Global.EXECUTOR_THREADS + " cores");

//        new Thread(() -> {
//            try {
//                if (Global.UPDATE_MACRO)
//                    macroWriter.start();
//                else
//                    LOGGER.info(">>> MACRO_UPDATE is switched off");
////                dbb.init("matel", "5432", "matel");
////                dbb.getMacroItemsByCountry("United States");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();

        startLive();
    }


    public void startLive() {
        Database database = appController.createDatabase("matel", "5432", "matel");
        appController.setContractsLive(appController.contractRepository.findTop100ByActiveAndType(true, "LIVE"));
        LOGGER.info(appController.getContractsLive().size() + " contracts found");
//        try {
            Long idTick = database.findTopIdTickOrderByIdDesc();
            if(idTick == null) idTick =0L;
            Global.startIdTick = idTick;
            Long idCandle = database.findTopIdCandleOrderByIdDesc();
            if(idCandle == null) idCandle =0L;
            Global.startIdCandle = idCandle;

        global.setIdTick(idTick);
            global.setIdCandle(idCandle);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
        LOGGER.info("Setting up last id tick: " + global.getIdTick(false));
        LOGGER.info("Setting up last id candle: " + global.getIdCandle(false));
        database.close();

        if (Global.READ_ONLY_TICKS)
            LOGGER.warn(">>> Read only lock! <<<");

        for (ContractBasic contract : appController.getContractsLive()) {
            if (contract.getIdcontract() == 2) {
                createGenerator(contract);
                createProcessor(contract, 0);
            }
        }

        LOGGER.info("Loading historical candles...");
        appController.getGenerators().forEach((id, generator) -> {
            try {
                semaphore.acquire();
                new Thread(() -> {
                    generator.initDatabase();

                    LoadingErrorHandler error = new LoadingErrorHandler();
                    this.errors.put(generator.getContract().getIdcontract(), error);
                    error.idcontract = generator.getContract().getIdcontract();
                    error.numTicksBreaking = generator.getDatabase().countIdTickBreaks(error.idcontract);
                    error.lastCandleId = generator.getDatabase().findTopIdCandleByIdcontractOrderByIdDesc(error.idcontract);

                    if (error.numTicksBreaking > 1) {
                        error.errorDetected = true;
                        error.minTickIdBreaking = generator.getDatabase().getSmallestIdTickBreak(error.idcontract);
                    }

                    if (error.errorDetected)
                        LOGGER.warn("Error: " + error.toString());

                    if (Global.COMPUTE_DEEP_HISTORICAL) {
                        Database tickDatabase = appController.createDatabase("cleanm", Global.port, "atmuser");
                        long minIdTick = 0;
                        int count =0;

                        while(minIdTick >=0) {
                            System.out.println(count + " round(s) with tick " + minIdTick);
                            count++;
                            minIdTick = tickDatabase.getTicksByTable(error.idcontract, false, "trading.data18", minIdTick);
                        }
                        minIdTick =0;
                        count =0;
                        while(minIdTick >=0) {
                            System.out.println(count + " round(s) with tick " + minIdTick);
                            count++;
                            minIdTick =tickDatabase.getTicksByTable(error.idcontract, false, "trading.data19", minIdTick);
                        }
//                        tickDatabase.getTicksByTable(error.idcontract, true, "trading.data20");
                        tickDatabase.close();
                    }

                    generator.getDatabase().getSaverController().saveNow(generator, true);


                    appController.loadHistoricalCandlesFromDbb(generator.getContract().getIdcontract(), false);
                    generator.getDatabase().close();
                    semaphore.release();


                    LOGGER.info("Connecting market data for all contracts...");
                    try {
                        generator.setDatabase(appController.getDatabase());
                        appController.connectMarketData(generator.getContract());
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    LOGGER.info(">>> Finished contract " + generator.getContract().getIdcontract());

//                    appController.getGenerators().forEach((idcon, gen) -> {
                        generator.saveGeneratorState();
                        generator.getProcessors().forEach((freq, proc) -> {
                            proc.saveProcessorState();
                        });
//                    });

                }).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


    }

    private Generator createGenerator(ContractBasic contract) {
        return appController.createGenerator(contract);
    }

    private void createProcessor(ContractBasic contract, int minFreq) {
        appController.createProcessors(contract, minFreq);
    }

    //    public void startDailyCon(){
//        new Thread(()->{
//            LOGGER.info("Creating daily contracts universe");
//            appController.setContractsDailyCon(appController.contractRepository.findTop100ByActiveAndType(true, "DAILYCON"));
//            appController.getContractsDailyCon().forEach((contract->{
//                appController.getContractsBySymbol().put(contract.getSymbol(), contract);
//            }));
//
//            for(ContractBasic contract : appController.getContractsDailyCon()){
//                createGenerator(contract);
//                createProcessor(contract, 1380);
//            }
////            dbb.getDailyConHisto();
////            appController.saverController.saveNow(true);
//
//            LOGGER.info(">>Daily contracts universe created");
//        }).start();
//    }

    @Scheduled(fixedRate = 5000)
    public void clock() {
        this.wsController.sendPrices(appController.getGeneratorsState());
    }


}
