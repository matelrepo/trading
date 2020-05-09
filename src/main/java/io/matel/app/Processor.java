package io.matel.app;


import io.matel.app.config.Global;
import io.matel.app.config.tools.Utils;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.EventType;
import io.matel.app.state.ProcessorState;
import io.matel.app.config.tools.DoubleStatistics;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class Processor extends FlowMerger {
    private int offset = 0; // Used for offset candle if frequency >0
    private ZonedDateTime dateNow = ZonedDateTime.now();
    private List<ProcessorListener> listeners = new ArrayList<>();


    public Processor(ContractBasic contract, int freq) {
        super(contract, freq);
        processorState = new ProcessorState(contract.getIdcontract(), freq);
    }

    public void process(ZonedDateTime timestamp, long idTick, Double open, Double high, Double low, double close, boolean computing) {
        if (Global.COMPUTE_DEEP_HISTORICAL && freq < 240 && timestamp.until(dateNow, ChronoUnit.DAYS) > 80) {
        } else {
            merge(timestamp, idTick, open, high, low, close, computing);
            if(flow.get(0).isNewCandle())
                processorState.setEvent(EventType.NONE);
            processorState.setOpen(flow.get(0).getOpen());
            processorState.setHigh(flow.get(0).getHigh());
            processorState.setLow(flow.get(0).getLow());
            processorState.setClose(flow.get(0).getClose());
            processorState.setIdTick(flow.get(0).getIdtick());
            processorState.setIdCandle(flow.get(0).getId());
            if (flow.size() > 4) {
                processorState.setTimestamp(timestamp);
                algorythm(computing);
            }

            if(!computing)
            if (Global.ONLINE || Global.RANDOM || Global.HISTO) {
                wsController.sendLiveCandle(flow.get(0));
            }
        }
    }

    public void algorythm(boolean computing) {
        processorState.setTradable(false);
      //  if ((computing && freq == 1380) || freq == 0)
            offset = freq==0 ? 1 : 0;

        processorState.setMaxTrend(flow.get(2 - offset).getHigh() <= processorState.getMax());
        if (isMaxDetect() && !smallCandleNoiseRemoval) {

            processorState.setMaxValue(flow.get(2 - offset).getHigh());
            processorState.setMaxValid(flow.get(1 - offset).getLow());
            processorState.setTradable(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()) < 0 && processorState.getColor() >= 0);
            recordEvent(EventType.MAX_DETECT);
        }

        if (processorState.activeEvents().get(EventType.MAX_DETECT) && flow.get(0).getLow() < processorState.getMaxValid()) {
            if (processorState.getColor() >= -1) {
                processorState.setMax(processorState.getMaxValue());
            }

            processorState.setTradable(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()) < 0 && processorState.getColor() >= 0);
            processorState.setColor(this.getColorMax(processorState.isMaxTrend(), processorState.isMinTrend()));
            recordEvent(EventType.MAX_CONFIRM);
        }


        if (processorState.activeEvents().get(EventType.MAX_DETECT) && flow.get(0).getHigh() > processorState.getMaxValue()) {
            processorState.setTradable(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()) < 0 && processorState.getColor() >= 0);
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

        flow.get(0).setCloseAverage(Utils.round(flow.stream().mapToDouble(x -> x.getClose()).summaryStatistics().getAverage(),contract.getRounding()));
        flow.get(0).setAbnormalHeightLevel(Utils.round(flow.stream().map(x -> (x.getHigh() - x.getLow()))
                .collect(Collector.of(
                        DoubleStatistics::new,
                        DoubleStatistics::accept,
                        DoubleStatistics::combine,
                        d -> d.getAverage() + 2 * d.getStandardDeviation()
                )),contract.getRounding()));


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
            this.processorState.setActiveEvents(processorState.getEvents());
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
        processorState.setType(type);
        if (freq > 0 && (type == EventType.MIN_CONFIRM || type == EventType.MAX_CONFIRM)) {
            processorState.setCheckpoint(true);
            listeners.forEach(listener -> {
                listener.notifyEvent(processorState);
            });
        }
        wsController.sendEvent(processorState, contract);
        processorState.setCheckpoint(false);
    }

    public ProcessorState getProcessorState() {
        return processorState;
    }

    public void addListener(ProcessorListener listener) {
        this.listeners.add(listener);
    }
}
