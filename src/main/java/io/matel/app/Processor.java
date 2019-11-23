package io.matel.app;


import io.matel.app.domain.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

public class Processor extends FlowMerger {

    @Autowired
    ProcessorDataRepository processorDataRepository;

    private ProcessorData processorData;
    private int offset; // Used for offset candle if frequency >0

    public Processor(ContractBasic contract, int freq) {
        super(contract, freq);
        processorData = new ProcessorData(contract.getIdcontract(), freq);
    }

    public List<Candle> getFlow(){
        return flow;
    }


    public void process(Tick tick) {
        merge(tick);
        if (flow.size() > 4)
            algorythm();
    }

    public void algorythm() {
        processorData.setTimestamp(ZonedDateTime.now());
        if (isMaxDetect()) {
            processorData.setMaxValue(flow.get(2 - offset).getHigh());
            processorData.setMaxTrend(flow.get(2 - offset).getHigh() < processorData.getMax());
            processorData.setMaxValid(flow.get(1 - offset).getLow());
            recordEvent(EventType.MAX_DETECT);
        }

        if (processorData.getActiveEvents().get(EventType.MAX_DETECT) && flow.get(0).getLow() < processorData.getMaxValid()) {
            if (processorData.getColor() > -1)
                processorData.setMax(processorData.getMaxValue());
            processorData.setColor(this.getColorMax(processorData.isMaxTrend(), processorData.isMinTrend()));
            recordEvent(EventType.MAX_CONFIRM);
        }

        if (processorData.getActiveEvents().get(EventType.MAX_DETECT) && flow.get(0).getHigh() > processorData.getMaxValue()) {
            recordEvent(EventType.MAX_DETECT_CANCEL);
        }

        if (isMinDetect()) {
            processorData.setMinValue(flow.get(2 - offset).getLow());
            processorData.setMinTrend(flow.get(2 - offset).getLow() > processorData.getMin());
            processorData.setMinValid(flow.get(1 - offset).getHigh());
            recordEvent(EventType.MIN_DETECT);
        }

        if (processorData.getActiveEvents().get(EventType.MIN_DETECT) && flow.get(0).getHigh() > processorData.getMinValid()) {
            if (processorData.getColor() < 1)
                processorData.setMin(processorData.getMinValue());
            processorData.setColor(this.getColorMin(processorData.isMaxTrend(), processorData.isMinTrend()));
            recordEvent(EventType.MIN_CONFIRM);
        }

        if (processorData.getActiveEvents().get(EventType.MIN_DETECT) && flow.get(0).getLow() < processorData.getMinValue()) {
            recordEvent(EventType.MIN_DETECT_CANCEL);
        }
        flow.get(0).setColor(processorData.getColor());
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

    private void recordEvent(EventType type) {
        processorData.setType(type);
//        System.out.println("Saving new event");
        if(freq>0)
        processorDataRepository.save(processorData);
    }

    private boolean isMaxDetect() {
        return flow.get(0).isNewCandle() && flow.get(1 - offset).getHigh() < flow.get(2 - offset).getHigh()
                && flow.get(3 - offset).getHigh() <= flow.get(2 - offset).getHigh() && flow.get(4 - offset).getHigh() <= flow.get(2 - offset).getHigh();
    }

    private boolean isMinDetect() {
        return flow.get(0).isNewCandle() && flow.get(1 - offset).getLow() > flow.get(2 - offset).getLow()
                && flow.get(3 - offset).getLow() >= flow.get(2 - offset).getLow() && flow.get(4 - offset).getLow() >= flow.get(2 - offset).getLow();
    }

    public void setProcessorData(ProcessorData processorData){
        if(processorData!= null)
        this.processorData = processorData;
    }
}
