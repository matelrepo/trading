package io.matel.app;

import io.matel.app.config.BeanFactory;
import io.matel.app.config.Global;
import io.matel.app.connection.activeuser.ActiveUserEvent;
import io.matel.app.controller.SaverController;
import io.matel.app.domain.*;
import io.matel.app.repo.*;
import io.matel.app.state.GeneratorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Controller
public class AppController {

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    ProcessorDataRepository processorDataRepository;

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

    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);
    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);
    private List<ContractBasic> contracts = new ArrayList<>();
    private Map<Long, Generator> generators = new ConcurrentHashMap<>();
    private Map<Long, GeneratorState> generatorsState = new ConcurrentHashMap<>();
    private Map<String, ActiveUserEvent> activeUsers = new ConcurrentHashMap<>();  //By SessionId

    public void loadHistoricalCandles(Long idcontract, boolean reset) {
        getGenerators().forEach((id, gen) -> {
            if (idcontract == id) {
                gen.getProcessors().forEach((freq, processor) -> {
                    try {
                        if (reset)
                            processor.resetFlow();
                        List<Candle> candles = getCandlesFromDatabase(idcontract, freq).get();
                        processor.setFlow(candles);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public Future<List<Candle>> getCandlesFromDatabase(long idcontract, int freq) {
        return executor.submit(() -> candleRepository.findTop100ByIdcontractAndFreqOrderByTimestampDesc(idcontract, freq));
    }

    public Generator createGenerator(ContractBasic contract){
        Generator generator = beanFactory.createBeanGenerator(contract, Global.RANDOM);
        generators.put(contract.getIdcontract(), generator);
        generator.setGeneratorState();
        generatorsState.put(contract.getIdcontract(), generator.getGeneratorState());
        return generator;
    }

    public void createProcessors(ContractBasic contract){
//        Map<Integer, ProcessorData> procData = new HashMap<>();
//        processorDataRepository.findByIdcontract(contract.getIdcontract()).forEach( data ->{
//            procData.put(data.getFreq(), data);
//        });
//
        for (int frequency : Global.FREQUENCIES) {
            Processor processor = beanFactory.createBeanProcessor(contract, frequency);
//            processor.setProcessorData(procData.get(frequency));
            generators.get(contract.getIdcontract()).getProcessors().put(frequency, processor);
        }
    }

    public void connectMarketData(ContractBasic contract) throws ExecutionException, InterruptedException {
        generators.get(contract.getIdcontract()).connectMarketData();
    }

    public List<ContractBasic> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractBasic> contracts) {
        this.contracts = contracts;
    }

    public void setContract(ContractBasic contract) {
        this.contracts.add(contract);
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


    @Scheduled(fixedRate = 67000)
    public void clock() {
        LOGGER.info("Auto-saving");
        generators.forEach((id, gen) -> {
            generatorStateRepo.save(gen.getGeneratorState());
        });
        saverController.saveNow();

    }

}
