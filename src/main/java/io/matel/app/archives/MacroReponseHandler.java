//package io.matel.app;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class MacroReponseHandler {
//    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);
//    private DailyRepo dailyRepo;
//
//    public MacroReponseHandler(DailyRepo dailyRepo){
//        this.dailyRepo = dailyRepo;
//    }
//
//
//    public void processResponse(String json) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        String code = objectMapper.readTree(json).get("dataset").get("dataset_code").asText();
//        LOGGER.info("Receiving response for code " + code);
//        JsonNode dataNode = objectMapper.readTree(json).get("dataset").path("data");
//        Map<LocalDate, MacroData> map = new ConcurrentHashMap<>();
//
//        for (JsonNode node : dataNode) {
//            MacroData macroData = new MacroData(code, node.get(0).asText(), node.get(1).asText());
//          //  map.put(macroData.getDate(), macroData);
//        }
//
//        List<MacroData> data = dailyRepo.findByCode(code);
//        for (MacroData d : data) {
//           // if (map.get(d.getDate()) != null) {
//            //    if (d.getValue() == map.get(d.getDate()).getValue()) {
//             //       map.remove(d.getDate());
//              //  }
//           // }
//        }
//
//        List<MacroData> newItems = new ArrayList<>();
//        map.forEach((date, value) -> {
//            newItems.add(value);
//        });
//
//        LOGGER.info("Exclusive new items " + newItems.size());
//        if (newItems.size() > 0) {
//            synchronized (this) {
//                dailyRepo.saveAll(newItems);
//            }
//        }
//
//    }
//}