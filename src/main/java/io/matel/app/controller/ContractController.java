package io.matel.app.controller;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import io.matel.app.AppController;
import io.matel.app.Generator;
import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.repo.ContractRepository;
import io.matel.app.repo.ProcessorStateRepo;
import io.matel.app.state.ProcessorState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;


@Controller
public class ContractController {


    @Autowired
    ContractRepository contractRepository;
    @Autowired
    ProcessorStateRepo processorStateRepo;
    @Autowired
    AppController appController;
    private List<ContractBasic> contracts = new ArrayList<>();

    public Map<Long,List<ContractBasic>> getContractsDetails() {
        return contractsDetails;
    }

    private Map<Long,List<ContractBasic>> contractsDetails = new ConcurrentHashMap<>();

    @Autowired
    HistoricalDataController historicalDataController;

    public List<ContractBasic> getDailyContracts() {
        return dailyContracts;
    }

    private List<ContractBasic> dailyContracts = new ArrayList<>();
    private Map<String, ContractBasic> dailyContractsBySymbol = new HashMap<>();
    //private ContractBasic defaultContract = new ContractBasic(1, "", "STK", "SMART", "USD", "symbol", 0.01, 2, "1", null, null, "TRADES",0, "DAILYCON");

    public synchronized long getLastIdContract(List<ContractBasic> contracts){
        long maxId =0;
        for (ContractBasic contract : contracts) {
            if(contract.getIdcontract()> maxId)
                maxId = contract.getIdcontract();
        }
        return maxId;
    }

    public List<ContractBasic> initContracts(boolean createGenerator) throws NullPointerException {
        List<ContractBasic> list = new ArrayList<>();
          list = contractRepository.findByActiveAndTypeOrderByIdcontract(true, "LIVE");
          dailyContracts = contractRepository.findByActiveAndTypeOrderByIdcontract(true, "DAILY");
        for (ContractBasic dailyContract : dailyContracts) {
            dailyContractsBySymbol.put(dailyContract.getSymbol(), dailyContract);
        }
        //list.add(contractRepository.findByIdcontract(33));
//        list.add(contractRepository.findByIdcontract(23));


        contracts= list;
        if(createGenerator) {
            list.forEach(contract -> {
                appController.createGenerator(contract, true);
                contractsDetails.put(contract.getIdcontract(),new ArrayList<>());
            });
        }
        return list;
    }

    public ContractBasic cloneContract(int idcontract, long idTick) {
        ContractBasic con =null;
        ContractBasic contract= null;
        for (ContractBasic c : contracts) {
            if(c.getIdcontract() == idcontract)
                contract = c;
        }
        try {
            AtomicLong tickThreshold = new AtomicLong(idTick);
             con = (ContractBasic)  contract.clone();
            con.setIdcontract(idcontract+1000);
            con.setTitle(con.getTitle() + " CLONE");
            con.setCloneid(idcontract);
           Generator generator = appController.createGenerator(con, true);
            //createProcessor(con);
            contracts.add(con);
           // appController.loadHistoricalData(generator);
            Map<Integer, Candle> idCandles = generator.getDatabase().getIdCandlesTable(tickThreshold.get(), generator.getContract().getIdcontract()-1000);
//            Map<Integer, ProcessorState> idStates = generator.getDatabase().getProcessorStateTable(tickThreshold.get(), generator.getContract().getIdcontract()-1000);
//            if(idStates.size()>0)
//                idStates.forEach((freq, state)->{
//                    tickThreshold.set(state.getEvent().getIdTick());
//                });
            generator.getProcessors().forEach((freq, processor) -> {
                processor.resetFlow();
                if (freq > 0) {
                  //  historicalDataController.getCandlesByIdContractByFreq(generator.getContract().getIdcontract(), generator.getContract().getSymbol(), freq, idCandles.get(freq).getId(), true, Global.MAX_LENGTH_CANDLE);
                    idCandles.get(freq).setIdcontract(generator.getContract().getIdcontract());
                    processor.getFlow().add(0,idCandles.get(freq));
//                    System.out.println(idCandles.get(freq).toString());
//                    processor.setProcessorState(idStates.get(freq));
                    processor.setFlow(null);
                }
            });
            generator.getGeneratorState().setLastPrice(idCandles.get(generator.getFrequencies()[generator.getFrequencies().length-1]).getClose());
            Global.hasCompletedLoading = true;
            if(Global.ONLINE || Global.RANDOM) {
                    appController.connectMarketData(generator.getContract());
            }else if(Global.HISTO){
                historicalDataController.computingDeepHistorical(generator.getContract().getIdcontract()-1000, tickThreshold.get(), true);
            }
            //appController.computeTicks(genera
                // tor, 0);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return con;
    }

    public void removeContract(int idcontract){
        ContractBasic contract = null;
            for (ContractBasic c : contracts) {
                if (c.getIdcontract() == idcontract)
                    contract = c;
            }
            contracts.remove(contract);
    }

    public Contract contractBasic2ContractIB(ContractBasic contract){
        Contract con = new Contract();
        con.symbol(contract.getSymbol());
        con.secType(contract.getSecType());
        con.currency(contract.getCurrency());
        con.exchange(contract.getExchange());
        con.multiplier(contract.getMultiplier());
        if(contract.getExpiration()!= null)
            con.lastTradeDateOrContractMonth(contract.getExpiration().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        return con;
    }


//    public synchronized  Generator createGenerator(ContractBasic contract) {
//        return appController.createGenerator(contract,1);
//    }

//    public synchronized void createProcessor(ContractBasic contract)  {
//        appController.createProcessors(contract);
//    }

    public List<ContractBasic> findByActiveAndTypeOrderByIdcontract(boolean active, String type){
        return contractRepository.findByActiveAndTypeOrderByIdcontract(active, type);
    }

    public void saveContract(ContractBasic contract){
        contractRepository.save(contract);
    }

    public List<ContractBasic> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractBasic> contracts) {
        this.contracts = contracts;
    }

    public synchronized Map<String, ContractBasic> getDailyContractsBySymbol() {
        return dailyContractsBySymbol;
    }

}
