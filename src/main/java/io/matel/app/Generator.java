package io.matel.app;


import com.ib.client.TickAttrib;
import io.matel.app.config.Ibconfig.DataService;
import io.matel.app.config.Ibconfig.IbClient;
import io.matel.app.config.Global;
import io.matel.app.controller.WsController;
import io.matel.app.database.Database;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.EventType;
import io.matel.app.domain.Tick;
import io.matel.app.repo.ContractRepository;
import io.matel.app.repo.GeneratorStateRepo;
import io.matel.app.repo.ProcessorStateRepo;
import io.matel.app.state.ContractController;
import io.matel.app.state.GeneratorState;
import io.matel.app.config.tools.Utils;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class Generator implements IbClient, ProcessorListener {
    private static final Logger LOGGER = LogManager.getLogger(Generator.class);

    @Autowired
    Global global;

    @Autowired
    WsController wsController;

    @Autowired
    AppController appController;

    @Autowired
    GeneratorStateRepo generatorStateRepo;

    @Autowired
    DataService dataService;

    @Autowired
    ContractRepository contractRepo;

    @Autowired
    ProcessorStateRepo processorStateRepo;

    @Autowired
    ContractController contractController;


    private ContractBasic contract;
    private List<Tick> flowLive = new ArrayList<>();
    private Map<Integer, Processor> processors = new ConcurrentHashMap<>();
    private GeneratorState generatorState;
    private Database database;
    private ZonedDateTime dateNow = ZonedDateTime.now();

    public Database getDatabase() {
        if(database ==null)
            database = appController.getDatabase();
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }


    public Generator(ContractBasic contract, boolean random) {
        this.contract = contract;
        generatorState = new GeneratorState(contract.getIdcontract(), random, 3000);
    }

    public void initDatabase() {
        database = appController.createDatabase("matel", Global.PORT, "matel");
    }

    public void connectMarketData() throws ExecutionException, InterruptedException {
        Random rand = new Random();
        contract = contractRepo.findByIdcontract(contract.getIdcontract());
//        contractController.initContracts(false);
        if (this.generatorState.getMarketDataStatus() > 0 && Global.ONLINE)
            disconnectMarketData(false);

        Double close = database.findTopCloseFromTickByIdContractOrderByTimeStampDesc(contract.getIdcontract());
        if (close == null)
            close = database.findTopCloseFromCandleByIdContractOrderByTimeStampDesc(contract.getIdcontract());

        if (Global.RANDOM) {
            generatorState.setRandomGenerator(true);
            if (close != null)
                generatorState.setLastPrice(close);
            else
                generatorState.setLastPrice(Global.STARTING_PRICE);
        }

        this.generatorState.setMarketDataStatus(2);


        Thread t = new Thread(() -> {
//            contract.setTickSize(0.2);
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
                    int volumeTotal = generatorState.getDailyVolume() + volume;
                    runSize(contract.getIdcontract(), 8, volumeTotal);

                    try {
//                        System.out.println(generatorState.getSpeed());
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else if (Global.ONLINE) {
                try {
                    if (contract.getIdcontract() > 1) {
                        dataService.reqMktData(contract, this);
                    } else {
                        dataService.connectPortfolioUpdate(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.setName(contract.getSymbol() + "." + contract.getCurrency() + "." + contract.getIdcontract());
        t.start();

        if (close != null) {
            if (generatorState.getLastPrice() < 0) {
                Double price = close;
                if (price != null) {
                    generatorState.setLastPrice(price);
                }
            }
        }
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

        if (field == 0 || field == 69) {
            generatorState.setBidQuantity(size);
        }

        if (field == 3 || field == 70) {
            generatorState.setAskQuantity(size);
        }

        if (field == 5 || field == 71) {
            generatorState.setTickQuantity(size);
        }

        if (field == 8 || field == 74) {
            generatorState.setDailyVolume((int) size);
            if (flowLive.size() > 0) {
                flowLive.get(0).setVolume(size - generatorState.getPreviousVolume());
                generatorState.setPreviousVolume(size);
            }
        }

        if (field != 8 && field != 74 && field != 5 && field != 71 && field != 3 && field != 70 && field != 0 && field != 69)
            System.out.println("Size: " + field + " " + size);

    }

    private void updateGeneratorState(Tick tick) {
        this.generatorState.setMarketDataStatus(1);
        double price = tick.getClose();
        generatorState.setIdtick(tick.getId());
        generatorState.setColor(price > generatorState.getLastPrice() ? 1 : -1);

        if (price > generatorState.getHigh())
            generatorState.setHigh(Utils.round(price, contract.getRounding()));

        if (price < generatorState.getLow())
            generatorState.setLow(Utils.round(price, contract.getRounding()));

        generatorState.setTimestamp(tick.getTimestamp());
        generatorState.setLastPrice(tick.getClose());
        generatorState.setChangeValue(Utils.round(generatorState.getLastPrice() - generatorState.getDailyMark(), contract.getRounding()));
        generatorState.setChangePerc(generatorState.getLastPrice() / generatorState.getDailyMark() - 1);

    }

    private void runPrice(long tickerId, int field, double price, TickAttrib attrib) {

        if (price > 0 && (((field == 4 || field == 68) && contract.getFlowType().equals("TRADES"))
                || ((field == 1 || field == 2) && contract.getFlowType().equals("MID")))) {
            double newPrice = reformatPrice(price, field);
            if (newPrice > 0 && newPrice != generatorState.getLastPrice()) {
                Tick tick = new Tick(global.getIdTick(true), contract.getIdcontract(), ZonedDateTime.now().withZoneSameInstant(Global.ZONE_ID), newPrice);
                processPrice(tick, true, true);
                wsController.sendLiveGeneratorState(generatorState);
            }
        } else if ((price > 0 && (field == 1 || field == 2) && contract.getFlowType().equals("TRADES"))) {
            switch (field) {
                case 1:
                    generatorState.setBid(price);
                    break;
                case 2:
                    generatorState.setAsk(price);
                    break;
            }
        }
    }

//    public void process(Candle candle) throws InterruptedException {
//        processors.forEach((freq, processor) -> {
//            processor.process(candle.getTimestamp(), candle.getId(), candle.getOpen(),
//                    candle.getHigh(), candle.getLow(), candle.getClose(), true);
//        });
//    }

    public void processPrice(Tick tick, boolean countConsecutiveUpDown, boolean savingTick) {
        updateGeneratorState(tick);
        flowLive.add(0, tick);
        if (flowLive.size() > Global.MAX_LENGTH_TICKS)
            flowLive.remove(Global.MAX_LENGTH_TICKS);
        if (countConsecutiveUpDown)
            consecutiveUpDownCounter(tick);
        processors.forEach((freq, processor) -> {
            processor.process(tick.getTimestamp(), tick.getId(), null, null, null, tick.getClose(), false);
        });

        if (savingTick) {
            int count = database.getSaverController().saveBatchTicks(flowLive.get(0), false);
            if (count > 0)
                appController.getGeneratorsState().forEach((id, state) -> {
                    generatorStateRepo.save(state);
                });
        }
    }

    private double reformatPrice(double price, int field) {
        double newPrice = -1;
        if (contract.getFlowType().equals("TRADES")) {
            newPrice = Utils.round(price, contract.getRounding());
        } else if (contract.getFlowType().equals("MID")) {
            if (field == 66 || field == 1) {  //bid
                generatorState.setBid(price);
                if (generatorState.getAsk() < 0)
                    generatorState.setAsk(price);
            } else if (field == 67 || field == 2) { //ask
                generatorState.setAsk(price);
                if (generatorState.getBid() < 0)
                    generatorState.setBid(price);
            }

            if (contract.getFusion() == 1) {
                newPrice = Utils.round((generatorState.getAsk() + generatorState.getBid()) / 2, contract.getRounding() - 1);
            } else if (contract.getFusion() == 2) {
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
            generatorState.setTriggerDown(generatorState.getTriggerDown() + 1);
        }
        tick.setTriggerUp(generatorState.getTriggerUp());
        tick.setTriggerDown(generatorState.getTriggerDown());
        flowLive.set(0, tick);
    }

    public void disconnectMarketData(boolean save) {
        if (this.generatorState.getMarketDataStatus() > 0) {
            this.generatorState.setMarketDataStatus(0);
            if (Global.RANDOM)
                generatorState.setRandomGenerator(false);
            if (save) {
                database.getSaverController().saveBatchTicks(true);
            }
            if (contract.getIdcontract() > 1) {
                this.dataService.cancelMktData(contract, true);
            } else {
                this.dataService.connectPortfolioUpdate(false);
            }
        }
    }

    public void saveGeneratorState() {
        generatorStateRepo.save(generatorState);
    }

    public GeneratorState getGeneratorState() {
        return generatorState;
    }

    public Map<Integer, Processor> getProcessors() {
        return processors;
    }

    public ContractBasic getContract() {
        return contract;
    }

    public void setGeneratorState() {
        GeneratorState generatorState = generatorStateRepo.findByIdcontract(contract.getIdcontract());
        if (generatorState != null) {
            this.generatorState = generatorState;
        }
    }

    @Scheduled(fixedRate = 2400000)
    public void clock() {
        try {
            if (ZonedDateTime.now().minusMinutes(1).isAfter(generatorState.getTimestamp())) {
                if (generatorState.getMarketDataStatus() != 0)
                    generatorState.setMarketDataStatus(2);
            } else {
                generatorState.setMarketDataStatus(1);

            }
        } catch (NullPointerException e) {
            e.getMessage();
        }
    }

    @Override
    public void notifyEvent(ProcessorState state) {

        if (Global.COMPUTE_DEEP_HISTORICAL && state.getTimestamp().until(dateNow, ChronoUnit.DAYS) > 80) {
        } else {

                synchronized (this) {
                    processors.forEach((id, proc) -> {
                        if (proc.getFlow().size() > 0)
                            proc.getFlow().get(0).setCheckpoint(true);
                        try {
                            database.getSaverController().saveBatchProcessorState((ProcessorState) proc.getProcessorState().clone(), false);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    });
               }

        }
    }
}


