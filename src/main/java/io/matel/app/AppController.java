package io.matel.app;

import io.matel.app.config.BeanFactory;
import io.matel.app.config.Global;
import io.matel.app.connection.activeuser.ActiveUserEvent;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.macro.domain.MacroDAO;
import io.matel.app.repo.*;
import io.matel.app.state.GeneratorState;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class AppController {

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    ProcessorStateRepository processorStateRepository;

    @Autowired
    BeanFactory beanFactory;

//    @Autowired
//    CandleRepository candleRepository;

//    @Autowired
//    TickRepository tickRepository;
//
//    @Autowired
//    SaverController saverController;

    @Autowired
    GeneratorStateRepo generatorStateRepo;

    Database database;

    @Autowired
    Global global;

    public Database getDatabase() {
        if (database == null) {
            database = new Database("matel", "5432", "matel");
        }
        return database;
    }

    private static final Logger LOGGER = LogManager.getLogger(AppController.class);
    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);
    private List<ContractBasic> contractsLive = new ArrayList<>();
    private List<ContractBasic> contractsDailyCon = new ArrayList<>();
    private Map<String, ContractBasic> contractsBySymbol = new HashMap<>();
    private Map<Long, Generator> generators = new ConcurrentHashMap<>();
    private Map<Long, GeneratorState> generatorsState = new ConcurrentHashMap<>();
    private Map<String, ActiveUserEvent> activeUsers = new ConcurrentHashMap<>();  //By SessionId
    private List<MacroDAO> tickerCrawl;


    public boolean loadHistoricalCandlesFromDbb(Long idcontract, boolean reset) {
        generators.get(idcontract).getProcessors().forEach((freq, processor) -> {
            if (reset) processor.resetFlow();
            if (freq > 0)
               getCandlesByIdContractByFreq(idcontract, freq);
        });
        return true;
    }

    public List<Candle> getCandlesByIdContractByFreq(long idcontract, int freq) {
        List<Candle> candles;
        if (generators.get(idcontract).getProcessors().get(freq).getFlow().size() > 0) {
            candles = generators.get(idcontract).getProcessors().get(freq).getFlow();
        } else {
            candles = generators.get(idcontract).getDatabase().findTop100ByIdcontractAndFreqOrderByTimestampDesc(idcontract, freq);
            if (candles.size() > 0)
                generators.get(idcontract).getProcessors().get(freq).setFlow(candles);
        }
        return candles;
    }

    public Database createDatabase(String databaseName, String port, String username) {
        Database database = beanFactory.createDatabaseJdbc(databaseName, port, username);
        return database;
    }

    public Generator createGenerator(ContractBasic contract) {
        Generator generator = beanFactory.createBeanGenerator(contract, Global.RANDOM);
        generators.put(contract.getIdcontract(), generator);
        generator.setGeneratorState();
        generatorsState.put(contract.getIdcontract(), generator.getGeneratorState());
        return generator;
    }

    public void createProcessors(ContractBasic contract, int minFreq) {
        Map<Integer, ProcessorState> procData = new HashMap<>();
        processorStateRepository.findByIdcontract(contract.getIdcontract()).forEach(data -> {
            procData.put(data.getFreq(), data);
        });

        for (int frequency : Global.FREQUENCIES) {
            if (frequency >= minFreq) {
                Processor processor = beanFactory.createBeanProcessor(contract, frequency);
                processor.setProcessorState(procData.get(frequency));
                generators.get(contract.getIdcontract()).getProcessors().put(frequency, processor);
            }
        }
    }

    public void connectMarketData(ContractBasic contract) throws ExecutionException, InterruptedException {
        generators.get(contract.getIdcontract()).connectMarketData();
    }

    public List<ContractBasic> getContractsLive() {
        return contractsLive;
    }

    public void setContractsLive(List<ContractBasic> contracts) {
        this.contractsLive = contracts;
    }

    public Map<Long, Generator> getGenerators() {
        return generators;
    }

    public Map<Long, GeneratorState> getGeneratorsState() {
        return generatorsState;
    }

    public Map<String, ActiveUserEvent> getActiveUsers() {
        return activeUsers;
    }


    public List<ContractBasic> getContractsDailyCon() {
        return contractsDailyCon;
    }

    public void setContractsDailyCon(List<ContractBasic> contractsDailyCon) {
        this.contractsDailyCon = contractsDailyCon;
    }

    public Map<String, ContractBasic> getContractsBySymbol() {
        return contractsBySymbol;
    }

    public void setContractsBySymbol(Map<String, ContractBasic> contractsBySymbol) {
        this.contractsBySymbol = contractsBySymbol;
    }

//    @Scheduled(fixedRate = 67000)
//    public void clock() {
////        if(global.isHasCompletedLoading()) {
////            LOGGER.info("Auto-saving");
////            if(Global.READ_ONLY_CANDLES)
////            generators.forEach((id, gen) -> {
////                if (gen.getGeneratorState().getLastPrice() > 0)
////                    generatorStateRepo.save(gen.getGeneratorState());
////
////                gen.getProcessors().forEach((freq, processor)->{
////                    processorStateRepository.save(processor.getProcessorState());
////                });
////            });
//        saverController.saveBatchTicks();
//        saverController.saveGeneratorStates();
//
////        }
//    }

    public List<MacroDAO> getTickerCrawl() {
        return tickerCrawl;
    }

    public void setTickerCrawl(List<MacroDAO> tickerCrawl) {
        this.tickerCrawl = tickerCrawl;
    }
}
