//package io.matel.trader;
//
//import java.util.List;
//
//public class Processor extends FlowMerger implements Listener {
//
//    private Event event;
//    private int offset; // Used for offset candle if frequency >0
//
//    public Processor(ContractBasic contract, int freq, Database database) {
//        super(contract, freq, database);
//        event = new Event(contract.getIdcontract(), freq);
//    }
//
//    @Override
//    public void end() {
//        // Saving candles & events for this specific tick
//        // Saving or updating?
//        try {
//            manageSavingClosingApplication(true);
////			database.saveCandles(flow.get(0));
////			database.saveCandlesNow(true);
//
//            if (database.reqEventState(event.getIdcontract(), event.getFreq()) != null) {
//                System.out.println("Event exist");
//                database.updateEventState(event);
//            } else {
//                System.out.println("Event doesnt exist");
//                database.saveEventState(event);
//            }
//        } catch (IndexOutOfBoundsException e) {
//        }
////		database.saveBatchEventsNow(true);
//    }
//
//    public void updateFlowCandles(List<Candle> flow) {
//        this.flow = flow;
//        if (flow.size() > 0) {
//            Candle candle = flow.get(0); // last candle
//            lastIdCandleInDatabase = candle.getId();
//            lastPrice = candle.getClose();
//            stampReference = candle.getTimestamp() - candle.getTimestamp() % base;
//            Instant instant = new Timestamp(candle.getTimestamp()).toInstant();
//            previousDate = instant.atZone(ZoneId.of("Europe/London"));
//        }
//    }
//
//    public void updateFlow(Event event) {
//        if (event != null) {
//            // contract, freq
//            this.event.setColor(event.getColor());
//            this.event.setTags(event.getTags());
//            this.event.setMaxTrend(event.getMaxTrend());
//            this.event.setMaxValue(event.getMaxValue());
//            this.event.setMaxValid(event.getMaxValid());
//            this.event.setMinTrend(event.getMaxTrend());
//            this.event.setMinValue(event.getMinValue());
//            this.event.setMinValid(event.getMinValid());
//        }
//    }
//
//    @Override
//    public void process(Candle tick) {
//        event.setIdtick(Global.getInstance().getIdTick(false));
//        merge(tick);
//        if (flow.size() > 4)
//            algorythm();
//    }
//
//    public void algorythm() {
//        event.setTimestamp(flow.get(0).getChildTimeStamp());
////		if (flow.get(0).isNewCandle()) {
////			event.getTags().put(EventType.MIN_ADV, false);
////			event.getTags().put(EventType.MAX_ADV, false);
////		}
////
////		if (isMinAdvDetect()) {
//////			event.setTradeReco(getTradeRecoMin(event.getMaxTrend(), flow.get(1 - offset).getLow() > event.getMin()));
////			recordEvent(EventType.MIN_ADV);
////		}
////
////		if (isMaxAdvDetect()) {
//////			event.setTradeReco(getTradeRecoMax(flow.get(1 - offset).getHigh() < event.getMax(), event.getMinTrend()));
////			recordEvent(EventType.MAX_ADV);
////		}
////
////		if (isMaxAdvDetectCancelled()) {
////			recordEvent(EventType.MAX_ADV_CANCEL);
////		}
////
////		if (isMinAdvDetectCancelled()) {
////			recordEvent(EventType.MIN_ADV_CANCEL);
////		}
//
//        if (isMaxDetect()) {
//            event.setMaxValue(flow.get(2 - offset).getHigh());
//            event.setMaxTrend(flow.get(2 - offset).getHigh() < event.getMax());
//            event.setMaxValid(flow.get(1 - offset).getLow());
//            recordEvent(EventType.MAX_DETECT);
//        }
//
//        if (event.getTags().get(EventType.MAX_DETECT) && flow.get(0).getLow() < event.getMaxValid()) {
//            if (event.getColor() > -1)
//                event.setMax(event.getMaxValue());
//            event.setColor(this.getColorMax(event.getMaxTrend(), event.getMinTrend()));
//            recordEvent(EventType.MAX_CONFIRM);
//        }
//
//        if (event.getTags().get(EventType.MAX_DETECT) && flow.get(0).getHigh() > event.getMaxValue()) {
//            recordEvent(EventType.MAX_DETECT_CANCEL);
//        }
//
//        if (isMinDetect()) {
//            event.setMinValue(flow.get(2 - offset).getLow());
//            event.setMinTrend(flow.get(2 - offset).getLow() > event.getMin());
//            event.setMinValid(flow.get(1 - offset).getHigh());
//            recordEvent(EventType.MIN_DETECT);
//        }
//
//        if (event.getTags().get(EventType.MIN_DETECT) && flow.get(0).getHigh() > event.getMinValid()) {
//            if (event.getColor() < 1)
//                event.setMin(event.getMinValue());
//            event.setColor(this.getColorMin(event.getMaxTrend(), event.getMinTrend()));
//            recordEvent(EventType.MIN_CONFIRM);
//        }
//
//        if (event.getTags().get(EventType.MIN_DETECT) && flow.get(0).getLow() < event.getMinValue()) {
//            recordEvent(EventType.MIN_DETECT_CANCEL);
//        }
//        flow.get(0).setColor(Integer.valueOf(event.getColor()));
//    }
//
//    public int getColorMax(boolean maxTrend, boolean minTrend) {
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
//        } else if (!maxTrend && !minTrend) {
//            if (flow.get(1).getColor() <= -1)
//                return -2;
//            return -1;
//        }
//        return 0;
//    }
//
//    public int getColorMin(boolean maxTrend, boolean minTrend) {
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
//        } else if (!maxTrend && !minTrend) {
//            if (flow.get(1).getColor() >= 1) {
//                return 2;
//            } else {
//                return 1;
//            }
//        }
//
//        return 0;
//    }
//
//    private void recordEvent(EventType type) {
//        event.setType(type);
////		if (freq > 420 && Global.getInstance().isOnline) {
////			webControllerOut.sendEmail(event, contract);
////		}
////		database.saveBatchEvents(event);
////		if (!Global.getInstance().histoFusion)
////			webControllerOut.sendLiveEventByContract(event, contract.getIdcontract(), freq);
//    }
//
//    private boolean isMaxDetect() {
//        return flow.get(0).isNewCandle() && flow.get(1 - offset).getHigh() < flow.get(2 - offset).getHigh()
//                && flow.get(3 - offset).getHigh() <= flow.get(2 - offset).getHigh() && flow.get(4 - offset).getHigh() <= flow.get(2 - offset).getHigh();
//    }
//
//    private boolean isMinDetect() {
//        return flow.get(0).isNewCandle() && flow.get(1 - offset).getLow() > flow.get(2 - offset).getLow()
//                && flow.get(3 - offset).getLow() >= flow.get(2 - offset).getLow() && flow.get(4 - offset).getLow() >= flow.get(2 - offset).getLow();
//    }
//
////	private boolean isMinAdvDetect() {
////
////		return !event.getTags().get(EventType.MIN_ADV) && flow.get(0).getProgress() > 70 && !event.getTags().get(EventType.MIN_DETECT)
////				&& flow.get(0 - offset).getLow() > flow.get(1 - offset).getLow() && flow.get(2 - offset).getLow() >= flow.get(1 - offset).getLow()
////				&& flow.get(3 - offset).getLow() >= flow.get(1 - offset).getLow();
////	}
////
////	private boolean isMinAdvDetectCancelled() {
////		return event.getTags().get(EventType.MIN_ADV) && !(flow.get(0).getProgress() > 70 && flow.get(0 - offset).getLow() > flow.get(1 - offset).getLow()
////				&& flow.get(2 - offset).getLow() >= flow.get(1 - offset).getLow() && flow.get(3 - offset).getLow() >= flow.get(1 - offset).getLow());
////	}
////
////	private boolean isMaxAdvDetect() {
////		return !event.getTags().get(EventType.MAX_ADV) && !event.getTags().get(EventType.MAX_DETECT) && flow.get(0).getProgress() > 70
////				&& flow.get(0 - offset).getHigh() < flow.get(1 - offset).getHigh() && flow.get(2 - offset).getHigh() <= flow.get(1 - offset).getHigh()
////				&& flow.get(3 - offset).getHigh() <= flow.get(1 - offset).getHigh();
////	}
////
////	private boolean isMaxAdvDetectCancelled() {
////		return event.getTags().get(EventType.MAX_ADV) && !(flow.get(0).getProgress() > 70 && flow.get(0 - offset).getHigh() < flow.get(1 - offset).getHigh()
////				&& flow.get(2 - offset).getHigh() <= flow.get(1 - offset).getHigh() && flow.get(3 - offset).getHigh() <= flow.get(1 - offset).getHigh());
////	}
//
//    public Event getEvent() {
//        return event;
//    }
//
//}
