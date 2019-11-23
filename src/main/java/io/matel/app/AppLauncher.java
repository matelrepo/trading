package io.matel.app;

import io.matel.app.connection.user.UserRepository;
import io.matel.app.controller.WsController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.domain.TickRepository;
import io.matel.app.macro.Macro;
import io.matel.app.macro.config.GetterCountryCsv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private boolean errorDetected = false;


    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TickRepository tickRepository;

    @Autowired
    GetterCountryCsv getterCountryPrefCsv;

    @Autowired
    Global global;

    @Autowired
    Macro macro;

    public AppLauncher(AppController appController, WsController wsController) {
        this.appController = appController;
        this.wsController = wsController;
    }

    @Override
    public void run(String... args) throws Exception {
//        ContractBasic contract = new ContractBasic(200, "SPX", "FUT", "GLOBEX", "USD", "ES", 0.25, 2, "50",
//                "2019-01-01", "2019-01-01", true, "TRADES", 0);
//        appController.contractRepository.save(contract);

//        User trader = new User("trader", passwordEncoder.encode("trader123"), "TRADER", "");
//        List<User> users = Arrays.asList(trader);
//        userRepository.saveAll(users);

//        getterMacroCsv.readCsvLineByLine();
//        getterCountryPrefCsv.readCsvLineByLine();

        if(Global.UPDATE_MACRO) {
            new Thread(() -> {
                try {
                    macro.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }else{
            LOGGER.info(">>> MACRO_UPDATE is switched off");
        }

        start();
    }

    public void start() throws InterruptedException, ExecutionException {
        appController.setContracts(appController.contractRepository.findByActive(true));
//        appController.setContract(appController.contractRepository.findByIdcontract(6L));
        try {
            global.setIdTick(tickRepository.findTopByOrderByIdDesc().getId());
            global.setIdCandle(appController.candleRepository.findTopByOrderByIdDesc().getId());
        } catch (NullPointerException e) {
        }
        LOGGER.info("Setting up last id tick: " + global.getIdTick(false));
        if (Global.READ_ONLY_TICKS)
            LOGGER.warn(">>> Read only lock! <<<");

        List<FutureTask<Void>> tasks = new ArrayList<>();
        for (ContractBasic contract : appController.getContracts()) {
            RunChecksTask callable = new RunChecksTask(contract);
            FutureTask task = new FutureTask(callable);
            tasks.add(task);
            executor.execute(task);
        }

        tasks.forEach(task-> {
            try {
                task.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        if(!errorDetected)
        LOGGER.warn(">> No computation errors detected");
        LOGGER.info(">>> Computation completed!");

        tasks.clear();
        for (ContractBasic contract : appController.getContracts()) {
            LoadAndConnectTask callable = new LoadAndConnectTask(contract);
            FutureTask task = new FutureTask(callable);
            tasks.add(task);
            executor.execute(task);
        }

        tasks.forEach(task -> {
            try {
                task.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }


    @Scheduled(fixedRate = 1000)
    public void clock() {
        this.wsController.sendPrices(appController.getGeneratorsState());
    }

    private class RunChecksTask implements Callable{
        ContractBasic contract;
        public RunChecksTask(ContractBasic contract){
            this.contract= contract;
        }

        @Override
        public FutureTask<Void> call() {
                Generator generator = appController.createGenerator(contract);
                appController.createProcessors(contract);
                long numTickUsed = appController.candleRepository.countIdTickBreaks(contract.getIdcontract());
                Candle lastCandle = appController.candleRepository.findTopByIdcontractOrderByIdDesc(contract.getIdcontract());
                if (numTickUsed > 1) {
                    errorDetected = true;
                    LOGGER.warn(">>> Error ticks for contract: " + contract.getIdcontract() + " >>> " + numTickUsed);
                    long minTickUsed = appController.candleRepository.getSmallestIdTickBreak(contract.getIdcontract());
                    LOGGER.warn(">>> Minimum ticks for reference >>> " + minTickUsed);
                    appController.candleRepository.deleteIncorrectCandles(contract.getIdcontract(), minTickUsed);
                    LOGGER.warn(">>> Clean up completed!");
                }
                Tick lastTick = appController.tickRepository.findTopByIdcontractOrderByTimestampDesc(contract.getIdcontract());
                if ((lastCandle == null || numTickUsed > 1 || lastTick.getId() > lastCandle.getIdtick())) { //needs recompute
                    if (lastTick != null) {
                        LOGGER.info(">>> Computation started for contract " + contract.getIdcontract());
                        List<Tick> ticks = null;
                        if (lastCandle == null) {
                            ticks = tickRepository.getTicksGreatherThanTickByIdContract(contract.getIdcontract(), 0);
                        } else {
                            ticks = tickRepository.getTicksGreatherThanTickByIdContract(contract.getIdcontract(), lastTick.getId());
                        }
                        ticks.forEach(tick -> generator.processPrice(tick, false));
                    } else {
                        LOGGER.info(">>> No ticks for contract " + contract.getIdcontract());
                    }
                }
                return null;
        }
    }

    private class LoadAndConnectTask implements Callable{
        ContractBasic contract;
        public LoadAndConnectTask(ContractBasic contract){
            this.contract= contract;
        }

        @Override
        public FutureTask<Void> call() {
            appController.loadHistoricalCandles(contract.getIdcontract(), false);
            try {
                appController.connectMarketData(contract);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
