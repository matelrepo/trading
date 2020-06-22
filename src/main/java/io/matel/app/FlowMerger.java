package io.matel.app;

import io.matel.app.config.Global;
import io.matel.app.controller.WsController;
import io.matel.app.database.SaverController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.state.ProcessorState;
import io.matel.app.config.tools.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class FlowMerger {

    @Autowired
    protected WsController wsController;

    @Autowired
    AppController appController;

    @Autowired
    private Global global;

    @Autowired
    SaverController saverController;

    protected ProcessorState processorState;
//    protected Event event;


    protected List<Candle> flow = new ArrayList<>();
    protected int freq;
    protected ContractBasic contract;
    protected long base;
    protected long stampReference;
    protected Candle previousCandle;
    protected LocalDate lastDayOfQuarter;
    private long lastIdcandleDatabase;
     boolean smallCandleNoiseRemoval = false;
    private boolean readOnlyCandle = true;
    private boolean readOnlyProcessorState = true;

    public static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public FlowMerger(ContractBasic contract, int freq) {
        this.contract = contract;
        this.freq = freq;
        base = 60000L * freq <= 0 ? 60000L : 60000L * freq; // Special freq = 1380, 6900,35000
    }

//    public void setEvent(Event event){
//        this.event=event;
//    }

    public synchronized void newCandle(OffsetDateTime timestampCandle, OffsetDateTime timestampTick, long idTick, Double open, Double high, Double low, double close, int volume) {
        if (flow.size() > 1) {
            smallCandleNoiseRemoval = flow.get(0).getLow() >= flow.get(1).getLow() && flow.get(0).getHigh() <= flow.get(1).getHigh() && freq > 0;
            if (smallCandleNoiseRemoval || flow.get(0).isSmallCandleNoiseRemoval()) {
                flow.get(0).setSmallCandleNoiseRemoval(true);
                updateCandle(timestampTick, idTick, open, high, low, close, volume);

            }
        }
        if (!smallCandleNoiseRemoval) {
            double price = previousCandle == null ? -1 : previousCandle.getClose();
            Candle candle = new Candle(timestampCandle, timestampTick, price, open, high, low, close, contract, freq);
            candle.setVolume(volume);
            candle.setId(global.getIdCandle(true));
            candle.setIdtick(idTick);
            candle.setNewCandle(true);
            candle.setFreq(freq);

            if (flow.size() > 0) {
                candle.setColor(flow.get(0).getColor());
                saverController.saveBatchCandles(flow.get(0), false);
            }


            flow.add(0, candle);
            if (flow.size() > Global.MAX_LENGTH_CANDLE) {
                flow.remove(flow.size() - 1);
            }
           // consecutiveUpDownCounter();
        }
    }

    private void updateCandle(OffsetDateTime timestamp, long idTick, Double open, Double high, Double low, double close, int volume) {
        if (flow.size() > 0) {
            flow.get(0).setNewCandle(false);
            flow.get(0).setIdtick(idTick);
            flow.get(0).setTimestampTick(timestamp);
            flow.get(0).setVolume(flow.get(0).getVolume() + volume);

                if (close > flow.get(0).getHigh())
                    flow.get(0).setHigh(close);

                if (close < flow.get(0).getLow())
                    flow.get(0).setLow(close);

            flow.get(0).setClose(close);
//        consecutiveUpDownCounter();
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

    protected void merge(OffsetDateTime timestampTick, long idTick, Double open, Double high, Double low, double close, int volume) {
        if (previousCandle == null) {
            newCandle(timestampTick, timestampTick, idTick, open, high, low, close, volume);
            if (freq == 100000)
                lastDayOfQuarter = Utils.getEndDayOfTheQuarter(timestampTick.getYear(), timestampTick.getMonth().getValue());
        } else if (freq > 840) {
                switch (freq) {
                    case 100000:
                        if (lastDayOfQuarter != null) {
                            if (timestampTick.toLocalDate().compareTo(lastDayOfQuarter) > 0) { // new quarter
                                flow.get(0).setSmallCandleNoiseRemoval(false);
                                newCandle(timestampTick, timestampTick, idTick, open, high, low, close, volume);
                                lastDayOfQuarter = Utils.getEndDayOfTheQuarter(timestampTick.getYear(), timestampTick.getMonth().getValue());
                                processorState.setLastDayOfQuarter(lastDayOfQuarter);
                            } else {
                                updateCandle(timestampTick, idTick, open, high, low, close, volume);
                            }
                        } else {
                            newCandle(timestampTick, timestampTick, idTick, open, high, low, close, volume);
                            lastDayOfQuarter = Utils.getEndDayOfTheQuarter(timestampTick.getYear(), timestampTick.getMonth().getValue());
                            processorState.setLastDayOfQuarter(lastDayOfQuarter);
                        }
                        break;
                    case 300000:
                        if (timestampTick.getYear() > previousCandle.getTimestampTick().getYear()) {
                            if (flow.size() > 0)
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setYearlyMark(flow.get(0).getClose());
                            flow.get(0).setSmallCandleNoiseRemoval(false);
                            newCandle(timestampTick, timestampTick, idTick, open, high, low, close, volume);
                        } else
                            updateCandle(timestampTick, idTick, open, high, low, close, volume);
                        break;
                    case 35000:
                        if (timestampTick.getDayOfMonth() < previousCandle.getTimestampTick().getDayOfMonth()) {
                            if (flow.size() > 0)
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setMonthlyMark(flow.get(0).getClose());
                            flow.get(0).setSmallCandleNoiseRemoval(false);
                            newCandle(timestampTick, timestampTick, idTick, open, high, low, close, volume);
                        } else
                            updateCandle(timestampTick, idTick, open, high, low, close, volume);
                        break;
                    case 6900:
                        if (timestampTick.getDayOfWeek().getValue() < previousCandle.getTimestampTick().getDayOfWeek().getValue() ||
                                (timestampTick.getDayOfWeek().getValue() == previousCandle.getTimestampTick().getDayOfWeek().getValue() &&  Period.between(timestampTick.toLocalDate(), previousCandle.getTimestampTick().toLocalDate()).getDays()<-1)) {
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setWeeklyMark(flow.get(0).getClose());
                            flow.get(0).setSmallCandleNoiseRemoval(false);
                            newCandle(timestampTick, timestampTick, idTick, open, high, low, close, volume);
                        } else
                            updateCandle(timestampTick, idTick, open, high, low, close, volume);
                        break;
                    case 1380:
                        if (timestampTick.getDayOfMonth() != previousCandle.getTimestampTick().getDayOfMonth()) {
                            if (flow.size() > 0) {
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setDailyMark(flow.get(0).getClose());
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setHigh(close);
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setLow(close);
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setChangePerc(0);
                                appController.getGenerators().get(contract.getIdcontract()).getGeneratorState().setChangeValue(0);
                                flow.get(0).setSmallCandleNoiseRemoval(false);
                            }
                            newCandle(timestampTick, timestampTick, idTick, open, high, low, close, volume);
                        } else
                            updateCandle(timestampTick, idTick, open, high, low, close, volume);
                        break;
                }
               // previousCandle = flow.get(0);
        } else {
            long currentStamp = timestampTick.toEpochSecond() * 1000;
            if ((currentStamp - currentStamp % (base)) != stampReference || freq == 0 || flow.size() == 0) {
                stampReference = currentStamp - currentStamp % (base);
                OffsetDateTime refDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(stampReference / 1000), Global.ZONE_ID);
                flow.get(0).setSmallCandleNoiseRemoval(false);
                newCandle(refDate,timestampTick, idTick, open, high, low, close, volume);
            } else {
                updateCandle(timestampTick, idTick, open, high, low, close, volume);
                double progress = Utils.round((double) (currentStamp % (base)) / base, 2) * 100;
                flow.get(0).setProgress((int) progress);
            }
        }
        previousCandle = flow.get(0);
    }

    public void resetFlow() {
        flow.clear();
    }

    public void setFlow(List<Candle> candles) {
        if(candles!=null)
        flow = candles;
        if (flow.size() > 0) {
            previousCandle = flow.get(0);
            //previousPrice = flow.get(0).getClose();
            stampReference = flow.get(0).getTimestampCandle().toEpochSecond() * 1000;
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
