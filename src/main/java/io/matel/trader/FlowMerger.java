//package io.matel.trader;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class FlowMerger {
//
//    @Autowired
//    protected WebControllerOut webControllerOut;
//
//    @Autowired
//    protected AppController appController;
//
//    protected List<Candle> flow = new ArrayList<>();
//    protected int freq;
//    protected ContractBasic contract;
//    protected long base;
//
//    protected long stampReference;
//    protected ZonedDateTime previousDate;
//    protected double lastPrice = -1;
//    //	protected Generator generator;
//    protected long lastIdCandleInDatabase;
//    protected Database database;
//
//    protected SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//    public FlowMerger(ContractBasic contract, int freq, Database database) {
//        this.contract = contract;
//        this.database = database;
//        this.freq = freq;
//        base = 60000L * freq <= 0 ? 1L : 60000L * freq; // Special freq = 1380, 6900,35000
//    }
//
//    public void manageSavingClosingApplication(boolean saveNow) {
//        if (flow.size() > 0)
//            if (flow.get(0).getId() == lastIdCandleInDatabase) {
//                database.updateCandle(flow.get(0));
//            } else {
//                database.saveCandles(flow.get(0));
//                if (saveNow)
//                    database.saveCandlesNow(true);
//            }
//    }
//
//    public void newCandle(long timestamp, Candle source) {
//        boolean smallCandleNoiseRemoval = false;
//        if (flow.size() > 2) {
//            smallCandleNoiseRemoval = flow.get(0).getLow() >= flow.get(1).getLow() && flow.get(0).getHigh() <= flow.get(1).getHigh() && freq > 0;
//            if (smallCandleNoiseRemoval)
//                updateCandle(source);
//        }
//        if (!smallCandleNoiseRemoval) {
//            Candle candle = new Candle(timestamp, lastPrice, source.getClose(), contract, freq, timestamp);
//            candle.setId(Global.getInstance().getIdCandle());
//            candle.setIdtick(source.getIdtick());
//            candle.setNewCandle(true);
//            candle.setFreq(freq);
//
//            if (flow.size() > 0) {
//                candle.setColor(flow.get(0).getColor());
//                if (flow.get(0).getId() != lastIdCandleInDatabase)
//                    database.saveCandles(flow.get(0));
//            }
//            flow.add(0, candle);
//            if (flow.size() > Global.MAXLENGTH_FLOW)
//                flow.remove(flow.size() - 1);
//
//            lastPrice = candle.getClose();
//        }
//    }
//
//    public void updateCandle(Candle candle) {
//        flow.get(0).setNewCandle(false);
//        flow.get(0).setIdtick(candle.getIdtick());
//        flow.get(0).setChildTimeStamp(candle.getTimestamp());
//
//        if (candle.getClose() > flow.get(0).getHigh())
//            flow.get(0).setHigh(candle.getClose());
//
//        if (candle.getClose() < flow.get(0).getLow())
//            flow.get(0).setLow(candle.getClose());
//        flow.get(0).setClose(candle.getClose());
//        lastPrice = candle.getClose();
//    }
//
//    protected void merge(Candle tick) {
//        Instant instant = new Timestamp(tick.getTimestamp()).toInstant();
//        ZonedDateTime date = instant.atZone(ZoneId.of("Europe/London"));
//
//        if (freq > 420) {
//            if (previousDate == null)
//                newCandle(tick.getTimestamp(), tick);
//            else
//                switch (freq) {
//                    case 100000:
//                        if (date.getDayOfYear() < previousDate.getDayOfYear())
//                            newCandle(tick.getTimestamp(), tick);
//                        else
//                            updateCandle(tick);
//                        break;
//                    case 35000:
//                        if (date.getDayOfMonth() < previousDate.getDayOfMonth())
//                            newCandle(tick.getTimestamp(), tick);
//                        else
//                            updateCandle(tick);
//                        break;
//                    case 6900:
//                        if (date.getDayOfWeek().getValue() < previousDate.getDayOfWeek().getValue())
//                            newCandle(tick.getTimestamp(), tick);
//                        else
//                            updateCandle(tick);
//                        break;
//                    case 1380:
//                        if (date.getDayOfMonth() != previousDate.getDayOfMonth())
//                            newCandle(tick.getTimestamp(), tick);
//                        else
//                            updateCandle(tick);
//                        break;
//                }
//            previousDate = date;
//        } else {
//            long currentStamp = tick.getTimestamp();
//            if ((currentStamp - currentStamp % (base)) != stampReference || freq == 0 || flow.size() == 0) {
//                stampReference = currentStamp - currentStamp % (base);
//                Date refDate = new Date(stampReference);
//                newCandle(refDate.getTime(), tick);
//            } else {
//                updateCandle(tick);
//                double progress = Utils.round((double) (currentStamp % (base)) / base, 2) * 100;
//                flow.get(0).setProgress((int) progress);
//            }
//        }
//
//        if (Global.hasCompletedLoading && (Global.getInstance().isOnline || Global.getInstance().random))
//            webControllerOut.sendLiveFlow(flow.get(0));
//    }
//
//    public List<Candle> getFlow() {
//        return flow;
//    }
//}
