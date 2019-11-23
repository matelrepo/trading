package io.matel.app.domain;

import io.matel.app.Generator;
import io.matel.app.Processor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
}
