package io.matel.app;


import com.ib.client.TickAttrib;
import io.matel.app.config.Global;
import io.matel.app.controller.SaverController;
import io.matel.app.controller.WsController;
import io.matel.app.repo.CandleRepository;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.repo.GeneratorStateRepo;
import io.matel.app.repo.TickRepository;
import io.matel.app.state.GeneratorState;
import io.matel.app.tools.IBClient;
import io.matel.app.tools.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;


public class Generator implements IBClient {
    private ExecutorService executor = Executors.newFixedThreadPool(Global.EXECUTOR_THREADS);
    private static final Logger LOGGER = LogManager.getLogger(Generator.class);

    @Autowired
    Global global;

    @Autowired
    WsController WsController;

    @Autowired
    AppController appController;

    @Autowired
    TickRepository tickRepository;

    @Autowired
    CandleRepository candleRepository;

    @Autowired
    SaverController saverController;

    @Autowired
    GeneratorStateRepo generatorStateRepo;


    private ContractBasic contract;
    private List<Tick> flowLive = new ArrayList<>();
    private List<Tick> flowDelayed = new ArrayList<>();
    private Map<Integer, Processor> processors = new ConcurrentHashMap<>();
    private GeneratorState generatorState;


    public Generator(ContractBasic contract, boolean random) {
        this.contract = contract;
        generatorState = new GeneratorState(contract.getIdcontract(), random, 1000);
    }

    public void connectMarketData() throws ExecutionException, InterruptedException {
        Random rand = new Random();
        if (this.generatorState.isConnected())
            disconnectMarketData(true);

        if (Global.RANDOM) {
            generatorState.setRandomGenerator(true);
            Tick tick = tickRepository.findTopByIdcontractOrderByTimestampDesc(contract.getIdcontract());
            if (tick != null) {
                generatorState.setLastPrice(tick.getClose());
            } else {
                generatorState.setLastPrice(Global.STARTING_PRICE);
            }
        }

        this.generatorState.setConnected(true);

        Thread t = new Thread(() -> {
            if (Global.RANDOM) {
                while (generatorState.isRandomGenerator()) {
                    double price = 0;
                    if (Math.random() > 0.50) {
                        price = generatorState.getLastPrice() + contract.getTickSize();
                    } else {
                        price = generatorState.getLastPrice() - contract.getTickSize();
                    }
                    runPrice(contract.getIdcontract(), 4, price, null);
                    int volume = rand.nextInt(1000);
                    runSize(contract.getIdcontract(), 5, volume);
                    int volumeTotal = generatorState.getTotalVolume() + volume;
                    runSize(contract.getIdcontract(), 8, volumeTotal);

                    try {
                        Thread.sleep(generatorState.getSpeed());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //reqMarketData
            }
        });

        t.setName(contract.getSymbol() + "." + contract.getCurrency() + "." + contract.getIdcontract());
        t.start();

        Tick tick = tickRepository.findTopByIdcontractOrderByTimestampDesc(contract.getIdcontract());
        if (tick != null) {
            if (generatorState.getLastPrice() < 0) {
                Double price = tick.getClose();
                if (price != null) {
                    generatorState.setLastPrice(price);
                }
            }
        }
    }


    @Override
    public void reconnectMktData() {
//        dataService.connectLive(contract, this);
    }


    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
        runPrice(tickerId, field, price, attrib);
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        runSize(tickerId, field, size);
    }

    private void runSize(long tickerId, int field, int size) {
//        if (field == 8) {
//            generatorState.setTotalVolume(size);
//        } else if (field == 5) {
//            generatorState.setVolume(size);
//            flowLive.get(0).setVolume(size);
//        }
    }

    private void runPrice(long tickerId, int field, double price, TickAttrib attrib) {
//        if(!lock) {
        generatorState.setTimestamp(ZonedDateTime.now());

        if (price > 0 && (field == 4 || field == 68)) {
            double newPrice = reformatPrice(price);
            if (newPrice > 0 && newPrice != generatorState.getLastPrice()) {
                generatorState.setColor(newPrice > generatorState.getLastPrice() ? 1 : -1);
                Tick tick = new Tick(contract.getIdcontract(), generatorState.getTimestamp(), newPrice);
                tick.setSpeed(generatorState.getSpeed());
                tick.setId(global.getIdTick(true));
                generatorState.setIdtick(tick.getId());

                processPrice(tick, true);
                savingTick();
            }
        }
//        }
    }

    public void processPrice(Tick tick, boolean triggerProcessing) {
        flowLive.add(0, tick);
        if (flowLive.size() > Global.MAX_LENGTH_TICKS)
            flowLive.remove(Global.MAX_LENGTH_TICKS);
        if (triggerProcessing)
            consecutiveUpDownCounter(tick);
        processors.forEach((freq, processor) -> {
            processor.process(tick);
        });
        generatorState.setLastPrice(tick.getClose());
    }

    private double reformatPrice(double price) {
        double newPrice = -1;
        if (contract.getFlowType().equals("TRADES")) {
            newPrice = Utils.round(price, contract.getRounding());
        } else if (contract.getFlowType().equals("MID")) {
            if (contract.getFusion() == 1) {
                if (generatorState.getAsk() < 0)
                    generatorState.setAsk(price);

                generatorState.setBid(price);
                newPrice = Utils.round((generatorState.getAsk() + generatorState.getBid()) / 2, contract.getRounding() - 1);
            } else if (contract.getFusion() == 2) {
                if (generatorState.getBid() < 0)
                    generatorState.setBid(price);

                generatorState.setAsk(price);
                newPrice = Utils.round((generatorState.getAsk() + generatorState.getBid()) * 5 * Math.pow(10, (double) (contract.getRounding() - 2)) / 2, 0);
                newPrice = Utils.round(newPrice / (5 * Math.pow(10, (double) (contract.getRounding() - 2))), contract.getRounding());
            } else {
                newPrice = Utils.round((generatorState.getAsk() + generatorState.getBid()) / 2, contract.getRounding());
            }
        }
        return newPrice;
    }

    private void consecutiveUpDownCounter(Tick tick) {
        if (flowLive.get(0).getClose() > generatorState.getLastPrice()) {
            generatorState.setTriggerUp(generatorState.getTriggerUp() + 1);
            generatorState.setTriggerDown(0);
        } else if (flowLive.get(0).getClose() < generatorState.getLastPrice()) {
            generatorState.setTriggerUp(0);
            generatorState.setTriggerDown(generatorState.getTriggerUp() + 1);
        }
        tick.setTriggerUp(generatorState.getTriggerUp());
        tick.setTriggerDown(generatorState.getTriggerDown());
        flowLive.set(0, tick);
    }

    private void savingTick() {
        if (flowLive.size() > 2) {
            if (!flowLive.get(2).isDiscarded()
                    && ((flowLive.get(2).getTriggerDown() == 1 && flowLive.get(2).getTriggerUp() == 0 && flowLive.get(1).getTriggerDown() == 0
                    && flowLive.get(1).getTriggerUp() == 1 && flowLive.get(0).getTriggerDown() == 1 && flowLive.get(0).getTriggerUp() == 0)
                    || (flowLive.get(2).getTriggerDown() == 0 && flowLive.get(2).getTriggerUp() == 1 && flowLive.get(1).getTriggerDown() == 1
                    && flowLive.get(1).getTriggerUp() == 0 && flowLive.get(0).getTriggerDown() == 0 && flowLive.get(0).getTriggerUp() == 1))) {
                flowLive.get(2).setDiscarded(true);
                flowLive.get(1).setDiscarded(true);
            } else {
                if (!flowLive.get(2).isDiscarded()) {
                    flowDelayed.add(0, flowLive.get(2));
//                    saverController.saveBatchTicks(flowDelayed.get(0));
                    if (flowDelayed.size() > Global.MAX_LENGTH_TICKS)
                        flowDelayed.remove(Global.MAX_LENGTH_TICKS);
                }
            }
        }
        saverController.saveBatchTicks(flowLive.get(0));
    }

    public void disconnectMarketData(boolean save) {
        LOGGER.info("Disconnecting market data for contract " + contract.getIdcontract());
        if (this.generatorState.isConnected()) {
            this.generatorState.setConnected(false);
            if (Global.RANDOM)
                generatorState.setRandomGenerator(false);
            if (save) {
                saverController.saveBatchTicks();
                saverController.saveBatchCandles();
            }
        }
    }

//    public Future<Tick> lastTick() {
//        return executor.submit(() -> );
//    }

    public GeneratorState getGeneratorState() {
        return generatorState;
    }

    public Map<Integer, Processor> getProcessors() {
        return processors;
    }

    public ContractBasic getContract() {
        return contract;
    }

    public GeneratorStateRepo getGeneratorStateRepo() {
        return generatorStateRepo;
    }

    public void setGeneratorState() {
        GeneratorState generatorState = generatorStateRepo.findByIdcontract(contract.getIdcontract());
        if (generatorState != null)
            this.generatorState = generatorState;
    }
}


