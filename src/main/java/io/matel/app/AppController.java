package io.matel.app;

import io.matel.app.connection.activeuser.ActiveUserEvent;
import io.matel.app.controller.SaverController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
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

    @Autowired
    CandleRepository candleRepository;

    @Autowired
    TickRepository tickRepository;

    @Autowired
    SaverController saverController;

    @Autowired
    GeneratorStateRepo generatorStateRepo;

    @Autowired
    Global global;

    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);
    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);
    private List<ContractBasic> contractsLive = new ArrayList<>();
    private List<ContractBasic> contractsDailyCon = new ArrayList<>();
    private Map<String, ContractBasic> contractsBySymbol = new HashMap<>();
    private Map<Long, Generator> generators = new ConcurrentHashMap<>();
    private Map<Long, GeneratorState> generatorsState = new ConcurrentHashMap<>();
    private Map<String, ActiveUserEvent> activeUsers = new ConcurrentHashMap<>();  //By SessionId

    public void loadHistoricalCandlesFromDbb(Long idcontract, boolean reset) {
        generators.get(idcontract).getProcessors().forEach((freq, processor) -> {
            if (reset)  processor.resetFlow();
            if(freq>0)
            getCandlesByIdContractByFreq(idcontract, freq);
        });
    }

    public List<Candle> getCandlesByIdContractByFreq(long idcontract, int freq){
            List<Candle> candles;
            if(generators.get(idcontract).getProcessors().get(freq).getFlow().size()>0){
                LOGGER.info("Processor has some flow (" + idcontract +"," + generators.get(idcontract).getProcessors().get(freq).getFlow().size()+")");
                candles = generators.get(idcontract).getProcessors().get(freq).getFlow();
            }else {
                candles = candleRepository.findTop100ByIdcontractAndFreqOrderByTimestampDesc(idcontract, freq);
                if (candles.size() > 0) {
                    LOGGER.info("Loading from dbb " + candles.size() +" (" + idcontract+ "," + freq + ")");
                    generators.get(idcontract).getProcessors().get(freq).setFlow(candles);
                } else {
 //                   LOGGER.info("No historical data for contract " + idcontract + " and freq = " + freq);
                }
            }
        return candles;
    }

    public DatabaseJdbc createDatabase(String databaseName, String port, String username){
        DatabaseJdbc database = beanFactory.createDatabaseJdbc(databaseName, port, username);
        return database;
    }

    public Generator createGenerator(ContractBasic contract){
        Generator generator = beanFactory.createBeanGenerator(contract, Global.RANDOM);
        generators.put(contract.getIdcontract(), generator);
        generator.setGeneratorState();
        generatorsState.put(contract.getIdcontract(), generator.getGeneratorState());
        return generator;
    }

    public void createProcessors(ContractBasic contract, int minFreq){
        Map<Integer, ProcessorState> procData = new HashMap<>();
        processorStateRepository.findByIdcontract(contract.getIdcontract()).forEach(data ->{
            procData.put(data.getFreq(), data);
        });

        for (int frequency : Global.FREQUENCIES) {
            if(frequency >= minFreq) {
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
}
