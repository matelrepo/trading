package io.matel.app.config.Ibconfig;


import com.ib.client.Contract;
import com.ib.client.TickAttribBidAsk;
import io.matel.app.AppController;
import io.matel.app.config.Global;
import io.matel.app.controller.ContractController;
import io.matel.app.domain.ContractBasic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataService {
    private static final Logger LOGGER = LogManager.getLogger(DataService.class);

    private Map<Long, IbClient> liveMarketDataHandler = new ConcurrentHashMap<>();
    public static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

    private int numMktDataLines = 0;
    private String host = "127.0.0.1";
    private int port = 7496; //7496

    EWrapperImpl eWrapper;

    @Autowired
    AppController appController;

    @Autowired
    ContractController contractController;

    public  DataService(@Lazy EWrapperImpl eWrapper){
        this.eWrapper = eWrapper;
    }


    public void connect() {
        Random rand = new Random();
        int clientId = rand.nextInt(10000);
        while (clientId == 0)
            clientId = rand.nextInt(1000);
        eWrapper.getClient().eConnect(host, port, clientId);
    }

    public Map<Long, IbClient> getLiveMarketDataHandler() {
        return liveMarketDataHandler;
    }

//    public void reconnectAllMktData(){
//        LOGGER.info(repoIB.size() + " market data found");
//        repoIB.forEach((id, gen)->{
//            cancelMktData(appController.getGenerators().get(id).getContract(), false);
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//
//        repoIB.forEach((id, gen)->{
//            try {
//                reqMktData(appController.getGenerators().get(id).getContract(), appController.getGenerators().get(id));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//
//
//    }


    public void reqHistoricalData(ContractBasic contract){
        eWrapper.getClient().reqHistoricalData((int) contract.getIdcontract(), contractController.contractBasic2ContractIB(contract),
                "","20 D", "5 mins", "TRADES",0,1,false,null );

    }

    public void reqContractDetails(ContractBasic contract){
        System.out.println(contractController.contractBasic2ContractIB(contract).toString());
        eWrapper.getClient().reqContractDetails((int) contract.getIdcontract(), contractController.contractBasic2ContractIB(contract));
    }

    public void cancelMktData(ContractBasic contract, boolean cancelRepo) throws InterruptedException{
        Thread.sleep(50);
        LOGGER.info("Cancelling market data for contract " + contract.getIdcontract());
        eWrapper.getClient().cancelMktData((int) contract.getIdcontract());
        if(cancelRepo)
        liveMarketDataHandler.remove(contract.getIdcontract());
        numMktDataLines--;
    }

    public void connectPortfolioUpdate(boolean connect)   {
        eWrapper.getClient().reqAccountUpdates(connect, Global.ACCOUNT_NUMBER);
    }


    public void reqMktData(ContractBasic contract, IbClient handler) throws InterruptedException {
        Thread.sleep(50);
        LOGGER.info("Requesting market data for contract " + contract.getIdcontract());
        eWrapper.getClient().reqMarketDataType(3);
        if (Global.ONLINE) {
            liveMarketDataHandler.put(contract.getIdcontract(), handler);
            eWrapper.getClient().reqMktData((int) contract.getIdcontract(), contractController.contractBasic2ContractIB(contract), "", false, false, null);
            numMktDataLines++;
        }
    }


    public void reqTickByTickData(ContractBasic contract , IbClient handler) {
        if (Global.ONLINE) {
            liveMarketDataHandler.put(100000L, handler);
            liveMarketDataHandler.put(100001L, handler);
            eWrapper.getClient().reqTickByTickData(100000, contractController.contractBasic2ContractIB(contract), "Last", 0, false);
            eWrapper.getClient().reqTickByTickData(100001, contractController.contractBasic2ContractIB(contract), "BidAsk", 0, false);

        }
    }

}
