//package io.matel.app.controller;
//
//
//import io.matel.app.domain.ContractBasic;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.stereotype.Controller;
//
//import java.util.Map;
//
//
//
//@Controller
//public class WebSocketControllerIn {
//
//    private WebSocketControllerOut WebSocketControllerOut;
//
//    public WebSocketControllerIn(WebSocketControllerOut WebSocketControllerOut) {
//        this.WebSocketControllerOut = WebSocketControllerOut;
//    }
//
//    @MessageMapping("/contracts")
//    @SendTo("/get/contracts/all")
//    public Map<Integer, ContractBasic> reqContracts() throws Exception {
////        return appController.getContracts();
//        return null;
//    }
//
//    @MessageMapping("/histo/{$idcontract}/{$freq}")
//    public void reqHistoricalFlow(@DestinationVariable String $idcontract, @DestinationVariable String $freq) {
////        try {
////            int idcontract = Integer.valueOf($idcontract);
////            int freq = Integer.valueOf($freq);
////            List<Candle> list;
////            list = appController.getCandlesByContractByFreq(idcontract, freq);
////            this.webControllerOut.sendHistoFlowByContract(list, idcontract, freq);
////        } catch (ClassNotFoundException | InterruptedException | ExecutionException | SQLException | NumberFormatException e) {
////            e.printStackTrace();
////        }
//
//    }
//
//
//}
