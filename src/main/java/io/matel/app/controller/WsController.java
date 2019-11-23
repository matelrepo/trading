package io.matel.app.controller;

import io.matel.app.GeneratorState;
import io.matel.app.domain.Candle;
import io.matel.app.domain.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
public class WsController {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendLiveCandle(Candle candle) {
        if (candle != null) {
            String path = ("/get/candle-live/" + candle.getIdcontract() +"/" +candle.getFreq());
             template.convertAndSend(path, candle);
        }
    }

    public void sendPrices(Map<Long, GeneratorState> states) {
        if (states != null) {
            String path = ("/get/prices");
            template.convertAndSend(path, states);
        }
    }

    public void sendNotification(Ticket ticket){
        String path = ("/get/notifications");
        template.convertAndSend(path, ticket);
    }

}