package io.matel.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.matel.app.config.Global;
import io.matel.app.config.tools.Utils;
import io.matel.app.controller.ContractController;
import io.matel.app.controller.HistoricalDataController;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DailyCompute {

    @Autowired
    ContractController contractController;

    @Autowired
    AppController appController;
    @Autowired
    Global global;

    @Autowired
    AppLauncher appLauncher;

    @Autowired
    HistoricalDataController historicalDataController;

    private Semaphore semaphore = new Semaphore(10);
    private ExecutorService executor = Executors.newFixedThreadPool(100);


    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public void EODByCode(String code) {

        //List<Daily> dailyList = new ArrayList<>();
        String query ="https://eodhistoricaldata.com/api/eod/" + code + ".US?api_token=5ebab62db1bc49.83130691&to=2020-06-04&fmt=json";
        System.out.println(query);
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(query))
                .GET()
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                   // executor.execute(()->{
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        System.out.println(res.headers().toString());
                        long maxId =contractController.getLastIdContract(contractController.getDailyContracts());
                        JsonNode dataNode = objectMapper.readTree(res.body());
                        if(contractController.getDailyContractsBySymbol().get(code)==null) {
                            ContractBasic newContract = new ContractBasic(++maxId, "", "STK", "SMART", "USD", code, 0.02, 2, "1", null, null, "TRADES", 0, "ETF");
                            newContract.setType("DAILYCON");
                            contractController.getDailyContractsBySymbol().put(code, newContract);
                            contractController.getDailyContracts().add(newContract);
                            contractController.saveContract(newContract);
                            System.out.println("Saving contract");
                        }
                        ContractBasic contract = contractController.getDailyContractsBySymbol().get(code);
                        Generator generator = appController.createGenerator(contract, true);
                        //contractController.createProcessor(contract);
                       // appController.loadHistoricalData(generator,1);
                        int[] frequencies = new int[]{1380, 6900, 35000, 100000, 300000};
                        for (JsonNode node : dataNode) {
                           // dailyList.add(new Daily(code,node, false));
                                double multiplier = node.get("adjusted_close").asDouble() / node.get("close").asDouble();
                                Tick tick1 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).toOffsetDateTime(), Utils.round(node.get("open").asDouble()*multiplier,contract.getRounding()));
                                generator.processPrice(tick1,false, true, true,false, frequencies);
                                Tick tick2 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(1).toOffsetDateTime(), Utils.round(node.get("high").asDouble()*multiplier,contract.getRounding()));
                                generator.processPrice(tick2,false, true,true,false, frequencies);
                                Tick tick3 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(2).toOffsetDateTime(), Utils.round(node.get("low").asDouble()*multiplier,contract.getRounding()));
                                generator.processPrice(tick3,false, true,true,false, frequencies);
                                Tick tick4 = new Tick(global.getIdTick(true), contract.getIdcontract(),LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(3).toOffsetDateTime(), Utils.round(node.get("close").asDouble()*multiplier,contract.getRounding()));
                                generator.processPrice(tick4,false, true,true,false, frequencies);
                        }
                        generator.getDatabase().getSaverController().saveNow(generator, true);
                        generator.getDatabase().close();
                        appController.getGenerators().remove(generator);
                    } catch (IOException  | NullPointerException e) {
                        e.printStackTrace();
                        System.out.println(query);
                        appLauncher.releaseSemaphore();
                    }
                  //  });
                    appLauncher.releaseSemaphore();
                    return res;

                });

    }


    public void EODByExchange(String date) {
        List<Daily> dailyList = new ArrayList<>();
        String query = "https://eodhistoricaldata.com/api/eod-bulk-last-day/US?api_token=5ebab62db1bc49.83130691&date=" + date + "&fmt=json&filter=extended";
        System.out.println(query);

        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(query))
                .GET()
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode dataNode = objectMapper.readTree(res.body());
                        AtomicLong maxId = new AtomicLong(contractController.getLastIdContract(contractController.getDailyContracts()));
                        for (JsonNode node : dataNode) {
                            String code = node.get("code").asText();
                           // if (code.equals("AMZN")) {
                                if (contractController.getDailyContractsBySymbol().get(code) == null) {
                                } else {
                                    try {
                                        semaphore.acquire();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    executor.execute(()->{
                               //     new Thread(() -> {
                                        dailyList.add(new Daily(code, node, true));
//                                    if (contractController.getDailyContractsBySymbol().get(code) == null) {
//                                        ContractBasic newContract = new ContractBasic(maxId.incrementAndGet(), "", "STK", "SMART", "USD", code, 0.02, 2, "1", null, null, "TRADES", 0, "ETF");
//                                        newContract.setType("DAILYCON");
//                                        contractController.getDailyContractsBySymbol().put(code, newContract);
//                                        contractController.getDailyContracts().add(newContract);
//                                        contractController.saveContract(newContract);
//                                    }
                                        ContractBasic contract = contractController.getDailyContractsBySymbol().get(code);
                                        Generator generator = appController.createGenerator(contract, false);
                                        generator.setDatabase(appController.getDatabase());
                                       // contractController.createProcessor(contract);
                                        //historicalDataController.loadHistoricalData(generator, 100,1);
                                        int[] frequencies = new int[]{1380, 6900, 35000, 100000, 300000};
                                        double multiplier = node.get("adjusted_close").asDouble() / node.get("close").asDouble();
                                        Tick tick1 = new Tick(global.getIdTick(true), contract.getIdcontract(), LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).toOffsetDateTime(), node.get("open").asDouble() * multiplier);
                                        tick1.setVolume((int) node.get("volume").asInt()/4);
                                        generator.processPrice(tick1, false, true,true,false, frequencies);
                                        Tick tick2 = new Tick(global.getIdTick(true), contract.getIdcontract(), LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(1).toOffsetDateTime(), node.get("high").asDouble() * multiplier);
                                        tick2.setVolume((int) node.get("volume").asInt()/4);
                                        generator.processPrice(tick2, false, true,true,false, frequencies);
                                        Tick tick3 = new Tick(global.getIdTick(true), contract.getIdcontract(), LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(2).toOffsetDateTime(), node.get("low").asDouble() * multiplier);
                                        tick3.setVolume((int) node.get("volume").asInt()/4);
                                        generator.processPrice(tick3, false, true,true,false, frequencies);
                                        Tick tick4 = new Tick(global.getIdTick(true), contract.getIdcontract(), LocalDate.parse(node.get("date").asText()).atStartOfDay(Global.ZONE_ID).plusSeconds(3).toOffsetDateTime(), node.get("close").asDouble() * multiplier);
                                        tick4.setVolume((int) node.get("volume").asInt()/4);
                                        generator.processPrice(tick4, false, true,true,false, frequencies);
                                        generator.getDatabase().getSaverController().saveNow(generator, true);
                                        semaphore.release();
                                 //   }).start();
                                    });
                                }
                        //    }
                        }
                    } catch (IOException  | NullPointerException e) {
                        e.printStackTrace();
                    }
                System.out.println("EOD batch completed");
                    return res;
                });
    }


}