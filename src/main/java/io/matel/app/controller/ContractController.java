package io.matel.app.controller;

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

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class ContractController {


    @Autowired
    ContractRepository contractRepository;
    @Autowired
    ProcessorStateRepo processorStateRepo;
    @Autowired
    AppController appController;
    private List<ContractBasic> contracts = new ArrayList<>();


    public List<ContractBasic> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractBasic> contracts) {
        this.contracts = contracts;
    }

    public List<ContractBasic> initContracts(boolean createGenerator) {
        List<ContractBasic> list = new ArrayList<>();
          list = contractRepository.findByActiveAndTypeOrderByIdcontract(true, "LIVE");
        //list.add(contractRepository.findByIdcontract(8));
       //    list.add(contractRepository.findByIdcontract(98));

        setContracts(list);
        if(createGenerator) {
            list.forEach(contract -> {
                createGenerator(contract);
                createProcessor(contract);
            });
        }
        return list;
    }

    public ContractBasic cloneContract(int idcontract) {
        ContractBasic con =null;
        ContractBasic contract= null;
        for (ContractBasic c : contracts) {
            if(c.getIdcontract() == idcontract)
                contract = c;
        }
        try {
            AtomicLong tickThreshold = new AtomicLong(133998632L);
             con = (ContractBasic)  contract.clone();
            con.setIdcontract(idcontract+1000);
            con.setTitle(con.getTitle() + " CLONE");
            con.setCloneid(idcontract);
           Generator generator = createGenerator(con);
            createProcessor(con);
            contracts.add(con);
           // appController.loadHistoricalData(generator);
            Map<Integer, Candle> idCandles = generator.getDatabase().getIdCandlesTable(tickThreshold.get(), generator.getContract().getIdcontract()-1000);
            Map<Integer, ProcessorState> idStates = generator.getDatabase().getProcessorStateTable(tickThreshold.get(), generator.getContract().getIdcontract()-1000);
            if(idStates.size()>0)
                idStates.forEach((freq, state)->{
                    tickThreshold.set(state.getIdTick());
                });
            generator.getProcessors().forEach((freq, processor) -> {
                processor.resetFlow();
                if (freq > 0) {
                    appController.getCandlesByIdContractByFreq(generator.getContract().getIdcontract(), freq, idCandles.get(freq).getId(), true);
                    idCandles.get(freq).setIdcontract(generator.getContract().getIdcontract());
                    processor.getFlow().add(0,idCandles.get(freq));
//                    System.out.println(idCandles.get(freq).toString());
                    processor.setProcessorState(idStates.get(freq));
                    processor.setFlow(null);
                }
            });
            generator.getGeneratorState().setLastPrice(idCandles.get(Global.FREQUENCIES[Global.FREQUENCIES.length-1]).getClose());
            Global.hasCompletedLoading = true;
            if(Global.ONLINE || Global.RANDOM) {
                try {
                    appController.connectMarketData(generator.getContract());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else if(Global.HISTO){
                appController.simulateHistorical(generator.getContract().getIdcontract()-1000, tickThreshold.get(), true);
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


    public Generator createGenerator(ContractBasic contract) {
        return appController.createGenerator(contract);
    }

    public void createProcessor(ContractBasic contract) {
        appController.createProcessors(contract);
    }

    public List<ContractBasic> findByActiveAndTypeOrderByIdcontract(boolean active, String type){
        return contractRepository.findByActiveAndTypeOrderByIdcontract(active, type);
    }

    public void saveContract(ContractBasic contract){
        contractRepository.save(contract);
    }
}
