package io.matel.app;

import io.matel.app.config.Ibconfig.DataService;
import io.matel.app.config.BeanFactory;
import io.matel.app.config.Global;
import io.matel.app.config.connection.activeuser.ActiveUserEvent;
import io.matel.app.controller.ContractController;
import io.matel.app.database.Database;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.repo.*;
import io.matel.app.state.GeneratorState;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Controller
public class AppController {

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    DataService dataService;

//    @Autowired
//    GeneratorStateRepo generatorStateRepo;

    Database database;

    @Autowired
    Global global;

    @Autowired
    ContractController contractController;

     private  Map<Long, Long> processorStatesIdTickByIdContract;
    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);


    public Map<Long,Long> getProcessorStatesIdTickByIdContract(){
    if(processorStatesIdTickByIdContract == null)
        processorStatesIdTickByIdContract = findProcessorStateByContract();
    return processorStatesIdTickByIdContract;
}

    @Autowired
    ProcessorStateRepo processorStateRepo;

    public Database getDatabase() {
        if (database == null) {
            database = beanFactory.createDatabaseJdbc("matel", "5432", "matel");
        }
        return database;
    }

    private Map<Long, Generator> generators = new ConcurrentHashMap<>();
    private Map<Long, GeneratorState> generatorsState = new ConcurrentHashMap<>();
    private Map<String, ActiveUserEvent> activeUsers = new ConcurrentHashMap<>();  //By SessionId

    public boolean loadHistoricalCandlesFromDbb(Long idcontract, String code,  boolean reset, boolean clone, int numCandles) {
        generators.get(idcontract).getProcessors().forEach((freq, processor) -> {
            if (reset) processor.resetFlow();
            if (freq > 0) {
                getCandlesByIdContractByFreq(idcontract, code, freq, null, clone,  numCandles);

            }
        });
        return true;
    }

    public List<Candle> getCandlesByIdContractByFreq(long idcontract, String code, int freq, Long maxIdCandle, boolean clone, int numCandles) {
        List<Candle> candles= new ArrayList<>();
        try {
            if(generators.get(idcontract) != null) {
                if (generators.get(idcontract).getProcessors().get(freq).getFlow().size() > 0) {
                    candles = generators.get(idcontract).getProcessors().get(freq).getFlow();
                } else {
                    candles = generators.get(idcontract).getDatabase().getHistoricalCandles(idcontract, freq, maxIdCandle, clone, numCandles);
                    if (candles.size() > 0) {
                        generators.get(idcontract).getProcessors().get(freq).setFlow(candles);
                    }
                }
            }else{
                contractController.createGenerator(contractController.getDailyContractsBySymbol().get(code));
                contractController.createProcessor(contractController.getDailyContractsBySymbol().get(code));
            }
        }catch(NullPointerException e){
            return candles;
        }
        return candles;
    }

    public Map<Long, Long> findProcessorStateByContract(){
        return getDatabase().findTopIdTickFromProcessorState();
    }

    public Database createDatabase(String databaseName, String port, String username) {
        Database database = beanFactory.createDatabaseJdbc(databaseName, port, username);
        return database;
    }

    public Generator createGenerator(ContractBasic contract) {
        Generator generator = beanFactory.createBeanGenerator(contract, Global.RANDOM);
        generators.put(contract.getIdcontract(), generator);
        if(contract.getConid()==null && Global.ONLINE) dataService.reqContractDetails(contract);
        generator.setGeneratorState();
        generatorsState.put(contract.getIdcontract(), generator.getGeneratorState());
        return generator;
    }

    public void createProcessors(ContractBasic contract)  {
        try {
            for (int frequency : generators.get(contract.getIdcontract()).getFrequencies()) {
                if (frequency >= 0) {
                    Processor processor = beanFactory.createBeanProcessor(contract, frequency);
                    processor.addListener(generators.get(contract.getIdcontract()));
                    generators.get(contract.getIdcontract()).getProcessors().put(frequency, processor);
                }
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    public void loadHistoricalData(Generator generator, int numCandles){
        try {
        long id = generator.getContract().getCloneid()<0 ? generator.getContract().getIdcontract() : generator.getContract().getCloneid();
        loadHistoricalCandlesFromDbb(generator.getContract().getIdcontract(),  generator.getContract().getSymbol(),false,generator.getContract().getCloneid()>0,  numCandles);
            List<ProcessorState> states;
            states = processorStateRepo.findByIdTick(getProcessorStatesIdTickByIdContract().get(id));
            states.forEach(state -> {
                generator.getProcessors().get(state.getFreq()).setProcessorState(state);
            });
       }catch(NullPointerException e){
           e.getMessage();
       }
    }

    public void setHistoricalCandles(Generator gen, Long maxId, boolean clone, int numCandles){
        gen.getProcessors().forEach((freq, proc)->{
            proc.setFlow(getCandlesByIdContractByFreq(gen.getContract().getIdcontract(), gen.getContract().getSymbol(), freq, maxId, clone, numCandles));
        });
    }

    public void computeTicks(Generator generator, long minIdTick){
        LOGGER.info("Computing ticks");
        long id = generator.getContract().getCloneid()<0 ? generator.getContract().getIdcontract() : generator.getContract().getCloneid();
            generator.getDatabase().getTicksByTable(id, false, "public.tick", minIdTick, true, false);
    }

    public void connectMarketData(ContractBasic contract) throws ExecutionException, InterruptedException {
        generators.get(contract.getIdcontract()).connectMarketData();
    }

    public void simulateHistorical(long idcontract, Long idTick, boolean clone){
        LOGGER.info("computing historical");
        Database tickDatabase = createDatabase("cleanm", Global.PORT, "atmuser");
        long minIdTick = idTick == null ? 0 : idTick;
        int count = 0;

        while (minIdTick >= 0) {
            count++;
            minIdTick = tickDatabase.getTicksByTable(idcontract, false, "trading.data18", minIdTick, false, clone);
        }
         minIdTick = idTick == null ? 0 : idTick;
        count = 0;
        while (minIdTick >= 0) {
            count++;
            minIdTick = tickDatabase.getTicksByTable(idcontract, false, "trading.data19", minIdTick,false, clone);
        }
         minIdTick = idTick == null ? 0 : idTick;
        count = 0;
        while (minIdTick >= 0) {
            count++;
            minIdTick = tickDatabase.getTicksByTable(idcontract, false, "trading.data20", minIdTick, false, clone);
        }
        tickDatabase.close();

    }

    public void disconnectAllMarketData(boolean save){
        generators.forEach((id, gen)->{
            gen.disconnectMarketData(save);
        });
    }

    public void connectAllMarketData(){
        generators.forEach((id, gen)->{
            try {
                gen.connectMarketData();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
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

    @PreDestroy
    public void saveNow(){
      database.getSaverController().saveBatchTicks(true);
    }
}
