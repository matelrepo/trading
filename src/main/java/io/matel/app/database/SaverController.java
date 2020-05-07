package io.matel.app.database;

import io.matel.app.Generator;
import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.Tick;
import io.matel.app.repo.ProcessorStateRepo;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaverController {
    private static final Logger LOGGER = LogManager.getLogger(SaverController.class);
    private List<Tick> ticksBuffer = new ArrayList<>();

    private List<Candle> insertCandlesBuffer = new ArrayList<>();
    private List<ProcessorState> insertProcessorState = new ArrayList<>();
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
        //LOGGER.info("Saving now " + numTicks + " ticks for contract " + idcontract);
        if (numTicks > 0 || saveNow) {
            int numCandles = saveBatchCandles();
            //LOGGER.info("Saving now " + numCandles + " candles for contract " + idcontract);
            updateCurrentCandle(gen);
            saveBatchProcessorState(true);
//            saveProcessorStates();
        } else {
            //LOGGER.warn("Cannot save candles because ticks were not saved!");
        }

       // LOGGER.info("Saving completed");
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

    public synchronized int saveBatchProcessorState(boolean saveNow) {
        return saveBatchProcessorState(null, saveNow);
    }

    public synchronized int saveBatchProcessorState(ProcessorState processorState, boolean saveNow) {
        int count = 0;
            if (processorState != null) {
                this.insertProcessorState.add(processorState);
            }
        if (!Global.READ_ONLY_CANDLES && (Global.hasCompletedLoading || Global.COMPUTE_DEEP_HISTORICAL)) {
            if (insertProcessorState.size() > 0 && (insertProcessorState.size() > Global.MAX_CANDLES_SIZE_SAVING * 4 || processorState == null || saveNow)) {
                LOGGER.info(ZonedDateTime.now() + "- Regular batch processor state saving (" + insertProcessorState.size() + ")");
                count = database.saveProcessorStates(this.insertProcessorState);
                insertProcessorState.clear();
            }
        }
        return count;
    }


public List<ProcessorState> getProcessorStateBuffer(){
        return insertProcessorState;
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
