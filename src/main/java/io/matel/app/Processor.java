package io.matel.app;


import io.matel.app.config.Global;
import io.matel.app.config.tools.Utils;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.EventType;
import io.matel.app.state.ProcessorState;
import io.matel.app.config.tools.DoubleStatistics;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class Processor extends FlowMerger {
    private int offset = 0; // Used for offset candle if frequency >0
    private ZonedDateTime dateNow = ZonedDateTime.now();
    private List<ProcessorListener> listeners = new ArrayList<>();
    private int cpt = 0;


    public Processor(ContractBasic contract, int freq) {
        super(contract, freq);
    }

    public void process(OffsetDateTime timestampTick, long idTick, Double open, Double high, Double low, double close, int volume) {
        if (Global.COMPUTE_DEEP_HISTORICAL && freq < 240 && timestampTick.until(dateNow, ChronoUnit.DAYS) > 365) {
        } else {
            merge(timestampTick, idTick, open, high, low, close, volume);
            if (flow.get(0).isNewCandle())
                processorState.setEventType(EventType.NONE);

            processorState.setTimestampCandle(flow.get(0).getTimestampTick());
            processorState.setTimestampTick(timestampTick);
            processorState.setTimestampTick(timestampTick);
            processorState.setOpen(flow.get(0).getOpen());
            processorState.setHigh(flow.get(0).getHigh());
            processorState.setLow(flow.get(0).getLow());
            processorState.setClose(flow.get(0).getClose());
            processorState.setIdTick(flow.get(0).getIdtick());
            processorState.setIdTick(flow.get(0).getIdtick());
            processorState.setIdCandle(flow.get(0).getId());
            if (flow.size() > 4) {
                algorythm();
            }

//            processorState.setEvent(event);

            if (Global.ONLINE || Global.RANDOM || Global.HISTO) {
                wsController.sendLiveCandle(flow.get(0));
            }
        }
    }

    public void algorythm() {
        processorState.setTradable(false);
        processorState.setCheckpoint(false);
        offset = freq == 0 ? 1 : 0;

        processorState.setMaxTrend(flow.get(2 - offset).getHigh() <= processorState.getMax());
        if (isMaxDetect() && !smallCandleNoiseRemoval) {

            processorState.setMaxValue(flow.get(2 - offset).getHigh());
            processorState.setMaxValid(flow.get(1 - offset).getLow());
            processorState.setTradable(this.getColorMax(processorState.isMaxTrend(), processorState.isMinTrend()) < 0 && processorState.getColor() >= 0);
            recordEvent(EventType.MAX_DETECT);
        }

        if (processorState.activeEvents().get(EventType.MAX_DETECT) && flow.get(0).getLow() < processorState.getMaxValid()) {
            if (processorState.getColor() >= -1) {
                processorState.setMax(processorState.getMaxValue());
            }

            processorState.setTradable(this.getColorMax(processorState.isMaxTrend(), processorState.isMinTrend()) < 0 && processorState.getColor() >= 0);
            processorState.setColor(this.getColorMax(processorState.isMaxTrend(), processorState.isMinTrend()));
            recordEvent(EventType.MAX_CONFIRM);
        }


        if (processorState.activeEvents().get(EventType.MAX_DETECT) && flow.get(0).getHigh() > processorState.getMaxValue()) {
            processorState.setTradable(this.getColorMax(processorState.isMaxTrend(), processorState.isMinTrend()) < 0 && processorState.getColor() >= 0);
            recordEvent(EventType.MAX_DETECT_CANCEL);
        }


        processorState.setMinTrend(flow.get(2 - offset).getLow() >= processorState.getMin());
        if (isMinDetect() && !smallCandleNoiseRemoval) {
            processorState.setMinValue(flow.get(2 - offset).getLow());
            processorState.setMinValid(flow.get(1 - offset).getHigh());
            processorState.setTradable(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()) > 0 && processorState.getColor() <= 0);
            recordEvent(EventType.MIN_DETECT);
        }


        if (processorState.activeEvents().get(EventType.MIN_DETECT) && flow.get(0).getHigh() > processorState.getMinValid()) {
            if (processorState.getColor() <= 1) {
                processorState.setMin(processorState.getMinValue());
            }

            processorState.setTradable(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()) > 0 && processorState.getColor() <= 0);
            processorState.setColor(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()));
            recordEvent(EventType.MIN_CONFIRM);

        }


        if (processorState.activeEvents().get(EventType.MIN_DETECT) && flow.get(0).getLow() < processorState.getMinValue()) {
            processorState.setTradable(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()) > 0 && processorState.getColor() <= 0);
//            if(freq==5)
//                System.out.println("MININUM CANC " + freq);
            recordEvent(EventType.MIN_DETECT_CANCEL);
        }
        double averageClose = Utils.round(flow.stream().mapToDouble(x -> x.getClose()).summaryStatistics().getAverage(), contract.getRounding());
        flow.get(0).setCloseAverage(averageClose);
        processorState.setAverageClose(averageClose);

        double abnormalLevel = Utils.round(flow.stream().map(x -> (x.getHigh() - x.getLow()))
                .collect(Collector.of(
                        DoubleStatistics::new,
                        DoubleStatistics::accept,
                        DoubleStatistics::combine,
                        d -> d.getAverage() + 2 * d.getStandardDeviation()
                )), contract.getRounding());
        flow.get(0).setAbnormalHeightLevel(abnormalLevel);
        processorState.setAbnormalHeight(abnormalLevel);


        if ((flow.get(0).getHigh() - flow.get(0).getLow()) > flow.get(0).getAbnormalHeightLevel()) {
            if (!flow.get(0).isBigCandle()) {
                EventType type = null;
                if (flow.get(0).getClose() > flow.get(0).getOpen()) {
                    type = EventType.BUYERS;
                    processorState.setTradable(processorState.getColor() > 0);
                } else {
                    type = EventType.SELLERS;
                    processorState.setTradable(processorState.getColor() < 0);
                }
                recordEvent(type);
            }
            flow.get(0).setBigCandle(true);
        } else
            flow.get(0).setBigCandle(false);

        flow.get(0).setColor(processorState.getColor());


    }

    public void setProcessorState(ProcessorState processorState) {
        if (processorState != null) {
            this.processorState = processorState;
            this.processorState.setActiveEvents(processorState.getEventsList());
            this.processorState.setActiveEventsTradable(processorState.getEventsTradableList());
            lastDayOfQuarter = processorState.getLastDayOfQuarter();
        }
    }


    public List<Candle> getFlow() {
        return flow;
    }

    private int getColorMax(boolean maxTrend, boolean minTrend) {
        if (maxTrend && !minTrend) {
            if (flow.get(1).getColor() <= -1)
                return -2;
            return -1;
        } else if (!maxTrend && minTrend) {
            return 0;
        } else if (maxTrend && minTrend) {
            if (flow.get(1).getColor() <= -1)
                return -2;
            return -1;
        } else {
            if (flow.get(1).getColor() <= -1)
                return -2;
            return -1;
        }
    }

    private int getColorMin(boolean maxTrend, boolean minTrend) {
        if (maxTrend && !minTrend) {
            return 0;
        } else if (!maxTrend && minTrend) {
            if (flow.get(1).getColor() >= 1) {
                return 2;
            } else {
                return 1;
            }
        } else if (maxTrend && minTrend) {
            if (flow.get(1).getColor() >= 1) {
                return 2;
            } else {
                return 1;
            }
        } else {
            if (flow.get(1).getColor() >= 1) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    private boolean isMaxDetect() {
        return flow.get(0).isNewCandle() && flow.get(1 - offset).getHigh() <= flow.get(2 - offset).getHigh()
                && flow.get(3 - offset).getHigh() < flow.get(2 - offset).getHigh() && flow.get(4 - offset).getHigh() <= flow.get(2 - offset).getHigh();
    }

    private boolean isMinDetect() {
        return flow.get(0).isNewCandle() && flow.get(1 - offset).getLow() >= flow.get(2 - offset).getLow()
                && flow.get(3 - offset).getLow() > flow.get(2 - offset).getLow() && flow.get(4 - offset).getLow() >= flow.get(2 - offset).getLow();
    }

    private void recordEvent(EventType type) {
        processorState.setType(type, processorState.isTradable());

        if((freq ==60 || freq == 240 || freq ==480)) {
            if((
                    appController.getGenerators().get(contract.getIdcontract()).getStates().get(60).getColor()>0
                            &&appController.getGenerators().get(contract.getIdcontract()).getStates().get(240).getColor()>0
                            &&appController.getGenerators().get(contract.getIdcontract()).getStates().get(480).getColor()>0
            ) && ( appController.getGenerators().get(contract.getIdcontract()).getStates().get(1380).getEventsTradableList().contains("MIN_DETECT")
                    || appController.getGenerators().get(contract.getIdcontract()).getStates().get(6900).getEventsTradableList().contains("MIN_DETECT")
                    || appController.getGenerators().get(contract.getIdcontract()).getStates().get(100000).getEventsTradableList().contains("MIN_DETECT"))
        ){
            if(!appController.getGenerators().get(contract.getIdcontract()).isHedgeLongEvent()) {
                appController.getGenerators().get(contract.getIdcontract()).setHedgeLongEvent(true);
                System.out.println(">>>Long only event " + processorState.toString());
                processorState.setEvtype(String.valueOf(EventType.ENTRY_LONG_ONLY));
                try {
                    saverController.saveBatchEvents((ProcessorState) processorState.clone(),false);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }else{
                if(appController.getGenerators().get(contract.getIdcontract()).isHedgeLongEvent()) {
                    System.out.println(">>>Terminated long only event " + processorState.toString());
                    appController.getGenerators().get(contract.getIdcontract()).setHedgeLongEvent(false);
                    processorState.setEvtype(String.valueOf(EventType.EXIT_LONG_ONLY));
                    try {
                        saverController.saveBatchEvents((ProcessorState) processorState.clone(),false);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
        }
            if((
                    appController.getGenerators().get(contract.getIdcontract()).getStates().get(60).getColor()<0
                            &&appController.getGenerators().get(contract.getIdcontract()).getStates().get(240).getColor()<0
                            &&appController.getGenerators().get(contract.getIdcontract()).getStates().get(480).getColor()<0
            ) && ( appController.getGenerators().get(contract.getIdcontract()).getStates().get(1380).getEventsTradableList().contains("MAX_DETECT")
                    || appController.getGenerators().get(contract.getIdcontract()).getStates().get(6900).getEventsTradableList().contains("MAX_DETECT")
                    || appController.getGenerators().get(contract.getIdcontract()).getStates().get(100000).getEventsTradableList().contains("MAX_DETECT"))
            ){
                if(!appController.getGenerators().get(contract.getIdcontract()).isHedgeShortEvent()) {
                    appController.getGenerators().get(contract.getIdcontract()).setHedgeShortEvent(true);
                    System.out.println(">>Short only event " + processorState.toString());
                    processorState.setEvtype(String.valueOf(EventType.ENTRY_SHORT_ONLY));
                    try {
                        saverController.saveBatchEvents((ProcessorState) processorState.clone(),false);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }                }
            }else{
                if(appController.getGenerators().get(contract.getIdcontract()).isHedgeShortEvent()) {
                    System.out.println(">>>Terminated short only event " + processorState.toString());
                    appController.getGenerators().get(contract.getIdcontract()).setHedgeShortEvent(false);
                    processorState.setEvtype(String.valueOf(EventType.EXIT_SHORT_ONLY));
                    try {
                       saverController.saveBatchEvents((ProcessorState) processorState.clone(),false);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }                }
            }
    }

        if (freq > 0 && (type == EventType.MIN_CONFIRM || type == EventType.MAX_CONFIRM)) {
            processorState.setCheckpoint(true);
            listeners.forEach(listener -> {
                try {
                    listener.notifyEvent((ProcessorState) processorState.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            });
        }
        wsController.sendEvent(processorState, contract);
    }

    public ProcessorState getProcessorState() {
        return processorState;
    }

    public void addListener(ProcessorListener listener) {
        this.listeners.add(listener);
    }
}
