package io.matel.app;

import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.Tick;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SaverController {
    private static final Logger LOGGER = LogManager.getLogger(SaverController.class);
    private List<Tick> ticksBuffer = new ArrayList<>();
    private List<Candle> insertCandlesBuffer = new ArrayList<>();
    private List<Candle> updateCandlesBuffer = new ArrayList<>();
    private Database database;

    public SaverController(Database database) {
        this.database = database;
    }

    public void saveNow(boolean firstRun) {
        saveNow(null, firstRun);
    }


    public void saveNow(Long idcontract, boolean initialComputation) {
////        global.setSaving(true);
//        int num =  saveBatchTicks();
//        LOGGER.info("Saving now with " + num + " ticks for contract " + idcontract);
//        if(num>0 || initialComputation) {
//            saveBatchCandles();
////            updateCurrentCandle(idcontract);
////            saveBatchLogProcessor();
////            saveProcessorStates();
//        }else{
//            LOGGER.warn("Cannot save candles because ticks were not saved!");
//        }
//
//        LOGGER.info("Saving completed");
////        global.setSaving(false);
    }


    public synchronized int saveBatchTicks() {
        return saveBatchTicks(null);
    }

    public synchronized int saveBatchTicks(Tick tick) {
        int count = 0;
        if (!Global.READ_ONLY_TICKS) {
            if (tick != null)
                this.ticksBuffer.add(tick);

            if (ticksBuffer.size() > 0 && (ticksBuffer.size() > Global.MAX_TICKS_SIZE_SAVING || tick == null)) {
                LOGGER.info("Regular batch ticks saving (" + ticksBuffer.size() + ")");
                count = database.saveTicks(this.ticksBuffer);
                ticksBuffer.clear();
            }
        }
        return count;
    }

    public synchronized void saveBatchCandles(Candle candle) {
        if (!Global.READ_ONLY_CANDLES) {
            if (candle != null)
                if (candle.getFreq() > 0) {
                    if (candle.getId()<=Global.startIdCandle){
                     database.updateCandle(candle);
                    }else {
                        insertCandlesBuffer.add(candle);
                    }
                }
            if (insertCandlesBuffer.size() > Global.MAX_CANDLES_SIZE_SAVING || candle == null) {
                LOGGER.info("Regular batch candles saving (" + insertCandlesBuffer.size() + ")");
                database.saveCandles(this.insertCandlesBuffer);
                insertCandlesBuffer.clear();
            }
        }
    }
}
