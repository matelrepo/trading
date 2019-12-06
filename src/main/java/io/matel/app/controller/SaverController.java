package io.matel.app.controller;

import io.matel.app.AppController;
import io.matel.app.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.Tick;
import io.matel.app.repo.*;
import io.matel.app.state.LogProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SaverController {

    @PreDestroy
    public void beforeClosing() {
        int num =  saveBatchTicks();
        LOGGER.info("Saving with " + num + " ticks before closing!");
    }

    private static final Logger LOGGER = LogManager.getLogger(SaverController.class);
    private List<Tick> ticksBuffer = new ArrayList<>();
    private List<Candle> candlesBuffer = new ArrayList<>();
    List<LogProcessorState> logDataBuffer = new ArrayList<>();


    @Autowired
    Global global;

    @Autowired
    GeneratorStateRepo generatorStateRepo;

    @Autowired
    ProcessorStateRepository processorStateRepository;

    LogProcessorStateRepo logProcessorStateRepository;
    TickRepository tickRepository;
    CandleRepository candleRepository;
    AppController appController;

    public SaverController(TickRepository tickRepository,
                           CandleRepository candleRepository,
                           LogProcessorStateRepo logProcessorStateRepository,
                           AppController appController){
        this.tickRepository = tickRepository;
        this.candleRepository = candleRepository;
        this.appController = appController;
        this.logProcessorStateRepository = logProcessorStateRepository;
    }

    public void saveNow(boolean inititalComputation){
        saveNow(null, inititalComputation);
    }


    public void saveNow(Long idcontract, boolean initialComputation) {
        global.setSaving(true);
        int num =  saveBatchTicks();
        LOGGER.info("Saving now with " + num + " ticks for contract " + idcontract);
        if(num>0 || initialComputation) {
            saveBatchCandles();
            updateCurrentCandle(idcontract);
            saveBatchLogProcessor();
            saveProcessorStates();
        }else{
            LOGGER.warn("Cannot save candles because ticks were not saved!");
        }

        LOGGER.info("Saving completed");
        global.setSaving(false);
    }


    public synchronized int saveBatchTicks() {
        return saveBatchTicks(null);
    }

    public synchronized int saveBatchTicks(Tick tick) {
        List<Tick> results = new ArrayList<>();
        if (!Global.READ_ONLY_TICKS) {
            if (tick != null)
                this.ticksBuffer.add(tick);

                if (ticksBuffer.size()>0 && (ticksBuffer.size() > Global.MAX_TICKS_SIZE_SAVING || tick == null)) {
                    LOGGER.info("Regular batch ticks saving (" + ticksBuffer.size()+ ")");
                    results = tickRepository.saveAll(this.ticksBuffer);
                ticksBuffer.clear();
            }

//                appController.getGenerators().forEach((id, gen)->{
//                    gen.getGeneratorState().setTimestamp(ZonedDateTime.now());
//                    generatorStateRepo.save(gen.getGeneratorState());
//                });

        }
        return results.size();
    }

    public synchronized void saveBatchLogProcessor() {
        saveBatchLogProcessor(null);
    }


    public synchronized void saveBatchLogProcessor(LogProcessorState data) {
        if (!Global.READ_ONLY_LOG_PROCESSOR) {
            if (data != null)
                this.logDataBuffer.add(data);

            if (logDataBuffer.size() > Global.MAX_TICKS_SIZE_SAVING*3 || data == null) {
                LOGGER.info("Regular log processor saving (" + logDataBuffer.size()+ ")");
                logProcessorStateRepository.saveAll(this.logDataBuffer);
                logDataBuffer.clear();
            }
        }
    }

    public synchronized void saveBatchCandles() {
        saveBatchCandles(null);
    }

    public synchronized void saveBatchCandles(Candle candle) {
        if (!Global.READ_ONLY_CANDLES) {
            if (candle != null)
                if (candle.getFreq() > 0) {
                    candlesBuffer.add(candle);
                }
            if (candlesBuffer.size() > Global.MAX_CANDLES_SIZE_SAVING || candle == null) {
                    LOGGER.info("Regular batch candles saving (" + candlesBuffer.size()+ ")");
                candleRepository.saveAll(this.candlesBuffer);
                candlesBuffer.clear();
            }
        }
    }

    public void saveGeneratorStates(){
        LOGGER.info("Regular generators states saving");
        appController.getGenerators().forEach((id, gen)->{
            if(gen.getGeneratorState().getTimestamp()!=null)
            generatorStateRepo.save(gen.getGeneratorState());
        });
    }

    public void saveProcessorStates(){
        appController.getGenerators().forEach((id,gen)->{
            gen.getProcessors().forEach((freq, processor)->{
                processorStateRepository.save(processor.getProcessorState());
            });
        });
    }

    private synchronized void updateCurrentCandle(Long idcontract) {
    if(!Global.READ_ONLY_CANDLES) {
        LOGGER.info("Updating (now) ticks & candles for contract: " + idcontract);
        if (idcontract == null) {
            appController.getGenerators().forEach((id, gen) -> {
                gen.getProcessors().forEach((frequency, processor) -> {
                    if (processor.getFlow().size()>0) {
                        if (frequency > 0)
                            candleRepository.save(processor.getFlow().get(0));
                    }
                });
            });
        } else {
            long idTick = global.getIdTick(false);
            appController.getGenerators().get(idcontract).getProcessors().forEach((frequency, processor) -> {
                if (processor.getFlow().size() > 0 && processor.getFlow().get(0) != null && processor.getFreq() > 0) {
                    Candle candle = processor.getFlow().get(0);
                    candle.setIdtick(idTick);
                    candleRepository.save(candle);
                }
            });
        }
        LOGGER.info("Saving completed");
    }
    }
}
