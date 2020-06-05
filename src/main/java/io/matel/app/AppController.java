package io.matel.app;

import io.matel.app.config.Ibconfig.DataService;
import io.matel.app.config.BeanFactory;
import io.matel.app.config.Global;
import io.matel.app.config.connection.activeuser.ActiveUserEvent;
import io.matel.app.controller.ContractController;
import io.matel.app.database.Database;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.HistoricalDataType;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Autowired
    HistoricalDataController historicalDataController;





    @Autowired
    ProcessorStateRepo processorStateRepo;

    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);

    public Database getDatabase() {
        if (database == null) {
            database = beanFactory.createDatabaseJdbc("matel", "5432", "matel");
        }
        return database;
    }

    private Map<Long, Generator> generators = new ConcurrentHashMap<>();
    private Map<Long, GeneratorState> generatorsState = new ConcurrentHashMap<>();
    private Map<String, ActiveUserEvent> activeUsers = new ConcurrentHashMap<>();  //By SessionId


    public Database createDatabase(String databaseName, String port, String username) {
       // database = beanFactory.createDatabaseJdbc(databaseName, port, username);
        return beanFactory.createDatabaseJdbc(databaseName, port, username);
    }

    public Generator createGenerator(ContractBasic contract, HistoricalDataType type) {
        Generator generator = beanFactory.createBeanGenerator(contract, Global.RANDOM);
        generators.put(contract.getIdcontract(), generator);
        if(contract.getConid()==null && Global.ONLINE) dataService.reqContractDetails(contract);
        generator.setGeneratorState();
        generatorsState.put(contract.getIdcontract(), generator.getGeneratorState());

        try {
            for (int frequency : generators.get(contract.getIdcontract()).getFrequencies()) {
                if (frequency >= 0) {
                    Processor processor = beanFactory.createBeanProcessor(contract, frequency);
                    processor.addListener(generators.get(contract.getIdcontract()));
                    generators.get(contract.getIdcontract()).getProcessors().put(frequency, processor);
                    if(type!=HistoricalDataType.NONE)
                    historicalDataController.loadHistoricalData(contract.getIdcontract(),contract.getSymbol(),frequency,Global.MAX_LENGTH_CANDLE,Long.MAX_VALUE,true,type);
                    }
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        return generator;
    }


    public void connectMarketData(ContractBasic contract) throws ExecutionException, InterruptedException {
        generators.get(contract.getIdcontract()).connectMarketData();
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
