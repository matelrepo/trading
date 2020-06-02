package io.matel.app.config;

import io.matel.app.DailyCompute;
import io.matel.app.database.Database;
import io.matel.app.Generator;
import io.matel.app.Processor;
import io.matel.app.domain.ContractBasic;
import io.matel.app.state.ProcessorState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.Semaphore;

@Configuration
public class BeanFactoryConfig {

    @Bean(name = "Generator")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Generator createGenerator(ContractBasic contract, boolean randomGen) {
        return new Generator(contract, randomGen);
    }

    @Bean(name = "Processor")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Processor createProcessor(ContractBasic contract, int freq) {
        return new Processor(contract, freq);
    }

//    @Bean(name = "DailyCompute")
//    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//    public DailyCompute createDailyCompute() {
//        return new DailyCompute();
//    }

    @Bean(name = "Database")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Database createDatabaseJdbc(String databaseName, String port, String username) {
        return new Database(databaseName, port, username);
    }
}
