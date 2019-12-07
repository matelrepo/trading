package io.matel.app;


import com.ib.client.TickAttrib;
import io.matel.app.Ibconfig.DataService;
import io.matel.app.Ibconfig.IbClient;
import io.matel.app.config.Global;
import io.matel.app.controller.WsController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.repo.GeneratorStateRepo;
import io.matel.app.state.GeneratorState;
import io.matel.app.tools.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class Generator implements IbClient {
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


    private ContractBasic contract;
    private List<Tick> flowLive = new ArrayList<>();
//    private List<Tick> flowDelayed = new ArrayList<>();
    private Map<Integer, Processor> processors = new ConcurrentHashMap<>();
    private GeneratorState generatorState;
    private Database database;

    public Database getDatabase(){
        return database;
    }

    public void setDatabase(Database database){
        this.database = database;
    }


    public Generator(ContractBasic contract, boolean random) {
        this.contract = contract;
        generatorState = new GeneratorState(contract.getIdcontract(), random, 3000);

    }

    public void initDatabase(){
//        LOGGER.info("init. database for contract " + contract.getIdcontract());
        database = appController.createDatabase("matel", "5432", "matel");
    }

    public void connectMarketData() throws ExecutionException, InterruptedException {

        Random rand = new Random();
//        if (this.generatorState.isConnected())
//            disconnectMarketData(false);

        Double close = database.findTopCloseByIdContractOrderByTimeStampDesc(contract.getIdcontract());

        if (Global.RANDOM) {
            generatorState.setRandomGenerator(true);
//            Tick tick = tickRepository.findTopByIdcontractOrderByTimestampDesc(contract.getIdcontract());
            if (close != null) {
//            LOGGER.info("Connecting market data contract " + contract.getIdcontract() + " -> " + close);
                generatorState.setLastPrice(close);
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
//                    generatorState.setAsk(Utils.round(price,contract.getRounding()));
//                    generatorState.setBid(Utils.round(price,contract.getRounding()));
                    runPrice(contract.getIdcontract(), 4, price, null);
                    int volume = rand.nextInt(1000);
                    runSize(contract.getIdcontract(), 5, volume);
                    int volumeTotal = generatorState.getDailyVolume() + volume;
                    runSize(contract.getIdcontract(), 8, volumeTotal);

                    try {
                        Thread.sleep(generatorState.getSpeed());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else if(Global.ONLINE) {
                try {
                    dataService.reqMktData(contract, this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.setName(contract.getSymbol() + "." + contract.getCurrency() + "." + contract.getIdcontract());
        t.start();

//        Tick tick = tickRepository.findTopByIdcontractOrderByTimestampDesc(contract.getIdcontract());
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

        if(field == 0 || field == 69){
            generatorState.setBidQuantity(size);
        }

        if(field ==3 || field == 70){
            generatorState.setAskQuantity(size);
        }

            if(field ==5 || field == 71){
                generatorState.setTickQuantity(size);
            }

        if(field ==8 || field == 74){
            generatorState.setDailyVolume((int) size);
            if(flowLive.size()>0) {
                flowLive.get(0).setVolume(size - generatorState.getPreviousVolume());
                generatorState.setPreviousVolume(size);
            }
        }

        if(field != 8 && field != 74 && field != 5 && field != 71 && field != 3 && field != 70 && field != 0 && field != 69)
            System.out.println("Size: " + field + " " + size);

    }

    private void runPrice(long tickerId, int field, double price, TickAttrib attrib) {
        generatorState.setTimestamp(ZonedDateTime.now());

        if ((price > 0 && (field == 4 || field == 68) && contract.getFlowType().equals("TRADES"))
        || ((field == 1 || field == 2) && contract.getFlowType().equals("MID"))) {
            double newPrice = reformatPrice(price, field);
            if (newPrice > 0 && newPrice != generatorState.getLastPrice()) {
                generatorState.setColor(newPrice > generatorState.getLastPrice() ? 1 : -1);
                Tick tick = new Tick(contract.getIdcontract(), generatorState.getTimestamp(), newPrice);
//                System.out.println(tick.toString());
//                tick.setSpeed(generatorState.getSpeed());
                tick.setId(global.getIdTick(true));
                generatorState.setIdtick(tick.getId());

                if (price > generatorState.getHigh())
                    generatorState.setHigh(Utils.round(price, contract.getRounding()));

                if (price < generatorState.getLow())
                    generatorState.setLow(Utils.round(price, contract.getRounding()));

                processPrice(tick, true);
                savingTick();
                wsController.sendLiveGeneratorState(generatorState);
            }
        }else if((price > 0 && (field == 1 || field == 2) && contract.getFlowType().equals("TRADES"))){
            switch (field){
                case 1:
                    generatorState.setBid(price);
                    break;
                case 2:
                    generatorState.setAsk(price);
                    break;
            }
        }
    }

    public void process(Candle candle) throws InterruptedException {
        processors.forEach((freq, processor)->{
            processor.process(candle.getTimestamp(), candle.getId(), candle.getOpen(),
                    candle.getHigh(), candle.getLow(), candle.getClose(), true);
        });
        Thread.sleep(1000);
        System.out.println(candle.toString());
    }

    public void processPrice(Tick tick, boolean countConsecutiveUpDown)  {
        flowLive.add(0, tick);
        if (flowLive.size() > Global.MAX_LENGTH_TICKS)
            flowLive.remove(Global.MAX_LENGTH_TICKS);
        if (countConsecutiveUpDown)
            consecutiveUpDownCounter(tick);
        processors.forEach((freq, processor) -> {
            processor.process(tick.getTimestamp(), tick.getId(), null, null, null, tick.getClose(), false);
        });
        generatorState.setLastPrice(tick.getClose());
        generatorState.setChangeValue(Utils.round(generatorState.getLastPrice() - generatorState.getDailyMark(), contract.getRounding()));
        generatorState.setChangePerc(generatorState.getLastPrice() / generatorState.getDailyMark()-1);

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println(tick.toString());
    }


    private double reformatPrice(double price, int field) {
        double newPrice = -1;
        if (contract.getFlowType().equals("TRADES")) {
            newPrice = Utils.round(price, contract.getRounding());
        } else if (contract.getFlowType().equals("MID")) {

            if(field == 66 || field == 1){  //bid
                generatorState.setBid(price);
                if (generatorState.getAsk() < 0)
                    generatorState.setAsk(price);
            }else if (field == 67 || field == 2){ //ask
                generatorState.setAsk(price);
                if (generatorState.getBid() < 0)
                    generatorState.setBid(price);
            }

            if (contract.getFusion() == 1) {
                double bid = field == 4 ? 2 : 0;
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

    private void savingTick() {
//        if (flowLive.size() > 2) {
//            if (!flowLive.get(2).isDiscarded()
//                    && ((flowLive.get(2).getTriggerDown() == 1 && flowLive.get(2).getTriggerUp() == 0 && flowLive.get(1).getTriggerDown() == 0
//                    && flowLive.get(1).getTriggerUp() == 1 && flowLive.get(0).getTriggerDown() == 1 && flowLive.get(0).getTriggerUp() == 0)
//                    || (flowLive.get(2).getTriggerDown() == 0 && flowLive.get(2).getTriggerUp() == 1 && flowLive.get(1).getTriggerDown() == 1
//                    && flowLive.get(1).getTriggerUp() == 0 && flowLive.get(0).getTriggerDown() == 0 && flowLive.get(0).getTriggerUp() == 1))) {
//                flowLive.get(2).setDiscarded(true);
//                flowLive.get(1).setDiscarded(true);
//            } else {
//                if (!flowLive.get(2).isDiscarded()) {
//                    flowDelayed.add(0, flowLive.get(2));
////                    saverController.saveBatchTicks(flowDelayed.get(0));
//                    if (flowDelayed.size() > Global.MAX_LENGTH_TICKS)
//                        flowDelayed.remove(Global.MAX_LENGTH_TICKS);
//                }
//            }
//        }
        int count = database.getSaverController().saveBatchTicks(flowLive.get(0));
        if (count > 0 )
            appController.getGeneratorsState().forEach((id, state)->{
                generatorStateRepo.save(state);
        });
    }

//    public void disconnectMarketData(boolean save) {
//        LOGGER.info("Disconnecting market data for contract " + contract.getIdcontract());
//        if (this.generatorState.isConnected()) {
//            this.generatorState.setConnected(false);
//                generatorState.setRandomGenerator(false);
//            if(Global.ONLINE){
//                dataService.cancelMktData(contract, true);
//            }
//            if (save) {
//                saverController.saveBatchTicks();
//                saverController.saveBatchCandles();
//            }
//
//        }
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

//    public GeneratorStateRepo getGeneratorStateRepo() {
//        return generatorStateRepo;
//    }

    public void setGeneratorState() {
        GeneratorState generatorState = generatorStateRepo.findByIdcontract(contract.getIdcontract());
        if(generatorState!=null) {
            generatorState.setConnected(false);
            generatorState.setConnected(false);
                this.generatorState = generatorState;
        }
    }
}


