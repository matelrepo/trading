package io.matel.trader;


import com.ib.client.TickAttrib;
import io.matel.common.Global;
import io.matel.trader.domain.Candle;
import io.matel.trader.domain.ContractBasic;
import io.matel.trader.domain.Tick;
import io.matel.trader.repository.TickRepository;
import io.matel.trader.tools.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Generator implements Listener, IBClient {

    @Autowired
    Global global;

    @Autowired
    TickRepository tickRepository;

    List<Tick> ticksBuffer = new ArrayList<>();
    private List<Tick> flowLive = new ArrayList<>();
    private List<Tick> flowDelayed = new ArrayList<>();
    private long speed;
    private ContractBasic contract;
    private double lastPrice = -1;
    private double high = 0, low = Double.MAX_VALUE;
    private double bid = -1, ask = -1;
    private double price = 3000;
    private boolean generateRandom = true;

    public Generator(ContractBasic contract, boolean randomGen) {
        this.contract = contract;
        speed = 100;
    }


    @Override
    public void reconnectMktData() {
//        dataService.connectLive(contract, this);
    }



    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
        run(tickerId, field, price, attrib);
    }

    @Override
    public void process(Candle tick) {
//        tick.setSpeed(speed);
////		if (historicalFlow) {
////			processors.get(tick.getFreq()).process(tick, historicalFlow);
////		} else {
//        processors.forEach((freq, flow) -> {
//            flow.process(tick);
//        });
////		}

    }

    @Override
    public void end() {
//        processors.forEach((freq, flow) -> {
//            flow.end();
//        });
//        dataBase = appController.getDatabase();
    }

//    public Processor createProcessor(int freq) {
//        Processor processor = appController.getBeanFactory().createBeanProcessor(contract, freq, this.getDatabase());
//        processors.put(freq, processor);
//        return processor;
//    }

    private void run(long tickerId, int field, double price, TickAttrib attrib) {
        System.out.println(price);
        double newPrice = -1;
        if (price > 0) {
            if (contract.getFlowType() == FlowType.TRADES && (field == 4 || field == 68)) {
                newPrice = Global.round(price, contract.getRounding());
            } else if (contract.getFlowType() == FlowType.MID) {
                if (field == 1) {
                    if (this.ask < 0)
                        this.ask = price;

                    this.bid = price;
                    if (contract.getFusion() == 2) {
                        newPrice = Global.round((this.ask + this.bid) * 5 * Math.pow(10, (double) (contract.getRounding() - 2)) / 2, 0);
                        newPrice = Global.round(newPrice / (5 * Math.pow(10, (double) (contract.getRounding() - 2))), contract.getRounding());
                    } else if (contract.getFusion() == 1) {
                        newPrice = Global.round((this.ask + this.bid) / 2, contract.getRounding() - 1);
                    } else {
                        newPrice = Global.round((this.ask + this.bid) / 2, contract.getRounding());
                    }
                } else if (field == 2) {
                    if (this.bid < 0)
                        this.bid = price;

                    this.ask = price;
                    if (contract.getFusion() == 2) {
                        newPrice = Global.round((this.bid + price) * 5 * Math.pow(10, (double) (contract.getRounding() - 2)) / 2, 0);
                        newPrice = Global.round(newPrice / (5 * Math.pow(10, (double) (contract.getRounding() - 2))), contract.getRounding());
                    } else if (contract.getFusion() == 1) {
                        newPrice = Global.round((this.ask + this.bid) / 2, contract.getRounding() - 1);
                    } else {
                        newPrice = Global.round((this.ask + this.bid) / 2, contract.getRounding());
                    }
                }
            }

            if (newPrice > 0 && newPrice != lastPrice) {
                Date date = new Date();

                if (newPrice > high) {
                    high = newPrice;
                }
                if (newPrice < low) {
                    low = newPrice;
                }
                Tick candle = new Tick(contract.getIdcontract(), date.getTime(), newPrice);
                candle.setId(global.getIdTick(true));
                flowLive.add(0, candle);

                if (flowLive.size() > Global.MAX_TICKS_SIZE) {
                    flowLive.remove(Global.MAX_TICKS_SIZE);
                }

                try {
                    int idup = 0;
                    int iddown = 0;
                    while (flowLive.get(idup).getClose() > flowLive.get(idup + 1).getClose()) {
                        idup++;
                    }

                    while (flowLive.get(iddown).getClose() < flowLive.get(iddown + 1).getClose()) {
                        iddown++;
                    }
                    candle.setTriggerUp(idup);
                    candle.setTriggerDown(iddown);

                    flowLive.set(0, candle);

                } catch (IndexOutOfBoundsException e) {

                }

                // if (Global.getInstance().isOnline) {
//                process(candle);
                // }

                if (flowLive.size() > 2) {
                    if (!flowLive.get(2).isDiscarded()
                            && ((flowLive.get(2).getTriggerDown() == 1 && flowLive.get(2).getTriggerUp() == 0 && flowLive.get(1).getTriggerDown() == 0
                            && flowLive.get(1).getTriggerUp() == 1 && flowLive.get(0).getTriggerDown() == 1 && flowLive.get(0).getTriggerUp() == 0)
                            || (flowLive.get(2).getTriggerDown() == 0 && flowLive.get(2).getTriggerUp() == 1 && flowLive.get(1).getTriggerDown() == 1
                            && flowLive.get(1).getTriggerUp() == 0 && flowLive.get(0).getTriggerDown() == 0 && flowLive.get(0).getTriggerUp() == 1))) {
//                            && flowLive.get(0).getOpen() == flowLive.get(2).getOpen() && flowLive.get(0).getClose() == flowLive.get(2).getClose()) {
                        flowLive.get(2).setDiscarded(true);
                        flowLive.get(1).setDiscarded(true);

                    } else {
                        if (!flowLive.get(2).isDiscarded()) {
                            flowDelayed.add(0, flowLive.get(2));
                            this.saveBatchTicks(flowDelayed.get(0));
                            if (flowDelayed.size() > Global.MAX_TICKS_SIZE) {
                                flowDelayed.remove(Global.MAX_TICKS_SIZE);
                            }
                        }
                    }
                }
                high = newPrice;
                low = newPrice;
                lastPrice = newPrice;
            }
        }
    }

    public void saveBatchTicks(Tick tick) {
        this.ticksBuffer.add(tick);
        if(ticksBuffer.size()> Global.MAX_TICKS_SIZE){
            this.tickRepository.saveAll(this.ticksBuffer);
            this.ticksBuffer.clear();
        }
    }

    public void createTicks() {

        new Thread(() -> {
            while (generateRandom) {
                if (Math.random() > 0.25) {
                    price = price - contract.getTickSize();
                } else {
                    price = price + contract.getTickSize();
                }
                run(contract.getIdcontract(), 4, price, null);

                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



//    public Map<Integer, Processor> getProcessors() {
//        return processors;
//    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setLastPrice(double price) {
        this.lastPrice = price;
    }

    public long getIdcontract() {
        return contract.getIdcontract();
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setGenerateRandom(boolean random) {
        this.generateRandom = random;
    }
}
