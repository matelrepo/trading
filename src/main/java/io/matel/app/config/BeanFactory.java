package io.matel.app.config;

import io.matel.app.DailyCompute;
import io.matel.app.database.Database;
import io.matel.app.Generator;
import io.matel.app.Processor;
import io.matel.app.domain.ContractBasic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
public class BeanFactory {

    private ApplicationContext applicationContext;

    @Autowired
    public BeanFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Generator createBeanGenerator(ContractBasic contract, boolean randomGen) {
        final Generator generator = (Generator) applicationContext.getBean("Generator", contract, randomGen);
        return generator;
    }

    public Processor createBeanProcessor(ContractBasic contract, int freq) {
        final Processor processing = (Processor) applicationContext.getBean("Processor", contract, freq);
        return processing;
    }

    public Database createDatabaseJdbc(String databaseName, String port, String username){
        final Database database = (Database) applicationContext.getBean("Database", databaseName, port, username);
    return database;
    }

//    public DailyCompute createDailyCompute(){
//        final DailyCompute dailyCompute = (DailyCompute) applicationContext.getBean("DailyCompute");
//        return dailyCompute;
//    }

}