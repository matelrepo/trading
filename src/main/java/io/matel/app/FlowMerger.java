package io.matel.app;

import io.matel.app.config.Global;
import io.matel.app.controller.WsController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.state.ProcessorState;
import io.matel.app.config.tools.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlowMerger {

    @Autowired
    protected WsController wsController;

    @Autowired
    AppController appController;

    @Autowired
    private Global global;

    protected ProcessorState processorState;


    protected List<Candle> flow = new ArrayList<>();
    protected int freq;
    protected ContractBasic contract;
    protected long base;
    protected long stampReference;
    protected ZonedDateTime previousDate;
    protected LocalDate lastDayOfQuarter;
    protected double previousPrice = -1;
    private long lastIdcandleDatabase;
    boolean smallCandleNoiseRemoval = false;

    public static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public FlowMerger(ContractBasic contract, int freq) {
        this.contract = contract;
        this.freq = freq;
        base = 60000L * freq <= 0 ? 60000L : 60000L * freq; // Special freq = 1380, 6900,35000
    }

    public synchronized void newCandle(ZonedDateTime timestamp, long idTick, Double open, Double high, Double low, double close, boolean isCandleComputed) {
        if (flow.size() > 2) {
            smallCandleNoiseRemoval = flow.get(0).getLow() >= flow.get(1).getLow() && flow.get(0).getHigh() <= flow.get(1).getHigh() && freq > 0;
            if (smallCandleNoiseRemoval)
                updateCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
        }
        if (!smallCandleNoiseRemoval) {
            Candle candle = new Candle(timestamp, previousPrice, open, high, low, close, contract, freq, isCandleComputed);
            candle.setId(global.getIdCandle(true));
            candle.setIdtick(idTick);
            candle.setNewCandle(true);
            candle.setFreq(freq);

            if (flow.size() > 0) {
                candle.setColor(flow.get(0).getColor());
                appController.getGenerators().get(contract.getIdcontract())
                        .getDatabase().getSaverController().saveBatchCandles(flow.get(0), false);
            }

            flow.add(0, candle);
            if (flow.size() > Global.MAX_LENGTH_CANDLE) {
                flow.remove(flow.size() - 1);
            }

            consecutiveUpDownCounter();


            previousPrice = candle.getClose();
        }
    }

    private void updateCandle(ZonedDateTime timestamp, long idTick, Double open, Double high, Double low, double close, boolean isCandleComputed) {
        if (flow.size() > 0) {
            flow.get(0).setNewCandle(false);
            flow.get(0).setIdtick(idTick);

            if (isCandleComputed) {
                if (high > flow.get(0).getHigh())
                    flow.get(0).setHigh(high);

                if (low < flow.get(0).getLow())
                    flow.get(0).setLow(low);
            } else {
                if (close > flow.get(0).getHigh())
                    flow.get(0).setHigh(close);

                if (close < flow.get(0).getLow())
                    flow.get(0).setLow(close);
            }

            flow.get(0).setClose(close);
//        consecutiveUpDownCounter();
            previousPrice = close;
        }
    }

    private void consecutiveUpDownCounter() {
        if (flow.size() > 1) {
            if (flow.get(0).getClose() > flow.get(1).getClose()) {
                flow.get(0).setTriggerUp(flow.get(1).getTriggerUp() + 1);
                flow.get(0).setTriggerDown(0);
            } else if (flow.get(0).getClose() < flow.get(1).getClose()) {
                flow.get(0).setTriggerUp(0);
                flow.get(0).setTriggerDown(flow.get(1).getTriggerDown() + 1);
            }
//            candle.setTriggerUp(generatorState.getTriggerUp());
//            candle.setTriggerDown(generatorState.getTriggerDown());
//            flow.set(0, candle);
        }
    }

    protected void merge(ZonedDateTime timestamp, long idTick, Double open, Double high, Double low, double close, boolean isCandleComputed) {

        if (freq > 840) {
            if (previousDate == null)
                newCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
            else
                switch (freq) {
                    case 100000:
                        if (lastDayOfQuarter != null) {
                            if (timestamp.toLocalDate().compareTo(lastDayOfQuarter) > 0) { // new quarter
                                newCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                                lastDayOfQuarter = Utils.getEndDayOfTheQuarter(timestamp.getYear(), timestamp.getMonth().getValue());
                                processorState.setLastDayOfQuarter(lastDayOfQuarter);
                            } else {
                                updateCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                            }
                        } else {
                            newCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                            lastDayOfQuarter = Utils.getEndDayOfTheQuarter(timestamp.getYear(), timestamp.getMonth().getValue());
                            processorState.setLastDayOfQuarter(lastDayOfQuarter);
                        }
                        break;
                    case 300000:
                        if (timestamp.getYear() > previousDate.getYear()) {
                            if (flow.size() > 0)
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setYearlyMark(flow.get(0).getClose());
                            newCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        } else
                            updateCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        break;
                    case 35000:
                        if (timestamp.getDayOfMonth() < previousDate.getDayOfMonth()) {
                            if (flow.size() > 0)
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setMonthlyMark(flow.get(0).getClose());
                            newCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        } else
                            updateCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        break;
                    case 6900:
                        if (timestamp.getDayOfWeek().getValue() < previousDate.getDayOfWeek().getValue() ||
                                (timestamp.getDayOfWeek().getValue() == previousDate.getDayOfWeek().getValue() &&  Period.between(timestamp.toLocalDate(), previousDate.toLocalDate()).getDays()<-1)) {
                            if (flow.size() > 0) {
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setWeeklyMark(flow.get(0).getClose());
//
                            }
                            newCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        } else
                            updateCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        break;
                    case 1380:
                        if (timestamp.getDayOfMonth() != previousDate.getDayOfMonth()) {
                            if (flow.size() > 0) {
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setDailyMark(flow.get(0).getClose());
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setHigh(close);
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setLow(close);
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setChangePerc(0);
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setChangeValue(0);

                            }
                            newCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        } else
                            updateCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                        break;
                }
            previousDate = timestamp;
        } else {
            long currentStamp = timestamp.toEpochSecond() * 1000;
            if ((currentStamp - currentStamp % (base)) != stampReference || freq == 0 || flow.size() == 0) {
                stampReference = currentStamp - currentStamp % (base);
                ZonedDateTime refDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(stampReference / 1000), Global.ZONE_ID);
                newCandle(refDate, idTick, open, high, low, close, isCandleComputed);
            } else {
                updateCandle(timestamp, idTick, open, high, low, close, isCandleComputed);
                double progress = Utils.round((double) (currentStamp % (base)) / base, 2) * 100;
                flow.get(0).setProgress((int) progress);
            }
        }
    }

    public void resetFlow() {
        flow.clear();
    }

    public void setFlow(List<Candle> candles) {
        flow = candles;
        if (flow.size() > 0) {
            previousDate = flow.get(0).getTimestamp();
            previousPrice = flow.get(0).getClose();
            stampReference = flow.get(0).getTimestamp().toEpochSecond() * 1000;
        }
    }

    public long getLastIdcandleDatabase() {
        return lastIdcandleDatabase;
    }

    public void setLastIdcandleDatabase(long lastIdcandleDatabase) {
        this.lastIdcandleDatabase = lastIdcandleDatabase;
    }

    public int getFreq() {
        return freq;
    }
}
