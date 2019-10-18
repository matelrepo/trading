package io.matel.trader;

import io.matel.common.Global;
import io.matel.trader.tools.BeanFactory;
import io.matel.trader.domain.ContractBasic;
import io.matel.trader.repository.ContractRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class TraderLauncher implements CommandLineRunner {

    private Map<Long, Generator> generators = new ConcurrentHashMap<>();

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<ContractBasic> contracts;

    private ContractRepository contractRepository;
    private BeanFactory beanFactory;
    private Global global;

    public TraderLauncher(ContractRepository contractRepository, BeanFactory beanFactory, Global global) {
        this.contractRepository = contractRepository;
        this.beanFactory = beanFactory;
        this.global = global;
    }


    @Override
    public void run(String... args) throws Exception {
        contracts = this.contractRepository.findAll();
        contracts.forEach((contract) -> {
            executor.execute(() -> {
                run(contract);
            });
        });
    }

    public void run(ContractBasic contract) {
        System.out.println("Starting trader app");
        Generator generator = this.createGenerator(contract, false);

        switch (Global.RUNNING_STATE) {
            case 1: // running ticks
//                long idtick = getLastTickIdComputed(contract);
//                if (idtick > 0)
//                    global.setIdTick(idtick);
//                System.out.println(">>>> Last ticks computed: " + idtick);
//                generator.getDatabase().getDabaseHisto().getTicks2018ByContract(contract, generator);
//                generator.getDatabase().getDabaseHisto().getTicks2019ByContract(contract, generator, idtick);
//                System.out.println("Database histo loaded...");
//                generator.getDatabase().getDabaseLive().getTicks2019ByContract(contract, generator, idtick);
                System.out.println("Database remote loaded...");
                break;
            case 2: // running merges
                break;
            default:
                break;
        }



                if (global.isOnline) {
                    new Thread(() -> {
//                dataService.connectLive(contract, generator);
                    }).start();
                }else{
                    generator.createTicks();
                }



        global.completedContracts++;
        if (global.completedContracts == global.numContracts) {
            global.hasCompletedLoading = true;
        }
    }


    private Generator createGenerator(ContractBasic contract, boolean randomGen) {
        Generator gen = beanFactory.createBeanGenerator(contract, randomGen);
//        global.getFreqList().forEach(freq -> {
//            gen.createProcessor(freq);
//        });
        generators.put(gen.getIdcontract(), gen);
        return gen;
    }



}
