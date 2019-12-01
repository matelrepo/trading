package io.matel.app;

import io.matel.app.config.DatabaseJdbc;
import io.matel.app.config.Global;
import io.matel.app.connection.user.UserRepository;
import io.matel.app.controller.WsController;
import io.matel.app.dailycon.DaylisCsvUploader;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.macro.MacroWriter;
import io.matel.app.macro.config.GetterCountryCsv;
import io.matel.app.repo.TickRepository;
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
    MacroWriter macroWriter;

    @Autowired
    DatabaseJdbc dbb;

    @Autowired
    DaylisCsvUploader daylisCsvUploader;


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


        new Thread(() -> {
            try {
//                stocksCsvLoader.readCsvLineByLine();
//                LOGGER.info(">>> Stocks uploader");
                if (Global.UPDATE_MACRO)
                    macroWriter.start();
                else
                    LOGGER.info(">>> MACRO_UPDATE is switched off");
                dbb.init("matel", "5432");
                dbb.getMacroItemsByCountry("United States");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


//        dailyConCsvUploader.readCsvLineByLine();
        startLive();
//        startDailyCon();
    }

    private Generator createGenerator(ContractBasic contract){
        return appController.createGenerator(contract);
    }

    private void createProcessor(ContractBasic contract, int minFreq){
        appController.createProcessors(contract, minFreq);
    }

    public void startDailyCon(){
        new Thread(()->{
            LOGGER.info("Creating daily contracts universe");
            appController.setContractsDailyCon(appController.contractRepository.findTop100ByActiveAndType(true, "DAILYCON"));
            appController.getContractsDailyCon().forEach((contract->{
                appController.getContractsBySymbol().put(contract.getSymbol(), contract);
            }));

            for(ContractBasic contract : appController.getContractsDailyCon()){
                Generator generator = createGenerator(contract);
                createProcessor(contract, 1380);
            }

            dbb.getDailyConHisto();
            appController.saverController.saveNow(true);

            LOGGER.info(">>Daily contracts universe created");
        }).start();
    }

    public void startLive() throws InterruptedException, ExecutionException {
        appController.setContractsLive(appController.contractRepository.findTop100ByActiveAndType(true, "LIVE"));
//        List<ContractBasic> list = new ArrayList<>();
//        list.add(appController.contractRepository.findByIdcontract(8));
//        appController.setContractsLive(list);

        try {
            global.setIdTick(tickRepository.findTopByOrderByIdDesc().getId());
            global.setIdCandle(appController.candleRepository.findTopByOrderByIdDesc().getId());
        } catch (NullPointerException e) {
        }
        LOGGER.info("Setting up last id tick: " + global.getIdTick(false));
        if (Global.READ_ONLY_TICKS)
            LOGGER.warn(">>> Read only lock! <<<");

        runChecks();

        if (!errorDetected)
            LOGGER.warn(">>> No computation errors detected");
        LOGGER.info(">>> Computation completed!");

        ConnectMarketData();

        global.setHasCompletedLoading(true);
        LOGGER.info(">>> Market data connected");

    }

    private class RunChecksTask implements Callable {
        ContractBasic contract;
        Generator generator;

        public RunChecksTask(ContractBasic contract, Generator generator) {
            this.contract = contract;
            this.generator = generator;
        }

        @Override
        public FutureTask<Void> call() {
            Tick lastTick = appController.tickRepository.findTopByIdcontractOrderByTimestampDesc(contract.getIdcontract());

            if(lastTick != null)
            appController.candleRepository.deleteIncorrectCandles(contract.getIdcontract(), lastTick.getId());

            long numTickUsed = appController.candleRepository.countIdTickBreaks(contract.getIdcontract());

            Candle lastCandle = appController.candleRepository.findTopByIdcontractOrderByIdDesc(contract.getIdcontract());




            if (numTickUsed > 1) {
                errorDetected = true;
                if(contract.getIdcontract()==8)
                    LOGGER.warn(">>> ERROR >>> More than one tick used with for candles for contract: " + contract.getIdcontract() + " >>> " + numTickUsed);
                long minTickUsed = appController.candleRepository.getSmallestIdTickBreak(contract.getIdcontract());
                if(contract.getIdcontract()==8)
                    LOGGER.warn(">>> Minimum ticks for reference >>> " + minTickUsed);
                appController.candleRepository.deleteIncorrectCandles(contract.getIdcontract(), minTickUsed);
                if(contract.getIdcontract()==8)
                    LOGGER.warn(">>> Clean up completed!");
            }

//            if(numTickUsed != 0) { //=0 no candles
//            }


            if ((numTickUsed != 1) || (numTickUsed == 1 && lastTick.getId() > lastCandle.getIdtick())) { //needs recompute
                if (lastTick != null) {
                    LOGGER.info(">>> Computation started for contract " + contract.getIdcontract());
                    List<Tick> ticks = null;
                    if (numTickUsed == 0) {
                        ticks = tickRepository.getTicksGreatherThanTickByIdContractByOrderByTimestamp(contract.getIdcontract(), 0);
                    } else {
                        LOGGER.warn(">>> Loading clean historical candles");
                        appController.loadHistoricalCandlesFromDbb(contract.getIdcontract(), false);
                        ticks = tickRepository.getTicksGreatherThanTickByIdContractByOrderByTimestamp(contract.getIdcontract(), lastCandle.getIdtick());
                    }
                    ticks.forEach(tick -> generator.processPrice(tick, false));
                    appController.saverController.saveNow(true);
                } else {
                    LOGGER.info(">>> No ticks for contract " + contract.getIdcontract());
                }
            }else{
                LOGGER.warn(">>> Loading clean historical candles");
                appController.loadHistoricalCandlesFromDbb(contract.getIdcontract(), false);
            }
            return null;
        }
    }

    private class LoadAndConnectTask implements Callable {
        ContractBasic contract;

        public LoadAndConnectTask(ContractBasic contract) {
            this.contract = contract;
        }

        @Override
        public FutureTask<Void> call() {
            try {
                appController.connectMarketData(contract);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void runChecks(){
        List<FutureTask<Void>> tasks = new ArrayList<>();
        for (ContractBasic contract : appController.getContractsLive()) {
            Generator generator = createGenerator(contract);
            createProcessor(contract, 0);
            RunChecksTask callable = new RunChecksTask(contract, generator);
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

    private void ConnectMarketData(){
        List<FutureTask<Void>> tasks = new ArrayList<>();
        for (ContractBasic contract : appController.getContractsLive()) {
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

    @Scheduled(fixedRate = 5000)
    public void clock() {
        this.wsController.sendPrices(appController.getGeneratorsState());
    }



}
