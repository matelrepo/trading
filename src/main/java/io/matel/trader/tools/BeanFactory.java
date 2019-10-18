package io.matel.trader.tools;

import io.matel.trader.Generator;
import io.matel.trader.domain.ContractBasic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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

//    public Processor createBeanProcessor(ContractBasic contract, int freq, Database database) {
//        final Processor processing = (Processor) applicationContext.getBean("Processor", contract, freq, database);
//        return processing;
//    }
//
//    public OrderHandler createBeanOrderHandler(Generator gen, int idcontract) {
//        final OrderHandler OrderHandler = (OrderHandler) applicationContext.getBean("OrderHandler", gen, idcontract);
//        return OrderHandler;
//    }
//
//    public EWrapperImpl createEWrapper() {
//        final EWrapperImpl eWrapper = (EWrapperImpl) applicationContext.getBean("EWrapperCustom");
//        return eWrapper;
//    }
//
//    public MyEclientSocket createIBClient(EWrapper eWrapper, EReaderSignal signal) {
//        final MyEclientSocket client = (MyEclientSocket) applicationContext.getBean("ClientIB", eWrapper, signal);
//        return client;
//    }

//	public EReaderSignal createSignalIB() {
//		final EReaderSignal signal = (EReaderSignal) applicationContext.getBean("SignalIB");
//		return signal;
//	}

}