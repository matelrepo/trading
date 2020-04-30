package io.matel.app.state;

import io.matel.app.AppController;
import io.matel.app.Generator;
import io.matel.app.domain.ContractBasic;
import io.matel.app.repo.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import java.util.List;


@Component
public class ContractController {


    @Autowired
    ContractRepository contractRepository;
    @Autowired
    AppController appController;
    private List<ContractBasic> contracts = new ArrayList<>();


    public List<ContractBasic> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractBasic> contracts) {
        this.contracts = contracts;
    }

    public List<ContractBasic> initContracts() {
        List<ContractBasic> list = new ArrayList<>();
       //   list = contractRepository.findByActiveAndTypeOrderByIdcontract(true, "LIVE");
        list.add(contractRepository.findByIdcontract(5));
       //    list.add(contractRepository.findByIdcontract(98));

        setContracts(list);
        list.forEach(contract->{
            createGenerator(contract);
            createProcessor(contract);
        });
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
             con = (ContractBasic)  contract.clone();
            con.setIdcontract(idcontract+1000);
            con.setTitle(con.getTitle() + " CLONE");
            con.setCloneid(idcontract);
           Generator generator = createGenerator(con);
            createProcessor(con);
            contracts.add(con);
            appController.loadHistoricalData(generator);
            appController.computeTicks(generator, 0);
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
}
