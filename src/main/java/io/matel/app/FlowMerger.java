package io.matel.app;

import io.matel.app.config.Global;
import io.matel.app.controller.SaverController;
import io.matel.app.controller.WsController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.tools.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlowMerger {

    @Autowired
    protected WsController wsController;

    @Autowired
    SaverController saverController;

    @Autowired
    private Global global;

    protected List<Candle> flow = new ArrayList<>();
    protected int freq;
    protected ContractBasic contract;
    protected long base;
    protected long stampReference;
    protected ZonedDateTime previousDate;
    protected double previousPrice = -1;
    private long lastIdcandleDatabase;

    public FlowMerger(ContractBasic contract, int freq) {
        this.contract = contract;
        this.freq = freq;
        base = 60L * freq <= 0 ? 1L : 60L * freq; // Special freq = 1380, 6900,35000
    }

    public synchronized void newCandle(ZonedDateTime timestamp, Tick tick) {
        boolean smallCandleNoiseRemoval = false;
        if (flow.size() > 2) {
            smallCandleNoiseRemoval = flow.get(0).getLow() >= flow.get(1).getLow() && flow.get(0).getHigh() <= flow.get(1).getHigh() && freq > 0;
            if (smallCandleNoiseRemoval)
                updateCandle(tick);
        }
        if (!smallCandleNoiseRemoval) {
            Candle candle = new Candle(timestamp, previousPrice, tick.getClose(), contract, freq, timestamp);
            candle.setId(global.getIdCandle(true));
            candle.setIdtick(tick.getId());
            candle.setNewCandle(true);
            candle.setFreq(freq);

            if (flow.size() > 0) {
                candle.setColor(flow.get(0).getColor());
                 saverController.saveBatchCandles(flow.get(0));
            }

                flow.add(0, candle);
                if (flow.size() > Global.MAX_LENGTH_FLOW) {
                    flow.remove(flow.size() - 1);
                }


            previousPrice = candle.getClose();
        }
    }

    public void updateCandle(Tick tick) {
        flow.get(0).setNewCandle(false);
        flow.get(0).setIdtick(tick.getId());

        if (tick.getClose() > flow.get(0).getHigh())
            flow.get(0).setHigh(tick.getClose());

        if (tick.getClose() < flow.get(0).getLow())
            flow.get(0).setLow(tick.getClose());
        flow.get(0).setClose(tick.getClose());
        previousPrice = tick.getClose();
    }

    protected void merge(Tick tick) {
//        if(contract.getIdcontract() ==5 && freq == 1 )
//            System.out.println(tick.toString());
        if (freq > 420) {
            if (previousDate == null)
                newCandle(tick.getTimestamp(), tick);
            else
                switch (freq) {
                    case 100000:
                        if (tick.getTimestamp().getDayOfYear() < previousDate.getDayOfYear())
                            newCandle(tick.getTimestamp(), tick);
                        else
                            updateCandle(tick);
                        break;
                    case 35000:
                        if (tick.getTimestamp().getDayOfMonth() < previousDate.getDayOfMonth())
                            newCandle(tick.getTimestamp(), tick);
                        else
                            updateCandle(tick);
                        break;
                    case 6900:
                        if (tick.getTimestamp().getDayOfWeek().getValue() < previousDate.getDayOfWeek().getValue())
                            newCandle(tick.getTimestamp(), tick);
                        else
                            updateCandle(tick);
                        break;
                    case 1380:
                        if (tick.getTimestamp().getDayOfMonth() != previousDate.getDayOfMonth())
                            newCandle(tick.getTimestamp(), tick);
                        else
                            updateCandle(tick);
                        break;
                }
            previousDate = tick.getTimestamp();
        } else {
            long currentStamp = tick.getTimestamp().toEpochSecond();
            if ((currentStamp - currentStamp % (base)) != stampReference || freq == 0 || flow.size() == 0) {
                stampReference = currentStamp - currentStamp % (base);
                ZonedDateTime refDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(stampReference), ZoneId.of("Asia/Bangkok"));
                newCandle(refDate, tick);
            } else {
                updateCandle(tick);
                double progress = Utils.round((double) (currentStamp % (base)) / base, 2) * 100;
                flow.get(0).setProgress((int) progress);
            }
        }

        if (Global.ONLINE || Global.RANDOM) {
            wsController.sendLiveCandle(flow.get(0));
        }

//        if(contract.getIdcontract() ==5 && freq == 1 )
//            System.out.println(flow.get(0).toString());
    }

    public void resetFlow(){
        flow.clear();
    }

    public void setFlow(List<Candle> candles){
        flow.addAll(0, candles);
        if(flow.size()>0) {
            previousDate = flow.get(0).getTimestamp();
            previousPrice = flow.get(0).getClose();
            stampReference = flow.get(0).getTimestamp().toEpochSecond();
        }
    }

    public long getLastIdcandleDatabase() {
        return lastIdcandleDatabase;
    }

    public void setLastIdcandleDatabase(long lastIdcandleDatabase) {
        this.lastIdcandleDatabase = lastIdcandleDatabase;
    }

    public int getFreq(){
        return freq;
    }
}
