package io.matel.security.controller;

import java.util.List;
import java.util.Map;

import io.matel.trader.domain.Candle;
import io.matel.trader.domain.ContractBasic;
import io.matel.trader.domain.Tick;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
public class WebControllerOut {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendContracts(Map<Integer, ContractBasic> contracts) {
        if (contracts != null) {
            String path = ("/get/contracts/all");
            template.convertAndSend(path, contracts);
        }
    }


    // PRICES
    public void sendLiveFlow(Tick candle) {
        if (candle != null) {
            String path = ("/get/live");
//            System.out.println(path);
            template.convertAndSend(path, candle);
//		Messenger.log("Sending live flow - " + path, MessagePriority.LOW, MessageGroup.WEBOUT, idcontract, freq);
        }
    }

    public void sendHistoFlowByContract(List<Candle> candles, int idcontract, int freq) {
        if (candles != null)
            if (candles.size() > 0) {
                String path = ("/get/histo/" + idcontract + "/" + freq);
                template.convertAndSend(path, candles);
            }
    }

}
