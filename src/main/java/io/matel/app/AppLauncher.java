package io.matel.app;

import io.matel.app.config.Ibconfig.DataService;
import io.matel.app.config.Global;
import io.matel.app.config.tools.MailService;
import io.matel.app.controller.ContractController;
import io.matel.app.controller.WsController;
import io.matel.app.database.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
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
    private int numContracts = 0;

    @Autowired
    Global global;

    @Autowired
    ContractController contractController;


//    @Autowired
//    ProcessorStateRepo processorStateRepo;

    @Autowired
    MailService mailService;


    DataService dataService;

    public AppLauncher(AppController appController, WsController wsController, @Lazy DataService dataService) {
        this.dataService = dataService;
        this.appController = appController;
        this.wsController = wsController;
    }

    @Override
    public void run(String... args) {

        LOGGER.info("Starting with " + Global.EXECUTOR_THREADS + " cores");
        if(Global.ONLINE){
            dataService.connect();
        }else{
            startLive();
        }
    }


    public void startLive() {
        try {
            Database database = appController.createDatabase("matel", Global.PORT, "matel");
            contractController.setContracts(contractController.initContracts(true));

            numContracts = contractController.getContracts().size();
            LOGGER.info(contractController.getContracts().size() + " contracts found");
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

            if (Global.READ_ONLY_TICKS) {
                LOGGER.warn(">>> Read only lock! <<<");
                mailService.sendMessage(">>> Read only lock! <<<", "", true);
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
                            appController.simulateHistorical(error.idcontract, null, false);

                            generator.getDatabase().getSaverController().saveNow(generator, true);
//                            generator.getProcessors().forEach((freq, proc)->{
//                                processorStateRepo.save(proc.getProcessorState());
//                            });

                        }

                        if (!Global.COMPUTE_DEEP_HISTORICAL && !error.errorDetected) {
                            appController.loadHistoricalData(generator, null);
                            appController.computeTicks(generator, error.lastCandleId);
                            generator.getDatabase().getSaverController().saveNow(generator, true);
                            generator.getProcessors().forEach((freq, proc)->{
                                if(proc.getFlow().size()>0)
                                    generator.getGeneratorState().setLastPrice(proc.getFlow().get(0).getClose());
                            });
                        }
                        generator.setDatabase(database);
                        semaphore.release();


                        try {
                            generator.setDatabase(appController.getDatabase());
                            appController.connectMarketData(generator.getContract());
                            numContracts = numContracts -1;
                            Thread.sleep(1000);
                            LOGGER.info("Loading completed for contract " + generator.getContract().getIdcontract());
                            LOGGER.info("Remaining contracts " + numContracts +"/" + contractController.getContracts().size());
                            if(numContracts ==0) {
                                LOGGER.info("All completed");
                                Global.hasCompletedLoading = true;
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }

                        generator.saveGeneratorState();

                    }).start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }catch(NullPointerException e){
            e.printStackTrace();
        }

    }




    @Scheduled(fixedRate = 5000)
    public void clock() {
        this.wsController.sendPrices(appController.getGeneratorsState());
    }


}
