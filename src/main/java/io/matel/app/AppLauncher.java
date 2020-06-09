package io.matel.app;

import io.matel.app.config.Ibconfig.DataService;
import io.matel.app.config.Global;
import io.matel.app.config.tools.MailService;
import io.matel.app.controller.ContractController;
import io.matel.app.controller.HistoricalDataController;
import io.matel.app.controller.WsController;
import io.matel.app.database.Database;
import io.matel.app.domain.HistoricalDataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;


@Service
public class AppLauncher implements CommandLineRunner {

    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);
   // private ExecutorService executor = Executors.newFixedThreadPool(100);
    private AppController appController;
    private WsController wsController;
    private ConcurrentHashMap<Long, LoadingErrorHandler> errors = new ConcurrentHashMap();
    private Semaphore semaphore = new Semaphore(8);
    private Semaphore semaphoreDaily = new Semaphore(100);

    private int numContracts = 0;

    @Autowired
    Global global;

    @Autowired
    ContractController contractController;

    @Autowired
    MailService mailService;

    @Autowired
    DailyCompute dailyCompute;

    @Autowired
    HistoricalDataController historicalDataController;

    DataService dataService;

    public AppLauncher(AppController appController, WsController wsController, @Lazy DataService dataService) {
        this.dataService = dataService;
        this.appController = appController;
        this.wsController = wsController;
    }

    @Override
    public void run(String... args) {

        LOGGER.info("Starting with " + Global.EXECUTOR_THREADS + " cores");
        if (Global.ONLINE) {
            dataService.connect();
        } else {
            startLive();
        }
    }


    public void startLive() {
        try {
           Database database = appController.getDatabase();
            contractController.setContracts(contractController.initContracts(true));


            numContracts = contractController.getContracts().size();
            LOGGER.info(contractController.getContracts().size() + " contracts live found");
            LOGGER.info(contractController.getDailyContracts().size() + " contracts daily found");

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
           // database.close();

//            List<String> contractsSorted = new ArrayList<>(contractController.getDailyContractsBySymbol().keySet());
//            Collections.sort(contractsSorted);
//
//
//            new Thread(()-> {
//                contractsSorted.forEach((name) -> {
//                    if (contractController.getDailyContractsBySymbol().get(name).getIdcontract() > 47560) {
//                        try {
//                            semaphoreDaily.acquire();
//                        } catch (InterruptedException interruptedException) {
//                            interruptedException.printStackTrace();
//                        }
//                        // executor.execute(()->{
//                        new Thread(() -> {
//                           // System.out.println(Thread.currentThread().getName() + " " + semaphoreDaily.getQueueLength() + " " + name + " " + contractController.getDailyContractsBySymbol().get(name).getIdcontract());
//                            dailyCompute.EODByCode(name);
//                        }).start();
//                    }
//                });
//                }).start();


            int tmp = 1;
            if (tmp > 0) {
                if (Global.READ_ONLY_TICKS) {
                    LOGGER.warn(">>> Read only lock! <<<");
                    mailService.sendMessage(">>> Read only lock! <<<", "", true);
                }

                LOGGER.info("Loading historical candles...");
                historicalDataController.findProcessorStateByContract();
                appController.getGenerators().forEach((id, generator) -> {
                    try {
                        semaphore.acquire();
                        //System.out.println(">>>>Semaphore available >>> " + semaphore.availablePermits() + " -> " +generator.getContract().getIdcontract());
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
                                historicalDataController.computingDeepHistorical(error.idcontract, null, false);
                                generator.getDatabase().getSaverController().saveNow(generator, true);
//                            generator.getProcessors().forEach((freq, proc)->{
//                                processorStateRepo.save(proc.getProcessorState());
//                            });

                            }

                            if (!Global.COMPUTE_DEEP_HISTORICAL && !error.errorDetected) {
                                List<Thread> t = new ArrayList<>();
                                generator.getProcessors().forEach((freq, proc) -> {
                                    t.add(0,new Thread(()->{
                                        try {
                                            historicalDataController.loadHistoricalData(generator.getContract().getIdcontract(), generator.getContract().getSymbol(), freq, Global.MAX_LENGTH_CANDLE, Long.MAX_VALUE, true, HistoricalDataType.DATABASE);
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                    }));
                                    t.get(0).start();
                                });

                                t.forEach(thread->{
                                    try {
                                        thread.join();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                });
                                generator.getDatabase().close();
                                generator.setDatabase(database);
                                historicalDataController.setProcessorState(generator.getContract().getIdcontract());
                                if(Global.READ_ONLY_CANDLES) {
                                    historicalDataController.computeTicks(generator, error.lastCandleId);
                                    generator.getDatabase().getSaverController().saveNow(generator, true);
                                }
                                generator.getProcessors().forEach((freq, proc) -> {
                                    if (proc.getFlow().size() > 0)
                                        generator.getGeneratorState().setLastPrice(proc.getFlow().get(0).getClose());
                                });
                            }
                            //System.out.println("Semaphore available >>> " + semaphore.availablePermits());


                            try {
                                generator.setDatabase(appController.getDatabase());
                                appController.connectMarketData(generator.getContract());
                                synchronized (this) {
                                    numContracts = numContracts - 1;
                                    //Thread.sleep(1000);
                                    LOGGER.info("Loading completed for contract " + generator.getContract().getIdcontract());
                                    LOGGER.info("Remaining contracts " + numContracts + "/" + contractController.getContracts().size());
                                    if (numContracts == 0) {
                                        LOGGER.info("All completed");
                                        Global.hasCompletedLoading = true;
                                    }
                                }
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }

                            generator.saveGeneratorState();
                            semaphore.release();

                        }).start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }


    public synchronized void releaseSemaphore() {
        this.semaphoreDaily.release();
    }

    @Scheduled(fixedRate = 5000)
    public void clock() {
        this.wsController.sendPrices(appController.getGeneratorsState());
        //  System.out.println(appController.getGeneratorsState());
    }


}
