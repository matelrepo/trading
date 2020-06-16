package io.matel.app;


import com.ib.client.TickAttrib;
import com.ib.client.TickAttribBidAsk;
import com.ib.client.TickAttribLast;
import io.matel.app.config.Ibconfig.DataService;
import io.matel.app.config.Ibconfig.IbClient;
import io.matel.app.config.Global;
import io.matel.app.controller.ContractController;
import io.matel.app.controller.WsController;
import io.matel.app.database.Database;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.domain.TimeSales;
import io.matel.app.repo.ContractRepository;
import io.matel.app.repo.GeneratorStateRepo;
import io.matel.app.repo.ProcessorStateRepo;
import io.matel.app.repo.TimeSalesRepo;
import io.matel.app.state.GeneratorState;
import io.matel.app.config.tools.Utils;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.DateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class Generator implements IbClient, ProcessorListener {
    private static final Logger LOGGER = LogManager.getLogger(Generator.class);


    public int[] getFrequencies() {
        return frequencies;
    }


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

    @Autowired
    TimeSalesRepo timeSalesRepo;

    private boolean checkpoint = false;
    private OffsetDateTime checkpoint_stamp = null;


    private ContractBasic contract;
    private List<Tick> flowLive = new ArrayList<>();
    private List<Tick> flowDelayed = new ArrayList<>();
    private Map<Integer, Processor> processors = new ConcurrentHashMap<>();
    private GeneratorState generatorState;
    private Database database;
    private ZonedDateTime dateNow = ZonedDateTime.now();
    private final int[] frequencies;

    Map<Long, TimeSales> tradeCount = new ConcurrentHashMap<>();
    TimeSales timeSales = new TimeSales();
    long counter_time=0;
    long counter_id=0;


    public Database getDatabase() {
        if (database == null)
            database = appController.createDatabase("matel", Global.PORT, "matel");
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }


    public Generator(ContractBasic contract, boolean random) {
        this.contract = contract;
        timeSales.setIdcontract(contract.getIdcontract());
        generatorState = new GeneratorState(contract.getIdcontract(), random, 3000);
        frequencies = new int[]{0, 1, 5, 15, 60, 240, 480, 1380, 6900, 35000, 100000, 300000};
        //frequencies = new int[]{1380, 300000};

    }

    public void initDatabase() {
        database = appController.createDatabase("matel", Global.PORT, "matel");
    }

    public void connectMarketData() throws ExecutionException, InterruptedException {
        Random rand = new Random();
        long idcontract = contract.getCloneid() < 0 ? contract.getIdcontract() : contract.getIdcontract() - 1000;
        // contract = contractRepo.findByIdcontract(contract.getIdcontract());
//        contractController.initContracts(false);
        if (this.generatorState.getMarketDataStatus() > 0 && Global.ONLINE)
            disconnectMarketData(false);

//        Double close = database.findTopCloseFromTickByIdContractOrderByTimeStampDesc(idcontract);
//        if (close == null)
//            close = database.findTopCloseFromCandleByIdContractOrderByTimeStampDesc(idcontract);

        if (Global.RANDOM) {
            generatorState.setRandomGenerator(true);
        }

        this.generatorState.setMarketDataStatus(2);


        Thread t = new Thread(() -> {
//            contract.setTickSize(0.2);
            if (Global.RANDOM) {
                while (generatorState.isRandomGenerator()) {
                    double price = 0;
                    if (Math.random() > 0.50) {
                        price = Utils.round(generatorState.getLastPrice() + contract.getTickSize(), contract.getRounding());
                        generatorState.setBid(generatorState.getLastPrice() );
                        timeSales.setBid(generatorState.getLastPrice());
                        generatorState.setAsk(price);
                        timeSales.setAsk(price);
                    } else {
                        price = Utils.round(generatorState.getLastPrice() - contract.getTickSize(),contract.getRounding());
                        generatorState.setBid(price);
                        generatorState.setAsk(generatorState.getLastPrice() );
                        timeSales.setBid(price);
                        timeSales.setAsk(generatorState.getLastPrice() );
                    }
                  //  price = Utils.round(price, contract.getRounding());
                    runPrice(contract.getIdcontract(), 4, price, null);
                   // int volume = rand.nextInt(10);
                    int volume = 5;
                    runSize(contract.getIdcontract(), 5, volume, price);
                    int volumeTotal = generatorState.getDailyVolume() + volume;
                  //  runSize(contract.getIdcontract(), 8, volumeTotal, price);

                    try {
                       // Thread.sleep(generatorState.getSpeed());
                        Thread.sleep(2000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else if (Global.ONLINE) {
                try {
                    if (contract.getIdcontract() > 1) {
                        dataService.reqMktData(contract, this);
                        if(contract.getIdcontract()==9)
                        dataService.reqTickByTickData(contract, this);
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

//        if (close != null) {
//            if (generatorState.getLastPrice() < 0) {
//                Double price = close;
//                if (price != null) {
//                    generatorState.setLastPrice(price);
//                }
//            }
//        }
    }


    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
        runPrice(tickerId, field, price, attrib);
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        runSize(tickerId, field, size, -1);
    }

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast, String exchange, String specialConditions) {
        // System.out.println(reqId + " " + tickType + " " + time + " " + price + " " + size + " " + exchange + " " + specialConditions);
        price = Utils.round(price, contract.getRounding());
        synchronized (this) {
            if (time != counter_time || timeSales.isNewTrade()) {
                try {
                    if (Math.abs(tradeCount.get(timeSales.getId()).getSize()) > 2) {
                        timeSales.setNewTrade(false);
                        //Date dateLong = new Date(tradeCount.get(counter_time).time);
                        //System.out.println(dateLong + " " + tradeCount.get(counter_time).time);
                        System.out.println("        " + tradeCount.get(timeSales.getId()).toString());
                        timeSalesRepo.save(tradeCount.get(timeSales.getId()));
                    }else{
                        //System.out.println("DISCARD <<<< " + tradeCount.get(timeSales.id).toString());
                    }
                } catch (NullPointerException e) {
                   // counter_time = time;
                   // e.printStackTrace();
                }
                    timeSales.incrementId();
                timeSales.setPrice(price);
                timeSales.setWay(timeSales.getBid(), timeSales.getAsk());
                    if (timeSales.getWay().equals("SELL")) {
                        timeSales.setSize(- size);
                    } else if(timeSales.getWay().equals("BUY")) {
                        timeSales.setSize(size);
                    }else{
                        timeSales.setSize(0);
                    }
                    timeSales.setTime(time);
                    counter_time = time;
                    try {
                        tradeCount.put(timeSales.getId(), (TimeSales) timeSales.clone());
                        timeSales.setStart(false);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                //    System.out.println("New >> " + timeSales.toString());





            } else {
                try {
                    if (timeSales.getWay().equals("SELL")) {
                        tradeCount.get(timeSales.getId()).setSize(tradeCount.get(timeSales.getId()).getSize() - size);
                    } else {
                        tradeCount.get(timeSales.getId()).setSize(tradeCount.get(timeSales.getId()).getSize() + size);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            //    System.out.println("Update >> " + tradeCount.get(timeSales.id).toString());

            }
        }
    }

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttribBidAsk tickAttribBidAsk) {
        if(bidPrice == timeSales.getBid() && askPrice == timeSales.getAsk()){
        }else{
            //synchronized(this) {
               // timeSales.previousBid = timeSales.bid;
                timeSales.setBid(Utils.round(bidPrice,contract.getRounding()));
               // timeSales.previousAsk = timeSales.ask;
                timeSales.setAsk(Utils.round(askPrice,contract.getRounding()));
                timeSales.setNewTrade(true);
             //   System.out.println(bidPrice + " -- " + askPrice);
            //}
        }
    }

    private void runSize(long tickerId, int field, int size, double price) {

        if (field == 0 || field == 69) {
            generatorState.setBidQuantity(size);
        }

        if (field == 3 || field == 70) {
            generatorState.setAskQuantity(size);
        }

        if (field == 5 || field == 71) {
            generatorState.setTickQuantity(size);
            //tickByTickAllLast(1, 0, Instant.now().getEpochSecond(), price, size, null, "", "");
        }

        if (field == 8 || field == 74) {
            generatorState.setDailyVolume((int) size);
//            if (flowLive.size() > 0) {
//                flowLive.get(0).setVolume(size - generatorState.getPreviousVolume());
//                generatorState.setPreviousVolume(size);
//            }
        }

//        if (field != 8 && field != 74 && field != 5 && field != 71 && field != 3 && field != 70 && field != 0 && field != 69)
//            System.out.println("Size: " + field + " " + size);

    }

    private void updateGeneratorState(Tick tick) {
        // this.generatorState.setMarketDataStatus(1);
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
                Tick tick = new Tick(global.getIdTick(true), contract.getIdcontract(), OffsetDateTime.ofInstant(Instant.now(), Global.ZONE_ID), newPrice);
               // System.out.println(">>>>>> " + tick.toString());
                processPrice(tick, true, false, false, false, frequencies);
                if (flowLive.size() > 2) {
                    if (!flowLive.get(2).isDiscarded()
                            && ((flowLive.get(2).getTriggerDown() == 1 && flowLive.get(2).getTriggerUp() == 0 && flowLive.get(1).getTriggerDown() == 0 && flowLive.get(1).getTriggerUp() == 1
                            && flowLive.get(0).getTriggerDown() == 1 && flowLive.get(0).getTriggerUp() == 0)
                            || (flowLive.get(2).getTriggerDown() == 0 && flowLive.get(2).getTriggerUp() == 1 && flowLive.get(1).getTriggerDown() == 1
                            && flowLive.get(1).getTriggerUp() == 0 && flowLive.get(0).getTriggerDown() == 0 && flowLive.get(0).getTriggerUp() == 1))
                            && flowLive.get(0).getClose() == flowLive.get(2).getClose()) {
                        flowLive.get(2).setDiscarded(true);
                        flowLive.get(1).setDiscarded(true);

                    } else {
                        if (!flowLive.get(2).isDiscarded()) {
                            flowDelayed.add(0, flowLive.get(2));
                           // System.out.println(flowDelayed.get(0));
                            if (flowDelayed.size() > Global.MAX_LENGTH_TICKS) {
                                flowDelayed.remove(Global.MAX_LENGTH_TICKS);
                            }
                            int count = getDatabase().getSaverController().saveBatchTicks(flowDelayed.get(0), false);
                            if (count > 0)
                                appController.getGeneratorsState().forEach((id, state) -> {
                                    generatorStateRepo.save(state);
                                });
                        }
                    }
                }
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
            if (generatorState.isLiveQuote() && Global.hasCompletedLoading && generatorState.getSpeedMultiplier() <= 10) {
                wsController.sendLiveGeneratorState(generatorState);
            }
        }
    }


    public void processPrice(Tick tick, boolean countConsecutiveUpDown, boolean readOnlyTick, boolean readOnlyCandle, boolean readOnlyProcessorState, int[] frequencies) {
        if (Global.hasCompletedLoading && generatorState.getSpeedMultiplier() <= 10)
            wsController.sendLiveGeneratorState(generatorState);
        if(!readOnlyTick) {
            flowLive.add(0, tick);
            if (flowLive.size() > Global.MAX_LENGTH_TICKS)
                flowLive.remove(Global.MAX_LENGTH_TICKS);
        }
        if (countConsecutiveUpDown)
            consecutiveUpDownCounter(tick);
        for (int frequency : frequencies) {
            processors.get(frequency).process(tick.getTimestamp(), tick.getId(), null, null, null, tick.getClose(), tick.getVolume());
        }
//        processors.forEach((freq, processor) -> {
//            processor.process(tick.getTimestamp(), tick.getId(), null, null, null, tick.getClose(), tick.getVolume());
//        });
        updateGeneratorState(tick);
        if (checkpoint && (Global.COMPUTE_DEEP_HISTORICAL || Global.hasCompletedLoading)) {
            synchronized (this) {
                processors.forEach((freq, proc) -> {
                    if (freq > 0) {
                        if (proc.getFlow().size() > 0)
                            proc.getFlow().get(0).setCheckpoint(true);
                        try {
                            ProcessorState state = (ProcessorState) proc.getProcessorState().clone();
                            if(state.getTimestampTick()==null)
                                state.setTimestampTick(checkpoint_stamp);
                            if(state.getTimestampCandle()==null)
                                state.setTimestampCandle(checkpoint_stamp);
                            database.getSaverController().saveBatchProcessorState(state, false);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            checkpoint = false;
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
        if (tick.getClose() > generatorState.getLastPrice()) {
            generatorState.setTriggerUp(generatorState.getTriggerUp() + 1);
            generatorState.setTriggerDown(0);
        } else if (tick.getClose() < generatorState.getLastPrice()) {
            generatorState.setTriggerUp(0);
            generatorState.setTriggerDown(generatorState.getTriggerDown() + 1);
        }
        tick.setTriggerUp(generatorState.getTriggerUp());
        tick.setTriggerDown(generatorState.getTriggerDown());
        if(flowLive.size()>0)
        flowLive.set(0, tick);
    }

    public void disconnectMarketData(boolean save) {
        LOGGER.info("Disconnect data for contract " + contract.getIdcontract());
        if (this.generatorState.getMarketDataStatus() > 0) {
            this.generatorState.setMarketDataStatus(0);
            if (Global.RANDOM)
                generatorState.setRandomGenerator(false);
            if (contract.getIdcontract() > 1) {
                try {
                    this.dataService.cancelMktData(contract, true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (save) {
                    database.getSaverController().saveBatchTicks(true);
                }
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

    public void setContract(ContractBasic contract) {
        this.contract = contract;
    }

    public GeneratorState setGeneratorState() {
        GeneratorState generatorState = generatorStateRepo.findByIdcontract(contract.getIdcontract());
        if (generatorState != null) {
            this.generatorState = generatorState;
        }
        return generatorState;
    }

    @Scheduled(fixedRate = 180000)
    public void clock() {
        try {
            if (OffsetDateTime.now().minusMinutes(1).isAfter(generatorState.getTimestamp())) {
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
        if (Global.COMPUTE_DEEP_HISTORICAL && state.getTimestampTick().until(dateNow, ChronoUnit.DAYS) > 80) {
        } else {
            checkpoint = true;
            checkpoint_stamp = state.getTimestampTick();
        }
    }
}




