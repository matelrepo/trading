package io.matel.app;

import io.matel.app.Ibconfig.DataService;
import io.matel.app.config.Global;
import io.matel.app.connection.user.UserRepository;
import io.matel.app.controller.WsController;
import io.matel.app.dailycandle.DailyCandleCsvUploader;
import io.matel.app.domain.ContractBasic;
import io.matel.app.macro.MacroWriter;
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
import java.util.Map;
import java.util.concurrent.*;


@Service
public class AppLauncher implements CommandLineRunner {

    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);
    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);
    private AppController appController;
    private WsController wsController;
    private ConcurrentHashMap<Long, LoadingErrorHandler> errors = new ConcurrentHashMap();


    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GetterCountryCsv getterCountryPrefCsv;

    @Autowired
    Global global;

    @Autowired
    MacroWriter macroWriter;

    @Autowired
    DailyCandleCsvUploader dailyCandleCsvUploader;

    @Autowired
    DataService dataService;


    public AppLauncher(AppController appController, WsController wsController) {
        this.appController = appController;
        this.wsController = wsController;
    }

    @Override
    public void run(String... args) throws Exception {
//        ContractBasic contract = new ContractBasic(200, "SPX", "FUT", "GLOBEX", "USD", "ES", 0.25, 2, "50",
//                "20191220", "2019-01-01", true, "TRADES", 0);
//        appController.contractRepository.save(contract);

//        User trader = new User("trader", passwordEncoder.encode("trader123"), "TRADER", "");
//        List<User> users = Arrays.asList(trader);
//        userRepository.saveAll(users);

//        getterMacroCsv.readCsvLineByLine();
//        getterCountryPrefCsv.readCsvLineByLine();

        LOGGER.info("Starting with " + Global.EXECUTOR_THREADS + " cores");

        new Thread(() -> {
            try {
                if (Global.UPDATE_MACRO)
                    macroWriter.start();
                else
                    LOGGER.info(">>> MACRO_UPDATE is switched off");
//                dbb.init("matel", "5432", "matel");
//                dbb.getMacroItemsByCountry("United States");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        //       dbb.init("cleanm", "5432", "atmuser");

//        dailyCandleCsvUploader.start();
//        startDailyCon();

        startLive();
        //       dataService.connect();

        //    dbb.getTicks2018();

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

    public void startLive() {
        Database database = appController.createDatabase("matel", "5432", "matel");
        appController.setContractsLive(appController.contractRepository.findTop100ByActiveAndType(true, "LIVE"));
        LOGGER.info(appController.getContractsLive().size() + " contracts found");
        try {
            Global.startIdTick = database.findTopIdTickOrderByIdDesc();
            Global.startIdCandle = database.findTopIdCandleOrderByIdDesc();
            global.setIdTick(Global.startIdTick);
            global.setIdCandle(Global.startIdCandle);
        } catch (NullPointerException e) {
        }
        LOGGER.info("Setting up last id tick: " + Global.startIdTick);
        LOGGER.info("Setting up last id candle: " + Global.startIdCandle);

        if (Global.READ_ONLY_TICKS)
            LOGGER.warn(">>> Read only lock! <<<");

        LOGGER.info("Running checks and computation...");
        runChecks(errors);
        appController.getDatabase().getSaverController().saveNow(true);
        LOGGER.info("Checks completed");

//        appController.getContractsLive().forEach(con -> {
//        });


        ConnectMarketData();

        LOGGER.info(">>> Market data connected");

    }

    private class RunChecksTask implements Callable {
        ContractBasic contract;
        Generator generator;
        Map<Long, LoadingErrorHandler> errors;

        public RunChecksTask(ContractBasic contract, Generator generator, ConcurrentHashMap<Long, LoadingErrorHandler> errors) {
            this.contract = contract;
            this.generator = generator;
            this.errors = errors;
        }

        @Override
        public FutureTask<Void> call() {
            LoadingErrorHandler error = new LoadingErrorHandler();
            this.errors.put(contract.getIdcontract(), error);
            error.idcontract = contract.getIdcontract();
            error.numTicksBreaking = generator.getDatabase().countIdTickBreaks(error.idcontract);
            error.lastCandleId = generator.getDatabase().findTopIdCandleByIdcontractOrderByIdDesc(error.idcontract);

            if (error.numTicksBreaking > 1) {
                error.errorDetected = true;
                error.minTickIdBreaking = generator.getDatabase().getSmallestIdTickBreak(error.idcontract);
            }

            System.out.println("Requesting ticks for contract " + error.idcontract + " " + Thread.currentThread());
            Database tickDatabase = appController.createDatabase("cleanm", Global.port, "atmuser");
            tickDatabase.getTicks2018(error.idcontract);
            tickDatabase.close();

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
//                appController.loadHistoricalCandlesFromDbb(contract.getIdcontract(), false);
                appController.connectMarketData(contract);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void runChecks(ConcurrentHashMap errors) {
        List<FutureTask<Void>> tasks = new ArrayList<>();
        for (ContractBasic contract : appController.getContractsLive()) {
            Generator generator = createGenerator(contract);
            generator.initDatabase();
            createProcessor(contract, 0);
            if (Global.COMPUTE) {
                RunChecksTask callable = new RunChecksTask(contract, generator, errors);
                FutureTask task = new FutureTask(callable);
                tasks.add(task);
                executor.execute(task);
            }
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

    private void ConnectMarketData() {
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

    private Generator createGenerator(ContractBasic contract) {
        return appController.createGenerator(contract);
    }

    private void createProcessor(ContractBasic contract, int minFreq) {
        appController.createProcessors(contract, minFreq);
    }

    @Scheduled(fixedRate = 5000)
    public void clock() {
        this.wsController.sendPrices(appController.getGeneratorsState());
    }


}
