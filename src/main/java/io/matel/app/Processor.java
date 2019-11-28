package io.matel.app;


import io.matel.app.controller.SaverController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.domain.enumtype.EventType;
import io.matel.app.repo.ProcessorDataRepository;
import io.matel.app.state.LogProcessorData;
import io.matel.app.state.ProcessorData;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

public class Processor extends FlowMerger {

    @Autowired
    SaverController saverController;

    @Autowired
    ProcessorDataRepository processorDataRepository;


    private ProcessorData processorData;
    private int offset; // Used for offset candle if frequency >0
    LogProcessorData logData;

    public Processor(ContractBasic contract, int freq) {
        super(contract, freq);
        processorData = new ProcessorData(contract.getIdcontract(), freq);
        offset = freq > 0 ? 0 : 1;
    }

    public List<Candle> getFlow(){
        return flow;
    }


    public void process(Tick tick) {
        merge(tick);
        logData = new LogProcessorData(contract.getIdcontract(), freq, offset);
        logData.idTick = tick.getId();
        logData.flowSizeGreaterThan4 = flow.size() > 4;
        if (flow.size() > 4)
            algorythm();
    }

    public void algorythm() {
        logData.timestamp = ZonedDateTime.now();
        processorData.setTimestamp(ZonedDateTime.now());
        logData.isMaxDetect = isMaxDetect();

        logData.low0 = flow.get(0).getLow();
        logData.high0 = flow.get(0).getHigh();
        logData.low1 = flow.get(1 - offset).getLow();
        logData.high1 = flow.get(1 - offset).getHigh();
        logData.low2 = flow.get(2 - offset).getLow();
        logData.high2 = flow.get(2 - offset).getHigh();
        logData.low3 = flow.get(3 - offset).getLow();
        logData.high3 = flow.get(3 - offset).getHigh();
        logData.low4 = flow.get(4 - offset).getLow();
        logData.high4 = flow.get(4 - offset).getHigh();

        logData.isMaxDetect = isMaxDetect();
        logData.max = processorData.getMax();
        logData.isHigh2LessThanMax = flow.get(2 - offset).getHigh() < processorData.getMax();
        logData.maxTrend = flow.get(2 - offset).getHigh() < processorData.getMax();
        processorData.setMaxTrend(flow.get(2 - offset).getHigh() < processorData.getMax());
        if (isMaxDetect()) {
            logData.maxValue = flow.get(2 - offset).getHigh();
            logData.maxValid = flow.get(1 - offset).getLow();
            logData.isEventTypeMaxDetect = true;

            processorData.setMaxValue(flow.get(2 - offset).getHigh());
            processorData.setMaxValid(flow.get(1 - offset).getLow());
            recordEvent(EventType.MAX_DETECT);
        }

        logData.low0LessThanMaxValid = flow.get(0).getLow() < processorData.getMaxValid();
        if (processorData.getActiveEvents().get(EventType.MAX_DETECT) && flow.get(0).getLow() < processorData.getMaxValid()) {
            logData.colorGreaterThanMinus1 = processorData.getColor() > -1;
            if (processorData.getColor() > -1) {
                processorData.setMax(processorData.getMaxValue());
                logData.max = processorData.getMaxValue();
            }
            logData.maxTrend = processorData.isMaxTrend();
            logData.minTrend = processorData.isMinTrend();
            logData.getColorMax = this.getColorMax(processorData.isMaxTrend(), processorData.isMinTrend());
            processorData.setColor(this.getColorMax(processorData.isMaxTrend(), processorData.isMinTrend()));
            recordEvent(EventType.MAX_CONFIRM);
            logData.isEventTypeMaxConfirm = true;
        }

        logData.isEventTypeMaxDetect = processorData.getActiveEvents().get(EventType.MAX_DETECT);
        logData.high0GreaterThanMaxValue = flow.get(0).getHigh() > processorData.getMaxValue();
        if (processorData.getActiveEvents().get(EventType.MAX_DETECT) && flow.get(0).getHigh() > processorData.getMaxValue()) {
            recordEvent(EventType.MAX_DETECT_CANCEL);
            logData.isEventTypeMaxDetectCancel = true;
        }

        logData.isMinDetect = isMinDetect();
        logData.isLow2GreatherThanMin = flow.get(2 - offset).getLow() > processorData.getMin();
        logData.min = processorData.getMin();
        logData.minTrend = flow.get(2 - offset).getLow() > processorData.getMin();
        processorData.setMinTrend(flow.get(2 - offset).getLow() > processorData.getMin());
        if (isMinDetect()) {
            logData.minValue = flow.get(2 - offset).getLow();
            logData.minValid = flow.get(1 - offset).getHigh();
            logData.isEventTypeMinDetect = true;


            processorData.setMinValue(flow.get(2 - offset).getLow());
            processorData.setMinValid(flow.get(1 - offset).getHigh());
            recordEvent(EventType.MIN_DETECT);
        }

        logData.isEventTypeMinDetect = processorData.getActiveEvents().get(EventType.MIN_DETECT);
        logData.high0GreatherThanMinValid = flow.get(0).getHigh() > processorData.getMinValid();
        if (processorData.getActiveEvents().get(EventType.MIN_DETECT) && flow.get(0).getHigh() > processorData.getMinValid()) {
            logData.colorLessThan1 = processorData.getColor() < 1;
            if (processorData.getColor() < 1) {
                processorData.setMin(processorData.getMinValue());
                logData.min=processorData.getMinValue();
            }
            logData.getColorMin = this.getColorMin(processorData.isMaxTrend(), processorData.isMinTrend());
            logData.maxTrend = processorData.isMaxTrend();
            logData.minTrend = processorData.isMinTrend();
            processorData.setColor(this.getColorMin(processorData.isMaxTrend(), processorData.isMinTrend()));
            recordEvent(EventType.MIN_CONFIRM);
            logData.isEventTypeMinConfirm = true;
        }

        logData.isEventTypeMinDetect = processorData.getActiveEvents().get(EventType.MIN_DETECT);
        logData.low0LessThanMinValue = flow.get(0).getLow() < processorData.getMinValue();
        if (processorData.getActiveEvents().get(EventType.MIN_DETECT) && flow.get(0).getLow() < processorData.getMinValue()) {
            recordEvent(EventType.MIN_DETECT_CANCEL);
            logData.isEventTypeMinDetectCancel = true;
        }
        flow.get(0).setColor(processorData.getColor());
        logData.color0 = processorData.getColor();

        if (flow.get(0).getClose() == flow.get(0).getHigh() || flow.get(0).getClose() == flow.get(0).getLow())
            saverController.saveBatchLogProcessor(logData);
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
//        if(freq>0)
//        processorDataRepository.save(processorData);
    }

    private boolean isMaxDetect() {
        logData.isNewCandle0 = flow.get(0).isNewCandle();
        logData.isHigh1LessThanOrEqualHigh2 = flow.get(1 - offset).getHigh() <= flow.get(2 - offset).getHigh();
        logData.isHigh3LessThanOrEqualHigh2 = flow.get(3 - offset).getHigh() <= flow.get(2 - offset).getHigh();
        logData.isHigh4LessThanOrEqualHigh2 = flow.get(4 - offset).getHigh() <= flow.get(2 - offset).getHigh();
        return flow.get(0).isNewCandle() && flow.get(1 - offset).getHigh() <= flow.get(2 - offset).getHigh()
                && flow.get(3 - offset).getHigh() <= flow.get(2 - offset).getHigh() && flow.get(4 - offset).getHigh() <= flow.get(2 - offset).getHigh();
    }

    private boolean isMinDetect() {
        logData.isNewCandle0 = flow.get(0).isNewCandle();
        logData.isLow1GreaterThanOrEqualLow2 = flow.get(1 - offset).getLow() >= flow.get(2 - offset).getLow();
        logData.isLow3GreaterThanOrEqualLow2 = flow.get(3 - offset).getLow() >= flow.get(2 - offset).getLow();
        logData.isLow4GreaterThanOrEqualLow2 = flow.get(4 - offset).getLow() >= flow.get(2 - offset).getLow();
        return flow.get(0).isNewCandle() && flow.get(1 - offset).getLow() >= flow.get(2 - offset).getLow()
                && flow.get(3 - offset).getLow() >= flow.get(2 - offset).getLow() && flow.get(4 - offset).getLow() >= flow.get(2 - offset).getLow();
    }

    public void setProcessorData(ProcessorData processorData){
        if(processorData!= null)
        this.processorData = processorData;
    }
}
