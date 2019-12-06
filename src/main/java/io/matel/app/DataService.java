package io.matel.app;


import com.ib.client.Contract;
import io.matel.app.domain.ContractBasic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataService {
    private static final Logger LOGGER = LogManager.getLogger(DataService.class);

    private Map<Long, IbClient> repoIB = new ConcurrentHashMap<>();

    private int numMktDataLines = 0;
    private String host = "127.0.0.1";
    private int port = 7496;

    @Autowired
    EWrapperImpl eWrapper;

    @Autowired
    AppController appController;


    public void connect() {
        Random rand = new Random();
        int clientId = rand.nextInt(10000);
        while (clientId == 0)
            clientId = rand.nextInt(1000);
        eWrapper.getClient().eConnect(host, port, clientId);
    }

    public Map<Long, IbClient> getRepoIB() {
        return repoIB;
    }

    public void reconnectAllMktData(){
        repoIB.forEach((id, gen)->{
            cancelMktData(appController.getGenerators().get(id).getContract(), false);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        repoIB.forEach((id, gen)->{
            try {
                reqMktData(appController.getGenerators().get(id).getContract(), appController.getGenerators().get(id));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


    }

    public void cancelMktData(ContractBasic contract, boolean cancelRepo){
        LOGGER.info("Cancelling market data for contract " + contract.getIdcontract());
        eWrapper.getClient().cancelMktData((int) contract.getIdcontract());
        if(cancelRepo)
        repoIB.remove(contract.getIdcontract());
        numMktDataLines--;
    }


    public void reqMktData(ContractBasic contract, IbClient handler) throws InterruptedException {
        LOGGER.info("Requesting market data for contract " + contract.getIdcontract());
        eWrapper.getClient().reqMarketDataType(3);
        if (Global.ONLINE) {
            repoIB.put(contract.getIdcontract(), handler);
            Contract con = new Contract();
            con.symbol(contract.getSymbol());
            con.secType(contract.getSecType());
            con.currency(contract.getCurrency());
            con.exchange(contract.getExchange());
            con.multiplier(contract.getMultiplier());
            con.lastTradeDateOrContractMonth(contract.getExpiration());
            eWrapper.getClient().reqMktData((int) contract.getIdcontract(), con, "", false, false, null);
            numMktDataLines++;
            Thread.sleep(10);
        }
    }

}
