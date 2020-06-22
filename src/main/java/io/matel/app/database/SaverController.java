package io.matel.app.database;

import io.matel.app.AppController;
import io.matel.app.Generator;
import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.Tick;
import io.matel.app.repo.ProcessorStateRepo;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SaverController {
    private static final Logger LOGGER = LogManager.getLogger(SaverController.class);
    private List<Tick> ticksBuffer = new ArrayList<>();

    private List<Candle> insertCandlesBuffer = new ArrayList<>();
    private List<ProcessorState> insertProcessorState = new ArrayList<>();
    private List<ProcessorState> insertEvents = new ArrayList<>();
//    private Database database;

    public List<Candle> getInsertCandlesBuffer(){
        return insertCandlesBuffer;
    }

    @Autowired
    ProcessorStateRepo processorStateRepo;

    @Autowired
    AppController appController;

    public SaverController() {

    }

//    public void saveNow(boolean firstRun) {
//        saveNow(null, null, firstRun);
//    }


    public void saveNow(Generator gen, boolean saveNow) {
        Long idcontract = null;
        if (gen != null) idcontract = gen.getContract().getIdcontract();

        int numTicks = saveBatchTicks(saveNow);
      //  System.out.println(Thread.currentThread().getName());
        LOGGER.info("Saving now " + numTicks + " ticks for contract " + idcontract);
        if (numTicks > 0 || saveNow) {
            int numCandles = saveBatchCandles();
            //LOGGER.info("Saving now " + numCandles + " candles for contract " + idcontract);
            updateCurrentCandle(gen);
            saveBatchProcessorState(true);
            updateProcessorState(gen);
            saveBatchEvents(true);
//            saveProcessorStates();
        } else {
            //LOGGER.warn("Cannot save candles because ticks were not saved!");
        }

       // LOGGER.info("Saving completed");
    }

    public void updateProcessorState(Generator generator){
        if (!Global.READ_ONLY_PROCESSOR_STATE) {
            if (generator == null) {
                appController.getGenerators().forEach((id, g) -> {
                    g.getProcessors().forEach((freq, proc) -> {
                        if (freq > 0)
                            saveBatchProcessorState(proc.getProcessorState(), true);
                    });
                });
            } else {
                generator.getProcessors().forEach((freq, proc) -> {
                    if (freq > 0)
                        saveBatchProcessorState(proc.getProcessorState(), true);
                });
            }
        }
    }


    public synchronized int saveBatchTicks(boolean saveNow) {
        return saveBatchTicks(null, saveNow);
    }

    public synchronized int saveBatchTicks(Tick tick, boolean saveNow) {
        int count = 0;
        if (!Global.READ_ONLY_TICKS) {
            if (tick != null)
                this.ticksBuffer.add(tick);

            if (ticksBuffer.size() > 0 && (ticksBuffer.size() > Global.MAX_TICKS_SIZE_SAVING || tick == null || saveNow)) {
                count = appController.getDatabase().saveTicks(this.ticksBuffer);
                LOGGER.info(ZonedDateTime.now() + "- Regular batch ticks saving (" + count + ")");
                ticksBuffer.clear();
            }
        }
        return count;
    }

    public synchronized void saveBatchProcessorState(boolean saveNow) {
        saveBatchProcessorState(null, saveNow);
    }

    public synchronized void saveBatchProcessorState(ProcessorState processorState, boolean saveNow) {
        int count = 0;
            if (processorState != null) {
                this.insertProcessorState.add(processorState);
            }
        if (!Global.READ_ONLY_PROCESSOR_STATE && (Global.hasCompletedLoading || Global.COMPUTE_DEEP_HISTORICAL)) {
            if (insertProcessorState.size() > 0 && (insertProcessorState.size() > Global.MAX_CANDLES_SIZE_SAVING * 4 || processorState == null || saveNow)) {
                LOGGER.info(ZonedDateTime.now() + "- Regular batch processor state saving (" + insertProcessorState.size() + ")");
                processorStateRepo.saveAll(this.insertProcessorState);
                insertProcessorState.clear();
            }
        }
    }

    public synchronized void saveBatchEvents(boolean saveNow) {
       // System.out.println("matel " + saveNow);
        saveBatchEvents(null, saveNow);
    }

    public synchronized void saveBatchEvents(ProcessorState events, boolean saveNow) {
        int count = 0;
        if (events != null) {
            if(events.getTimestampTick().until(OffsetDateTime.now(), ChronoUnit.DAYS) > 80){
            }    else{
                this.insertEvents.add(events);
            }

        }
        if (!Global.READ_ONLY_PROCESSOR_STATE && (Global.hasCompletedLoading || Global.COMPUTE_DEEP_HISTORICAL)) {
            if (insertEvents.size() > 0 && (insertEvents.size() > Global.MAX_CANDLES_SIZE_SAVING * 4 || insertEvents == null || saveNow)) {
                LOGGER.info(ZonedDateTime.now() + "- Regular batch events state saving (" + insertEvents.size() + ")");
                appController.getDatabase().saveEvents(this.insertEvents);
                insertEvents.clear();
            }
        }
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
                        appController.getDatabase().updateCandle(candle);
                    } else {
                        insertCandlesBuffer.add(candle);
                    }
                }
            if (insertCandlesBuffer.size() > Global.MAX_CANDLES_SIZE_SAVING || candle == null || saveNow) {
                LOGGER.info("Regular batch candles saving (" + insertCandlesBuffer.size() + ")");
                count = appController.getDatabase().saveCandles(this.insertCandlesBuffer);
                insertCandlesBuffer.clear();
            }
        }
        return count;
    }


    private synchronized void updateCurrentCandle(Generator gen) {
        if (!Global.READ_ONLY_CANDLES) {
            if (gen == null) {
                appController.getGenerators().forEach((id, g)->{
                    g.getProcessors().forEach((frequency, processor) -> {
                        if (processor.getFlow().size() > 0)
                            if (processor.getFlow().get(0) != null && processor.getFreq() > 0) {
                                Candle candle = processor.getFlow().get(0);
                                saveBatchCandles(candle, true);
                            }
                    });
                });
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
