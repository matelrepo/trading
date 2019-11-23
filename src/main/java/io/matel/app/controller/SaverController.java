package io.matel.app.controller;

import io.matel.app.AppController;
import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.repo.CandleRepository;
import io.matel.app.domain.Tick;
import io.matel.app.repo.TickRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SaverController {

    private static final Logger LOGGER = LogManager.getLogger(SaverController.class);
    private List<Tick> ticksBuffer = new ArrayList<>();
    private List<Candle> candlesBuffer = new ArrayList<>();

    @Autowired
    Global global;

    TickRepository tickRepository;
    CandleRepository candleRepository;
    AppController appController;

    public SaverController(TickRepository tickRepository,
                           CandleRepository candleRepository,
                           AppController appController){
        this.tickRepository = tickRepository;
        this.candleRepository = candleRepository;
        this.appController = appController;
    }

    public void saveNow(){
        saveNow(null);
    }

    public void saveNow(Long idcontract) {
            saveBatchTicks();
            saveBatchCandles();
            updateCurrentCandle(idcontract);
    }

    public synchronized void saveBatchTicks() {
        saveBatchTicks(null);
    }

    public synchronized void saveBatchTicks(Tick tick) {
        if (!Global.READ_ONLY_TICKS) {
            if (tick != null)
                this.ticksBuffer.add(tick);

                if (ticksBuffer.size() > Global.MAX_TICKS_SIZE_SAVING || tick == null) {
                    LOGGER.info("Regular batch ticks saving (" + ticksBuffer.size()+ ")");
                    tickRepository.saveAll(this.ticksBuffer);
                ticksBuffer.clear();
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
                if (processor.getFlow().get(0) != null && processor.getFreq() > 0) {
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
