package io.matel.trader.tools;

import io.matel.trader.Generator;
import io.matel.trader.domain.ContractBasic;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

@Configuration
public class BeanFactoryConfig {

    @Bean(name = "Generator")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Generator createGenerator(ContractBasic contract, boolean auto) throws ClassNotFoundException, InterruptedException, ExecutionException, SQLException {
        return new Generator(contract, auto);
    }

//    @Bean(name = "Processor")
//    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//    public Processor createProcessor(ContractBasic contract, int freq, Database database) {
//        return new Processor(contract, freq, database);
//    }
//
//    @Bean(name = "OrderHandler")
//    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//    public OrderHandler createOrderHandle(Generator gen, int idcontract) {
//        return new OrderHandler(gen, idcontract);
//    }
//
//    @Bean(name = "EWrapperCustom")
//    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//    public EWrapperImpl createEWrapper() {
//        return new EWrapperImpl();
//    }
//
//    @Bean(name = "ClientIB")
//    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//    public MyEclientSocket createIBClient(EWrapper eWrapper, EReaderSignal signal) {
//        return new MyEclientSocket(eWrapper, signal);
//    }

//	@Bean(name = "SignalIB")
//	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
//	public EReaderSignal createSignalIB() {
//		return new EJavaSignal();
//	}

}
