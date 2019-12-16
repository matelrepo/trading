package io.matel.app;

import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.Tick;
import io.matel.app.state.LogProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaverController {
    private static final Logger LOGGER = LogManager.getLogger(SaverController.class);
    private List<Tick> ticksBuffer = new ArrayList<>();
    private List<LogProcessorState> logProcessorBuffer = new ArrayList<>();

    private List<Candle> insertCandlesBuffer = new ArrayList<>();
    private List<Candle> updateCandlesBuffer = new ArrayList<>();
    private Database database;

    public SaverController(Database database) {
        this.database = database;
    }

//    public void saveNow(boolean firstRun) {
//        saveNow(null, null, firstRun);
//    }


    public void saveNow(Generator gen, boolean saveNow) {
        Long idcontract = null;
        if (gen != null) idcontract = gen.getContract().getIdcontract();

        int numTicks = saveBatchTicks(saveNow);
        LOGGER.info("Saving now " + numTicks + " ticks for contract " + idcontract);
        if (numTicks > 0 || saveNow) {
            int numCandles = saveBatchCandles();
            LOGGER.info("Saving now " + numCandles + " candles for contract " + idcontract);
            updateCurrentCandle(gen);
  //          saveBatchLogProcessor(null);
//            saveProcessorStates();
        } else {
            LOGGER.warn("Cannot save candles because ticks were not saved!");
        }

        LOGGER.info("Saving completed");
    }


    public synchronized int saveBatchTicks(boolean saveNow) {
        return saveBatchTicks(null, saveNow);
    }

    public synchronized int saveBatchTicks(Tick tick, boolean save) {
        int count = 0;
        if (!Global.READ_ONLY_TICKS) {
            if (tick != null)
                this.ticksBuffer.add(tick);

            if (ticksBuffer.size() > 0 && (ticksBuffer.size() > Global.MAX_TICKS_SIZE_SAVING || tick == null || save)) {
                LOGGER.info(ZonedDateTime.now() + "- Regular batch ticks saving (" + ticksBuffer.size() + ")");
                count = database.saveTicks(this.ticksBuffer);
                ticksBuffer.clear();
            }
        }
        return count;
    }

    public synchronized int saveBatchLogProcessor(LogProcessorState logProcessorState) {
        int count =0;
        if (!Global.READ_ONLY_LOG_PROCESSOR) {
            if (logProcessorState != null)
                this.logProcessorBuffer.add(logProcessorState);

            if (logProcessorBuffer.size() > 0 && (logProcessorBuffer.size() > Global.MAX_CANDLES_SIZE_SAVING || logProcessorState == null)) {
                LOGGER.info(ZonedDateTime.now() + "- Regular batch logProcessor saving (" + logProcessorBuffer.size() + ")");
                List<LogProcessorState> list = null;
                list = database.logProcessorStateRepo.saveAll(this.logProcessorBuffer);
                count = list.size();
                logProcessorBuffer.clear();
            }
        }
        return count;
    }

    public synchronized int saveBatchCandles() {
        return saveBatchCandles(null, true);
    }

    public synchronized int saveBatchCandles(Candle candle, boolean saveNow) {
        int count = 0;
        if (!Global.READ_ONLY_CANDLES) {
            if (candle != null)
                if (candle.getFreq() > 0) {
                    if (candle.getId() <= Global.startIdCandle) {
                        database.updateCandle(candle);
                    } else {
                        insertCandlesBuffer.add(candle);
                    }
                }
            if (insertCandlesBuffer.size() > Global.MAX_CANDLES_SIZE_SAVING || candle == null || saveNow) {
                LOGGER.info("Regular batch candles saving (" + insertCandlesBuffer.size() + ")");
                count = database.saveCandles(this.insertCandlesBuffer);
                insertCandlesBuffer.clear();
            }
        }
        return count;
    }

    private synchronized void updateCurrentCandle(Generator gen) {
        if (!Global.READ_ONLY_CANDLES) {
            if (gen == null) {
//                gen.getProcessors().forEach((frequency, processor) -> {
//                    if (processor.getFlow().size() > 0) {
////                        if (frequency > 0)
////                            candleRepository.save(processor.getFlow().get(0));
//                    }
//                });
            } else {
                gen.getProcessors().forEach((frequency, processor) -> {
                    if (processor.getFlow().size() > 0)
                        if (processor.getFlow().get(0) != null && processor.getFreq() > 0) {
                            Candle candle = processor.getFlow().get(0);
                            saveBatchCandles(candle, true);
                        }
                });
            }
            LOGGER.info("Candle updated completed");
        }
    }
}
