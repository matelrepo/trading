//package io.matel.app;
//
//
//import io.matel.app.config.Global;
//import io.matel.app.domain.Candle;
//import io.matel.app.domain.ContractBasic;
//import io.matel.app.domain.EventType;
//import io.matel.app.repo.ProcessorStateRepo;
//import io.matel.app.state.LogProcessorState;
//import io.matel.app.state.ProcessorState;
//import io.matel.app.tools.DoubleStatistics;
//import io.matel.app.tools.Utils;
//import javazoom.jl.player.Player;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.io.FileInputStream;
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.stream.Collector;
//
//public class Processor extends FlowMerger {
//
//    @Autowired
//    ProcessorStateRepo processorStateRepo;
//
//
//    private int offset = 0; // Used for offset candle if frequency >0
////    LogProcessorState logData;
//
//    public Processor(ContractBasic contract, int freq) {
//        super(contract, freq);
//        processorState = new ProcessorState(contract.getIdcontract(), freq);
//        if (freq > 0 && contract.getIdcontract() == 5) {
//            alert = true;
//        }
////        offset = freq > 0 ? 0 : 1;
//    }
//
//    public List<Candle> getFlow() {
//        return flow;
//    }
//
//
//    public void process(ZonedDateTime timestamp, long idTick, Double open, Double high, Double low, double close, boolean isCandleComputed) {
//        merge(timestamp, idTick, open, high, low, close, isCandleComputed);
////        logData = new LogProcessorState(contract.getIdcontract(), freq, offset);
////        logData.idTick = idTick;
////        logData.flowSizeGreaterThan4 = flow.size() > 4;
//
//        if (flow.size() > 4)
//            algorythm(isCandleComputed);
//
//        if (Global.ONLINE || Global.RANDOM)
//            wsController.sendLiveCandle(flow.get(0));
//    }
//
//    public void algorythm(boolean isCandleComputed) {
//
////        logData.smallCandleNoiseRemoval = smallCandleNoiseRemoval;
//
//        if ((isCandleComputed && freq == 1380) || freq == 0)
//            offset = isCandleComputed ? 1 : 0;
//
////        logData.timestamp = flow.get(0).getTimestamp();
//        processorState.setTimestamp(ZonedDateTime.now());
////        logData.isMaxDetect = isMaxDetect();
//
////        logData.low0 = flow.get(0).getLow();
////        logData.high0 = flow.get(0).getHigh();
////        logData.low1 = flow.get(1 - offset).getLow();
////        logData.high1 = flow.get(1 - offset).getHigh();
////        logData.low2 = flow.get(2 - offset).getLow();
////        logData.high2 = flow.get(2 - offset).getHigh();
////        logData.low3 = flow.get(3 - offset).getLow();
////        logData.high3 = flow.get(3 - offset).getHigh();
////        logData.low4 = flow.get(4 - offset).getLow();
////        logData.high4 = flow.get(4 - offset).getHigh();
//
////        logData.isMaxDetect = isMaxDetect();
////        logData.max = processorState.getMax();
////        logData.isHigh2LessThanMax = flow.get(2 - offset).getHigh() < processorState.getMax();
////        logData.maxTrend = flow.get(2 - offset).getHigh() < processorState.getMax();
////        processorState.setMaxTrend(flow.get(2 - offset).getHigh() < processorState.getMax());
//        if (isMaxDetect() && !smallCandleNoiseRemoval) {
////            logData.maxValue = flow.get(2 - offset).getHigh();
////            logData.maxValid = flow.get(1 - offset).getLow();
////            logData.isEventTypeMaxDetect = true;
//            processorState.setMaxValue(flow.get(2 - offset).getHigh());
//            processorState.setMaxValid(flow.get(1 - offset).getLow());
//            processorState.setColor(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()));
//            recordEvent(EventType.MAX_DETECT);
//        }
//
////        logData.low0LessThanMaxValid = flow.get(0).getLow() < processorState.getMaxValid();
//        if (processorState.getActiveEvents().get(EventType.MAX_DETECT) && flow.get(0).getLow() < processorState.getMaxValid()) {
////            logData.colorGreaterThanMinus1 = processorState.getColor() > -1;
//            if (processorState.getColor() > -1) {
//                processorState.setMax(processorState.getMaxValue());
////                logData.max = processorState.getMaxValue();
//            }
////            logData.maxTrend = processorState.isMaxTrend();
////            logData.minTrend = processorState.isMinTrend();
////            logData.getColorMax = this.getColorMax(processorState.isMaxTrend(), processorState.isMinTrend());
//            processorState.setColor(this.getColorMax(processorState.isMaxTrend(), processorState.isMinTrend()));
//            recordEvent(EventType.MAX_CONFIRM);
////            logData.isEventTypeMaxConfirm = true;
//        }
//
////        logData.isEventTypeMaxDetect = processorState.getActiveEvents().get(EventType.MAX_DETECT);
////        logData.high0GreaterThanMaxValue = flow.get(0).getHigh() > processorState.getMaxValue();
//        if (processorState.getActiveEvents().get(EventType.MAX_DETECT) && flow.get(0).getHigh() > processorState.getMaxValue()) {
//            recordEvent(EventType.MAX_DETECT_CANCEL);
////            logData.isEventTypeMaxDetectCancel = true;
//        }
//
////        logData.isMinDetect = isMinDetect();
////        logData.isLow2GreatherThanMin = flow.get(2 - offset).getLow() > processorState.getMin();
////        logData.min = processorState.getMin();
////        logData.minTrend = flow.get(2 - offset).getLow() > processorState.getMin();
//        processorState.setMinTrend(flow.get(2 - offset).getLow() > processorState.getMin());
//        if (isMinDetect() && !smallCandleNoiseRemoval) {
////            logData.minValue = flow.get(2 - offset).getLow();
////            logData.minValid = flow.get(1 - offset).getHigh();
////            logData.isEventTypeMinDetect = true;
//
//
//            processorState.setMinValue(flow.get(2 - offset).getLow());
//            processorState.setMinValid(flow.get(1 - offset).getHigh());
////            processorState.setColor(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()));
//            recordEvent(EventType.MIN_DETECT);
//        }
//
////        logData.isEventTypeMinDetect = processorState.getActiveEvents().get(EventType.MIN_DETECT);
////        logData.high0GreatherThanMinValid = flow.get(0).getHigh() > processorState.getMinValid();
//        if (processorState.getActiveEvents().get(EventType.MIN_DETECT) && flow.get(0).getHigh() > processorState.getMinValid()) {
////            logData.colorLessThan1 = processorState.getColor() < 1;
//            if (processorState.getColor() < 1) {
//                processorState.setMin(processorState.getMinValue());
////                logData.min = processorState.getMinValue();
//            }
////            logData.getColorMin = this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend());
////            logData.maxTrend = processorState.isMaxTrend();
////            logData.minTrend = processorState.isMinTrend();
//            processorState.setColor(this.getColorMin(processorState.isMaxTrend(), processorState.isMinTrend()));
//            recordEvent(EventType.MIN_CONFIRM);
////            logData.isEventTypeMinConfirm = true;
//        }
//
////        logData.isEventTypeMinDetect = processorState.getActiveEvents().get(EventType.MIN_DETECT);
////        logData.low0LessThanMinValue = flow.get(0).getLow() < processorState.getMinValue();
//        if (processorState.getActiveEvents().get(EventType.MIN_DETECT) && flow.get(0).getLow() < processorState.getMinValue()) {
//            recordEvent(EventType.MIN_DETECT_CANCEL);
////            logData.isEventTypeMinDetectCancel = true;
//        }
//
//        flow.get(0).setCloseAverage(flow.stream().mapToDouble(x -> x.getClose()).summaryStatistics().getAverage());
////        logData.closeAverage = flow.get(0).getCloseAverage();
//        flow.get(0).setAbnormalHeightLevel(flow.stream().map(x -> (x.getHigh() - x.getLow()))
//                .collect(Collector.of(
//                        DoubleStatistics::new,
//                        DoubleStatistics::accept,
//                        DoubleStatistics::combine,
//                        d -> d.getAverage() + 2 * d.getStandardDeviation()
//                )));
//
////        logData.high0MinusLow0 = flow.get(0).getHigh() - flow.get(0).getLow();
////        logData.abnormalHeightLevel = flow.get(0).getAbnormalHeightLevel();
//        if ((flow.get(0).getHigh() - flow.get(0).getLow()) > flow.get(0).getAbnormalHeightLevel()) {
//            flow.get(0).setBigCandle(true);
//        } else {
//            flow.get(0).setBigCandle(false);
//        }
//
//        flow.get(0).setColor(processorState.getColor());
////        logData.color0 = processorState.getColor();
//
////        if(!Global.COMPUTE_DEEP_HISTORICAL || freq >=240)
////        if (isCandleComputed || (flow.get(0).getClose() == flow.get(0).getHigh() || flow.get(0).getClose() == flow.get(0).getLow()))
////            appController.getGenerators().get(contract.getIdcontract())
////                    .getDatabase().getSaverController().saveBatchLogProcessor(logData);
//
//    }
//
//    private int getColorMax(boolean maxTrend, boolean minTrend) {
//        if (maxTrend && !minTrend) {
//            if (flow.get(1).getColor() <= -1)
//                return -2;
//            return -1;
//        } else if (!maxTrend && minTrend) {
//            return 0;
//        } else if (maxTrend && minTrend) {
//            if (flow.get(1).getColor() <= -1)
//                return -2;
//            return -1;
//        } else {
//            if (flow.get(1).getColor() <= -1)
//                return -2;
//            return -1;
//        }
//    }
//
//    private int getColorMin(boolean maxTrend, boolean minTrend) {
//        if (maxTrend && !minTrend) {
//            return 0;
//        } else if (!maxTrend && minTrend) {
//            if (flow.get(1).getColor() >= 1) {
//                return 2;
//            } else {
//                return 1;
//            }
//        } else if (maxTrend && minTrend) {
//            if (flow.get(1).getColor() >= 1) {
//                return 2;
//            } else {
//                return 1;
//            }
//        } else {
//            if (flow.get(1).getColor() >= 1) {
//                return 2;
//            } else {
//                return 1;
//            }
//        }
//    }
//
//    private void recordEvent(EventType type) {
//        processorState.setType(type);
//        if (freq > 0 && !Global.READ_ONLY_CANDLES && Global.hasCompletedLoading)
//            processorStateRepo.save(processorState);
//
//        if (alert) {
//            System.out.println("New event -> " + type.toString() + " " + processorState.toString());
//            try {
//                FileInputStream fileInputStream1 = new FileInputStream("src/main/resources/audio/" + freq +
//                        type.toString().substring(0, 3) + type.toString().substring(3) + ".mp3");
//                Player player1 = new Player((fileInputStream1));
//
//
//                player1.play();
//                System.out.println("Song is playing");
//                if (processorState.getColor() != 0) {
//                    FileInputStream fileInputStream2 = new FileInputStream("src/main/resources/audio/" + processorState.getColor() + ".mp3");
//                    Player player2 = new Player((fileInputStream2));
//                    player2.play();
//                    System.out.println("Song is playing");
//                }
//
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//        }
//    }
//
//    public void saveProcessorState() {
//        if (!Global.READ_ONLY_CANDLES)
//            processorStateRepo.save(processorState);
//    }
//
//    private boolean isMaxDetect() {
////        logData.isNewCandle0 = flow.get(0).isNewCandle();
////        logData.isHigh1LessThanOrEqualHigh2 = flow.get(1 - offset).getHigh() <= flow.get(2 - offset).getHigh();
////        logData.isHigh3LessThanOrEqualHigh2 = flow.get(3 - offset).getHigh() <= flow.get(2 - offset).getHigh();
////        logData.isHigh4LessThanOrEqualHigh2 = flow.get(4 - offset).getHigh() <= flow.get(2 - offset).getHigh();
//        return flow.get(0).isNewCandle() && flow.get(1 - offset).getHigh() <= flow.get(2 - offset).getHigh()
//                && flow.get(3 - offset).getHigh() <= flow.get(2 - offset).getHigh() && flow.get(4 - offset).getHigh() <= flow.get(2 - offset).getHigh();
//    }
//
//    private boolean isMinDetect() {
////        logData.isLow1GreaterThanOrEqualLow2 = flow.get(1 - offset).getLow() >= flow.get(2 - offset).getLow();
////        logData.isLow3GreaterThanOrEqualLow2 = flow.get(3 - offset).getLow() >= flow.get(2 - offset).getLow();
////        logData.isLow4GreaterThanOrEqualLow2 = flow.get(4 - offset).getLow() >= flow.get(2 - offset).getLow();
//        return flow.get(0).isNewCandle() && flow.get(1 - offset).getLow() >= flow.get(2 - offset).getLow()
//                && flow.get(3 - offset).getLow() >= flow.get(2 - offset).getLow() && flow.get(4 - offset).getLow() >= flow.get(2 - offset).getLow();
//    }
//
//    public void setProcessorState(ProcessorState processorState) {
//        if (processorState != null) {
//            this.processorState = processorState;
//            lastDayOfQuarter = processorState.getLastDayOfQuarter();
//        }
//    }
//
//    public ProcessorState getProcessorState() {
//        return processorState;
//    }
//}
