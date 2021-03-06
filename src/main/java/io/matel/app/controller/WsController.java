package io.matel.app.controller;

import io.matel.app.AppController;
import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.TimeSales;
import io.matel.app.state.GeneratorState;
import io.matel.app.state.ProcessorState;
import io.matel.app.config.tools.MailService;
import javazoom.jl.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class WsController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private MailService mailService;

    @Autowired
    AppController appController;

    public void sendLiveCandle(Candle candle) {
        if (candle != null) {
            String path = "/get/candle-live/" + candle.getIdcontract() + "/" + candle.getFreq();
            template.convertAndSend(path, candle);
//            if(candle.getFreq()==15) {
//                System.out.println("Websocket");
//             System.out.println(candle.toString());
//            }
        }
    }

    public void sendLiveGeneratorState(GeneratorState genState) {
        String path = ("/get/quote/" + genState.getIdcontract());
        template.convertAndSend(path, genState);
    }

    public void sendTimeSales(TimeSales timeSale) {
        String path = ("/get/timesales/" + timeSale.getIdcontract());
        template.convertAndSend(path, timeSale);
    }

    public void sendPrices(Map<Long, GeneratorState> states) {
        if (states != null) {
            String path = "/get/prices";
            template.convertAndSend(path, states);
        }
    }

    public void sendContractDetails(List<ContractBasic> contracts){
        String path = "/get/contract-details";
        System.out.println("Sending " + contracts.stream().limit(4).collect(Collectors.toList()).size());
        template.convertAndSend(path, contracts.stream().limit(4).collect(Collectors.toList()));
    }

    public void sendEvent(ProcessorState processorState, ContractBasic contract) {
        if (Global.hasCompletedLoading && contract.getIdcontract()<10000) { //10000 EOD reader from website search box
            String path = "/get/events";
            template.convertAndSend(path, processorState);
            if (processorState.isTradable()){
                if(appController.getGlobalSettings().get(contract.getIdcontract()).get(processorState.getFreq()).isEmail()){
                mailService.sendMessage(processorState, contract);
                }
                if(appController.getGlobalSettings().get(contract.getIdcontract()).get(processorState.getFreq()).isVoice()){
                    System.out.println("New event -> " + processorState.getEventType() + " " + processorState.toString());
                String test = "src/main/resources/audio/" + processorState.getFreq() + processorState.getEventType() + ".mp3";
                String test2 = "src/main/resources/audio/c" + contract.getIdcontract() + ".mp3";

                try {
                    FileInputStream fileInputStream1 = new FileInputStream("src/main/resources/audio/c" + contract.getIdcontract() + ".mp3");
                    Player player1 = new Player((fileInputStream1));
                    player1.play();
                } catch (Exception e) {
                    System.out.println(e);
                }
                try {
                    FileInputStream fileInputStream2 = new FileInputStream("src/main/resources/audio/" + processorState.getFreq() +
                            processorState.getEventType() + ".mp3");
                    Player player2 = new Player((fileInputStream2));
                    player2.play();


                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            }

//        System.out.println("Event sent " + processorState.toString());
        }
    }

}
