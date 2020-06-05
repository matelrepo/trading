package io.matel.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


    private Map<Long, Long> processorStatesIdTickByIdContract;

    public void findProcessorStateByContract() {
        processorStatesIdTickByIdContract= appController.getDatabase().findTopIdTickFromProcessorState();
        System.out.println(processorStatesIdTickByIdContract.size());
    }

    public Map<Long, Long> getProcessorStatesIdTickByIdContract() {
        return processorStatesIdTickByIdContract;
    }

    private List<Candle> loadHistoricalFromDBB(long idcontract, int numCandles, int freq, long maxIdCandle, boolean setCandles) {
        List<Candle> candles;
        Generator generator = appController.getGenerators().get(idcontract);
        ContractBasic contract = generator.getContract();
        long id = contract.getCloneid() < 0 ? contract.getIdcontract() : contract.getCloneid();
        candles = appController.getGenerators().get(idcontract).getDatabase().getHistoricalCandles(idcontract, freq, maxIdCandle, contract.getCloneid() > 0, numCandles);
        if (setCandles || generator.getProcessors().get(freq).getFlow().size()==0) {
            generator.getProcessors().get(freq).setFlow(candles);

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
            e.printStackTrace();
            LOGGER.warn(">>>no processor state for contract " + id);
        }

    }

    private void loadHistoricalFromIB(long idcontract) {
        dataService.reqHistoricalData(appController.getGenerators().get(idcontract).getContract());
    }

    private void loadHistoricalFromEODHistorical(String code) {
        String query ="https://eodhistoricaldata.com/api/eod/" + code + ".US?api_token=5ebab62db1bc49.83130691&from=2010-01-01&to=2020-05-28&fmt=json";
        System.out.println(query);
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(query))
                .GET()
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    System.out.println("Analysing results");
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
                        for (JsonNode node : dataNode) {
                            double multiplier = node.get("adjusted_close").asDouble() / node.get("close").asDouble();
                          //  Tick tick1 = new Tick(global.getIdTick(true), contract.getIdcontract(), LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID), Utils.round(node.get("open").asDouble()*multiplier,contract.getRounding()));
                          //  generator.processPrice(tick1,false, true,true,true);
                        //    Tick tick2 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(1), Utils.round(node.get("high").asDouble()*multiplier,contract.getRounding()));
                          //  generator.processPrice(tick2,false, true,true,true);
                         //   Tick tick3 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(2), Utils.round(node.get("low").asDouble()*multiplier,contract.getRounding()));
                          //  generator.processPrice(tick3,false, true,true,true);
                          //  Tick tick4 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(3), Utils.round(node.get("close").asDouble()*multiplier,contract.getRounding()));
                           // generator.processPrice(tick4,false, true,true,true);
                        }
                       // generator.getDatabase().getSaverController().saveNow(generator, true);
                    } catch (NullPointerException | IOException e) {
                        e.printStackTrace();
                    }
                    return res;
                });
    }


    public  CompletableFuture<List<Candle>> loadHistoricalData(long idcontract, String code, int freq, int numCandles, long maxIdCandle, boolean setCandles, HistoricalDataType type) {
       //System.out.println(Thread.currentThread().getName());
        CompletableFuture<List<Candle>> candles = new CompletableFuture<>();
        if (appController.getGenerators().get(idcontract) != null) {
            //System.out.println("coucou");
            try {
                if (appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow().size() > 0) {
                    LOGGER.info("Historical data from memory for contract " + idcontract + " - freq " + freq);
                    candles.complete(appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow());
                    return candles ;
                }
            }catch(NullPointerException e){
                return candles;
            }
        }else {
            //("hey");
                appController.createGenerator(contractController.getDailyContractsBySymbol().get(code), HistoricalDataType.NONE);
                LOGGER.info("Generator missing >>> Creation du generator for contract " + idcontract + " - freq " + freq);
        }
        switch (type) {
            case DATABASE:
                candles.complete(loadHistoricalFromDBB(idcontract, numCandles, freq, maxIdCandle,setCandles));
                LOGGER.info("Historical data from database for contract " + idcontract+ " - freq " + freq);
                break;
            case IB:
               // candles = loadHistoricalFromIB(idcontract);
                break;
            case WEBSITE:
              //  synchronized(this) {
//                    if (appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow().size() == 0) {
//                        // loadHistoricalFromEODHistorical(code);
//                        if (ordersEOD.get(idcontract) == null || !ordersEOD.get(idcontract)) {
//                            LOGGER.info("No data for contract " + idcontract + " - freq " + freq + " >> Request has been placed");
//                          //  ordersEOD.put(idcontract, true);
//                        } else {
//                            LOGGER.info("request placed, please wait for " + idcontract + " - freq " + freq);
//                        }
//                    } else {
//                    }
                candles.complete(loadHistoricalFromDBB(2, numCandles, freq, maxIdCandle,setCandles));
              //  }
//                if(historicalDataType>0) {
//                    return appController.getGenerators().get(idcontract).getProcessors().get(freq).getFlow();
//                }else{
//                    return null;
//                }
                break;
            case NONE:
                candles.complete(loadHistoricalFromDBB(2, numCandles, freq, maxIdCandle,setCandles));
                LOGGER.info("No historical data selected for contract " + idcontract+ " - freq " + freq);
                break;
        }
        return candles;
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
        LOGGER.info("Computing ticks");
        long id = generator.getContract().getCloneid() < 0 ? generator.getContract().getIdcontract() : generator.getContract().getCloneid();
        generator.getDatabase().getTicksByTable(id, "public.tick", minIdTick, false);
    }

    public void simulateHistorical(long idcontract, Long idTick, boolean clone) {
        LOGGER.info("computing historical");
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
