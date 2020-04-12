package io.matel.app;

import io.matel.app.Ibconfig.DataService;
import io.matel.app.config.Global;
import io.matel.app.controller.WsController;
import io.matel.app.domain.ContractBasic;
import io.matel.app.macro.MacroWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Service
public class AppLauncher implements CommandLineRunner {

    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);
    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);
    private AppController appController;
    private WsController wsController;
    private ConcurrentHashMap<Long, LoadingErrorHandler> errors = new ConcurrentHashMap();
    private Semaphore semaphore = new Semaphore(Global.EXECUTOR_THREADS);
    private int numContracts = 0;

    @Autowired
    Global global;

    @Autowired
    MacroWriter macroWriter;

    DataService dataService;

    public AppLauncher(AppController appController, WsController wsController, @Lazy DataService dataService) {
        this.dataService = dataService;
        this.appController = appController;
        this.wsController = wsController;
    }

    @Override
    public void run(String... args) {

        LOGGER.info("Starting with " + Global.EXECUTOR_THREADS + " cores");
        macroUpdate();
        if(Global.ONLINE){
            dataService.connect();
        }else{
            startLive();
        }
    }


    public void startLive() {
        try {
            Database database = appController.createDatabase("matel", Global.PORT, "matel");
            appController.setContractsLive(appController.initContracts());

            numContracts = appController.getContractsLive().size();
            LOGGER.info(appController.getContractsLive().size() + " contracts found");
            Long idTick = database.findTopIdTickOrderByIdDesc();
            if (idTick == null) idTick = 0L;
            Global.startIdTick = idTick;
            Long idCandle = database.findTopIdCandleOrderByIdDesc();
            if (idCandle == null) idCandle = 0L;
            Global.startIdCandle = idCandle;

            global.setIdTick(idTick);
            global.setIdCandle(idCandle);

            LOGGER.info("Setting up last id tick: " + global.getIdTick(false));
            LOGGER.info("Setting up last id candle: " + global.getIdCandle(false));
            database.close();

            if (Global.READ_ONLY_TICKS)
                LOGGER.warn(">>> Read only lock! <<<");

            for (ContractBasic contract : appController.getContractsLive()) {
                    createGenerator(contract);
                    createProcessor(contract, 0);
            }

            LOGGER.info("Loading historical candles...");
            appController.getGenerators().forEach((id, generator) -> {
                try {
                    semaphore.acquire();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
                            Database tickDatabase = appController.createDatabase("cleanm", Global.PORT, "atmuser");
                            long minIdTick = 0;
                            int count = 0;

//                            while (minIdTick >= 0) {
//                                //System.out.println(count + " round(s) with tick " + minIdTick + " 2018");
//                                count++;
//                                minIdTick = tickDatabase.getTicksByTable(error.idcontract, false, "trading.data18", minIdTick);
//                            }
//                            minIdTick = 0;
//                            count = 0;
//                            while (minIdTick >= 0) {
//                                //System.out.println(count + " round(s) with tick " + minIdTick + " 2019");
//                                count++;
//                                minIdTick = tickDatabase.getTicksByTable(error.idcontract, false, "trading.data19", minIdTick);
//                            }
//                            minIdTick = 0;
//                            count = 0;
                            while (minIdTick >= 0) {
                                count++;
                                minIdTick = tickDatabase.getTicksByTable(error.idcontract, false, "trading.data20", minIdTick);
                            }
                            tickDatabase.close();
                            generator.getDatabase().getSaverController().saveNow(generator, true);

                        }

                        if (!Global.COMPUTE_DEEP_HISTORICAL && !error.errorDetected) {
                            appController.loadHistoricalCandlesFromDbb(generator.getContract().getIdcontract(), false);
                            generator.getDatabase().getTicksByTable(error.idcontract, false, "public.tick", error.lastCandleId);
                            generator.getDatabase().getSaverController().saveNow(generator, true);
                        }

                        generator.setDatabase(database);
                        semaphore.release();


                        try {
                            generator.setDatabase(appController.getDatabase());
                            appController.connectMarketData(generator.getContract());
                            numContracts = numContracts -1;
                            Thread.sleep(1000);
                            LOGGER.info("Loading completed for contract " + generator.getContract().getIdcontract());
                            LOGGER.info("Remaining contracts " + numContracts +"/" + appController.getContractsLive().size());
                            if(numContracts ==0) {
                                LOGGER.info("All completed");
                                Global.hasCompletedLoading = true;
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }

                        generator.saveGeneratorState();
                        generator.getProcessors().forEach((freq, proc) -> {
                            proc.saveProcessorState();
                        });

                    }).start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }catch(NullPointerException e){
            e.printStackTrace();
        }

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

    @Scheduled(cron = "0 0 4 * * ?")
    public void macroUpdate(){
                new Thread(() -> {
            try {
                if (Global.UPDATE_MACRO)
                    macroWriter.start();
                else
                    LOGGER.info(">>> MACRO_UPDATE is switched off");
            } catch (Exception e) {
                e.printStackTrace();
            }
                    appController.getDatabase().getMacroItemsByCountry("United States");
                }).start();
    }


}
