package io.matel.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Bar;
import io.matel.app.AppController;
import io.matel.app.AppLauncher;
import io.matel.app.Generator;
import io.matel.app.config.Global;
import io.matel.app.config.Ibconfig.DataService;
import io.matel.app.config.tools.Utils;
import io.matel.app.controller.ContractController;
import io.matel.app.database.Database;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.HistoricalDataType;
import io.matel.app.domain.Tick;
import io.matel.app.repo.ProcessorStateRepo;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Controller
public class HistoricalDataController {

    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);

    @Autowired
    ProcessorStateRepo processorStateRepo;

    @Autowired
    ContractController contractController;

    @Autowired
    AppController appController;
    @Autowired
    Global global;

    @Autowired
    DataService dataService;
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private ExecutorService executor = Executors.newFixedThreadPool(8);
    private Map<Long,Boolean> ordersEOD = new ConcurrentHashMap<>();
    private Map<Long,Boolean> ordersIB = new ConcurrentHashMap<>();
    private Semaphore sempahore = new Semaphore(1);

    private double lastClose =0;
  //  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:SS");
    DateTimeFormatter formatter =
            new DateTimeFormatterBuilder().appendPattern("yyyyMMdd  HH:mm:ss")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter();

    private Map<Long, Long> processorStatesIdTickByIdContract;

    public void findProcessorStateByContract() {
        processorStatesIdTickByIdContract= appController.getDatabase().findTopIdTickFromProcessorState();
       // System.out.println(processorStatesIdTickByIdContract.size());
    }

    public Map<Long, Long> getProcessorStatesIdTickByIdContract() {
        return processorStatesIdTickByIdContract;
    }

    private List<Candle> loadHistoricalFromDBB(long idcontract, int numCandles, int freq, long maxIdCandle, boolean setCandles) {
        List<Candle> candles = null;
        try {
            Generator generator = appController.getGenerators().get(idcontract);
            ContractBasic contract = generator.getContract();
            long id = contract.getCloneid() < 0 ? contract.getIdcontract() : contract.getCloneid();
            candles = appController.getGenerators().get(idcontract).getDatabase().getHistoricalCandles(idcontract, freq, maxIdCandle, contract.getCloneid() > 0, numCandles);
            if (setCandles || generator.getProcessors().get(freq).getFlow().size() == 0) {
                generator.getProcessors().get(freq).setFlow(candles);

            }
        }catch(NullPointerException e){
          //  e.printStackTrace();
        }
        return candles;
    }

    public void setProcessorState(long idcontract){
        ContractBasic contract = appController.getGenerators().get(idcontract).getContract();
        long id = contract.getCloneid() < 0 ? contract.getIdcontract() : contract.getCloneid();
        List<ProcessorState> states;
        try {
            states = processorStateRepo.findByIdTick(getProcessorStatesIdTickByIdContract().get(id));
            states.forEach(state -> {
                appController.getGenerators().get(id).getProcessors().get(state.getFreq()).setProcessorState(state);
            });
        }catch(NullPointerException e){
           // e.printStackTrace();
            //LOGGER.warn(">>> No processor state for contract " + id);
        }

    }

    public void receiveHistoricalDataFromIB(int reqId, Bar bar){
        if(bar.close()!= lastClose) {
            Generator generator = appController.getGenerators().get(Long.valueOf(reqId));
            LocalDateTime localDateTime = LocalDateTime.parse(bar.time(), formatter);
            ZoneOffset zoneOffSet = Global.ZONE_ID.getRules().getOffset(localDateTime);
            int [] frequencies = new int[]{0, 1, 5, 15, 60, 240, 480};
            Tick tick1 = new Tick(global.getIdTick(true), reqId, localDateTime.atOffset(zoneOffSet), bar.open());
            generator.processPrice(tick1,false, true,true,true, frequencies);
            Tick tick2 = new Tick(global.getIdTick(true), reqId,localDateTime.atOffset(zoneOffSet), bar.high());
            generator.processPrice(tick2,false, true,true,true,frequencies);
            Tick tick3 = new Tick(global.getIdTick(true), reqId,localDateTime.atOffset(zoneOffSet), bar.low());
            generator.processPrice(tick3,false, true,true,true,frequencies);
            Tick tick4 = new Tick(global.getIdTick(true), reqId, localDateTime.atOffset(zoneOffSet), bar.close());
            generator.processPrice(tick4,false, true,true,true,frequencies);


            System.out.println("HistoricalData. " + reqId + " - Time: " + bar.time() + ", Open: " + bar.open() + ", High: " + bar.high() + ", Low: " + bar.low()
                    + ", Close: " + bar.close() + ", Volume: " + bar.volume() + ", Count: " + bar.count() + ", WAP: " + bar.wap());
            lastClose = bar.close();
        }
    }

    public void receiveHistoricalDataFromIBEnd(){
       System.out.println("historical end");
    }

    private void loadHistoricalFromIB(Generator generator, int freq) {
        long idcontract = generator.getContract().getIdcontract();
        if (ordersIB.get(idcontract) == null || !ordersIB.get(idcontract)) {
            ordersIB.put(idcontract, true);
            dataService.reqHistoricalData(generator.getContract());
            LOGGER.info("No data for contract " + idcontract + " - freq " + freq + " >> IB Request has been placed");
        } else {
            LOGGER.info("request placed, please wait for " + idcontract + " - freq " + freq);
        }
    }

    private void loadHistoricalFromEODHistorical(String code) {
        String query ="https://eodhistoricaldata.com/api/eod/" + code + ".US?api_token=5ebab62db1bc49.83130691&from=2005-01-01&to=" + LocalDate.now() +"&fmt=json";
        System.out.println(query);
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(query))
                .GET()
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    System.out.println("Analysing EOD results");
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        long maxId =contractController.getLastIdContract(contractController.getDailyContracts());
                        JsonNode dataNode = objectMapper.readTree(res.body());
                        if(contractController.getDailyContractsBySymbol().get(code)==null) {
                            ContractBasic newContract = new ContractBasic(++maxId, "", "STK", "SMART", "USD", code, 0.02, 2, "1", null, null, "TRADES", 0, "ETF");
                            newContract.setType("DAILYCON");
                            contractController.getDailyContractsBySymbol().put(code, newContract);
                            contractController.getDailyContracts().add(newContract);
                            contractController.saveContract(newContract);
                            LOGGER.info("Saving new contract " + code);
                        }
                        ContractBasic contract = contractController.getDailyContractsBySymbol().get(code);
                        Generator generator = appController.getGenerators().get(contract.getIdcontract());
                        //Generator generator = contractController.createGenerator(contract);
                        // appController.loadHistoricalData(generator,1);
                        int[] frequencies = new int[]{1380, 6900, 35000, 100000, 300000};
                        for (JsonNode node : dataNode) {
                            double multiplier = node.get("adjusted_close").asDouble() / node.get("close").asDouble();
                            Tick tick1 = new Tick(global.getIdTick(true), contract.getIdcontract(), LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).toOffsetDateTime(), Utils.round(node.get("open").asDouble()*multiplier,contract.getRounding()));
                            generator.processPrice(tick1,false, true,true,true,frequencies);
                            Tick tick2 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(1).toOffsetDateTime(), Utils.round(node.get("high").asDouble()*multiplier,contract.getRounding()));
                            generator.processPrice(tick2,false, true,true,true, frequencies);
                            Tick tick3 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(2).toOffsetDateTime(), Utils.round(node.get("low").asDouble()*multiplier,contract.getRounding()));
                            generator.processPrice(tick3,false, true,true,true, frequencies);
                            Tick tick4 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(3).toOffsetDateTime(), Utils.round(node.get("close").asDouble()*multiplier,contract.getRounding()));
                            generator.processPrice(tick4,false, true,true,true, frequencies);
                        }
                       // generator.getDatabase().getSaverController().saveNow(generator, true);
                    } catch (NullPointerException | IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("EOD Completed");
                    return res;
                });
    }




    public List<Candle> loadHistoricalData(long idcontract, String code, int freq, int numCandles, long maxIdCandle, boolean setCandles, HistoricalDataType type) throws ExecutionException, InterruptedException {
        CompletableFuture<List<Candle>> candles = new CompletableFuture<>();
        Generator generator =null;
        sempahore.acquire();
    //    synchronized (this) {
            if (appController.getGenerators().get(idcontract) != null) {
             try {
                 if (appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow().size() > 0) {
                    // LOGGER.info("Historical data from memory for contract " + idcontract + " - freq " + freq);
                     candles.complete(appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow());
                     sempahore.release();
                     return candles.get();
                 }
             } catch (NullPointerException e) {
             }
         } else {
                    generator = appController.createGenerator(contractController.getDailyContractsBySymbol().get(code), true);
                 LOGGER.info("Generator missing >>> Creation du generator for contract " + idcontract + " - freq " + freq);
         }
        switch (type) {
            case DATABASE:
                candles.complete(loadHistoricalFromDBB(idcontract, numCandles, freq, maxIdCandle,setCandles));
              //  LOGGER.info("Historical data from database for contract " + idcontract+ " - freq " + freq);
                break;
            case IB:
                if(generator!=null)
               loadHistoricalFromIB(generator, freq);
                candles.complete(new ArrayList<>());
                break;
            case WEBSITE:
                    if (appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow().size() == 0) {
                        if (ordersEOD.get(idcontract) == null || !ordersEOD.get(idcontract)) {
                            ordersEOD.put(idcontract, true);
                            loadHistoricalFromEODHistorical(code);
                            LOGGER.info("No data for contract " + idcontract + " - freq " + freq + " >> EOD Request has been placed");
                            } else {
                            LOGGER.info("request placed, please wait for " + idcontract + " - freq " + freq);
                        }
                        candles.complete(new ArrayList<>());
                    }
                break;
            case NONE:
                candles.complete(loadHistoricalFromDBB(2, numCandles, freq, maxIdCandle,setCandles));
                //LOGGER.info("No historical data selected for contract " + idcontract+ " - freq " + freq);
                break;
        }
            sempahore.release();
        return candles.get();
   //  }
    }

//    private List<Candle> getCandlesByIdContractByFreq(long idcontract, String code, int freq, Long maxIdCandle, boolean clone, int numCandles) {
//        List<Candle> candles = new ArrayList<>();
//        try {
//            if (appController.getGenerators().get(idcontract) != null) {
//                if (appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow().size() > 0) {
//                    candles = appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow();
//                } else {
//                    candles = appController.getGenerators().get(idcontract).getDatabase().getHistoricalCandles(idcontract, freq, maxIdCandle, clone, numCandles);
//                    if (candles.size() > 0) {
//                        appController.getGenerators().get(idcontract).getProcessors().get(freq).setFlow(candles);
//                    }
//                }
//            } else {
//                contractController.createGenerator(contractController.getDailyContractsBySymbol().get(code));
//                //   contractController.createProcessor(contractController.getDailyContractsBySymbol().get(code));
//            }
//        } catch (NullPointerException e) {
//            return candles;
//        }
//        return candles;
//    }


    public void computeTicks(Generator generator, long minIdTick) {
       // LOGGER.info("Computing ticks");
        long id = generator.getContract().getCloneid() < 0 ? generator.getContract().getIdcontract() : generator.getContract().getCloneid();
        generator.getDatabase().getTicksByTable(id, "public.tick", minIdTick, false);
    }

    public void computingDeepHistorical(long idcontract, Long idTick, boolean clone) {
      //  LOGGER.info("computing historical");
        Database tickDatabase = appController.createDatabase("cleanm", Global.PORT, "atmuser");
        long minIdTick = idTick == null ? 0 : idTick;
        int count = 0;

        while (minIdTick >= 0) {
            count++;
            minIdTick = tickDatabase.getTicksByTable(idcontract, "trading.data18", minIdTick, clone);
        }
        minIdTick = idTick == null ? 0 : idTick;
        count = 0;
        while (minIdTick >= 0) {
            count++;
            minIdTick = tickDatabase.getTicksByTable(idcontract, "trading.data19", minIdTick, clone);
        }
        minIdTick = idTick == null ? 0 : idTick;
        count = 0;
        while (minIdTick >= 0) {
            count++;
            minIdTick = tickDatabase.getTicksByTable(idcontract, "trading.data20", minIdTick, clone);
        }
        tickDatabase.close();

    }

}
