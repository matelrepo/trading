package io.matel.app.controller;


import io.matel.app.AppController;
import io.matel.app.AppLauncher;
import io.matel.app.config.Global;
import io.matel.app.config.connection.activeuser.ActiveUserEvent;
import io.matel.app.config.connection.user.UserRepository;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.repo.ContractRepository;
import io.matel.app.state.GeneratorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class HttpController {

    private static final Logger LOGGER = LogManager.getLogger(ActiveUserEvent.class);
    private WsController wsController;
    private AppController appController;

    @Autowired
    ContractRepository contractRepository;


    @Autowired
    Global global;



    public HttpController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          WsController wsController,
                          AppController appController,
                          AppLauncher appLauncher) {
        this.wsController = wsController;
        this.appController = appController;
    }

    @GetMapping("/contracts/{type}/{category_}")
    public List<ContractBasic> getContracts(@PathVariable String type, @PathVariable String category_ ){
        List<ContractBasic> contracts = null;
        final String category = category_.toUpperCase();
        System.out.println(category);
        if(appController.getContractsLive().size()>0){
            contracts = appController.getContractsLive().stream().filter(con -> con.getCategory().equals(category)).collect(Collectors.toList());
        }else {
            contracts = contractRepository.findByActiveAndTypeOrderByIdcontract(true, type).stream().filter(con -> con.getCategory().equals(category)).collect(Collectors.toList());
        }
        LOGGER.info("Sending (" + contracts.size() + ") contracts " + type );
        return contracts;
    }


    @GetMapping("/histo-candles/{id}/{frequency}")
    public List<Candle> getHistoricalCandles(@PathVariable String id, @PathVariable String frequency){
        long idcontract = Long.valueOf(id);
        int freq = Integer.valueOf(frequency);
        return appController.getCandlesByIdContractByFreq(idcontract, freq);
    }


    @GetMapping("/quote-histo/{id}")
    public GeneratorState getGeneratorState(@PathVariable String id){
        long idcontract = Long.valueOf(id);
        return appController.getGenerators().get(idcontract).getGeneratorState();
    }

    @PostMapping("/disconnect-all/{save_}")
    public void disconnectAll(@PathVariable String save_){
        boolean save = Boolean.valueOf(save_);
        appController.disconnectAllMarketData(save);
    }

    @PostMapping("/connect-all")
    public void connectALL(){
        System.out.println("coucou");
        appController.connectAllMarketData();
    }

    @PostMapping("/contract/{id}/{factor}")
    public boolean updateContract(@PathVariable String id, @PathVariable String factor, @RequestBody ContractBasic con) throws InterruptedException {
//        long idcontract = Long.valueOf(id);
//        double adjfactor = Double.valueOf(factor);
//        appController.getGenerators().get(idcontract).disconnectMarketData(true);
//        ContractBasic contract = appController.getGenerators().get(idcontract).getContract();
//        contract.setExpiration(con.getExpiration());
//        contract.setFirstNotice(con.getFirstNotice());
//        System.out.println(contract.toString());
//        contractRepository.save(contract);
//        saverController.saveNow(contract.getIdcontract());
//        Thread t1 = new Thread(()->{
//            String message = "Updating ticks (" + idcontract + ") with factor: " + adjfactor;
//            Ticket ticket = new Ticket(message, "pending");
//            LOGGER.info(message);
//            wsController.sendNotification(ticket);
//            tickRepository.updateHistoricalTicks(adjfactor, idcontract);
//            message = "Ticks updated (" + idcontract + ") with factor: " + adjfactor;
//            ticket = new Ticket(message, "completed");
//            LOGGER.info(message);
//            wsController.sendNotification(ticket);
//        });
//
//        t1.start();
//
//       Thread t2 = new Thread(()->{
//            String message = "Updating candles (" + idcontract + ") with factor: " + adjfactor;
//            Ticket ticket = new Ticket(message, "pending");
//            LOGGER.info(message);
//            wsController.sendNotification(ticket);
//           candleRepository.updateHistoricalCandles(adjfactor, idcontract);
//            message = "Candles updated (" + idcontract + ") with factor: " + adjfactor;
//            ticket = new Ticket(message, "completed");
//            LOGGER.info(message);
//            wsController.sendNotification(ticket);
//        });
//
//        t2.start();
//
//        t1.join();
//        t2.join();
//        appController.loadHistoricalCandles(idcontract,true);
//        LOGGER.info("Task completed");
        return true;
    }

    @PostMapping("/connect/{id}")
    public Map<Long, GeneratorState> connectMarketData(@PathVariable String id, @RequestBody String connect) throws ExecutionException, InterruptedException {
        long idcontract = Long.valueOf(id);
        appController.getDatabase().getSaverController().saveBatchTicks(true);
        if(Boolean.valueOf(connect)) {
            LOGGER.info("Connect data for contract " + idcontract);
            appController.getGenerators().get(idcontract).connectMarketData();
        }else{
            LOGGER.info("Disconnect data for contract " + idcontract);
            appController.getGenerators().get(idcontract).disconnectMarketData(true);
        }
        return appController.getGeneratorsState();
    }

    @PostMapping("/speed/{id}/{_speed}")
    public void increaseSpeed(@PathVariable String id, @PathVariable String _speed){
        long idcontract = Long.valueOf(id);
        int speed = Integer.valueOf(_speed);
        appController.getGenerators().get(idcontract).getGeneratorState().setSpeed(speed);
    }

    @PostMapping("/activate-email")
    public void setupGlobalAlert(@RequestBody String _email){
        boolean email = Boolean.valueOf(_email);
        Global.send_email = email;
    }
//
//    @PostMapping("/event-alert/{_id}/{_freq}/{type}")
//    public void setupProcessorAlert(@PathVariable String _id, @PathVariable String _freq, @PathVariable String type, @RequestBody String _alert){
//        long idcontract = Long.valueOf(_id);
//        int freq = Integer.valueOf(_freq);
//        boolean alert = Boolean.valueOf(_alert);
//        EventType eventType = EventType.valueOf(type);
//        appController.getGenerators().get(idcontract).getProcessors().get(freq).getAlertConfig().put(eventType, alert);
//    }
//
//    @PostMapping("event-alert/big/{_id}/{_freq}")
//    public void setupBigCandleAlert(@PathVariable String _id, @PathVariable String _freq,@RequestBody String _alert){
//        long idcontract = Long.valueOf(_id);
//        int freq = Integer.valueOf(_freq);
//        boolean alert = Boolean.valueOf(_alert);
//        appController.getGenerators().get(idcontract).getProcessors().get(freq).setAlertAudioBigVolume(alert);
//    }
//
//    @PostMapping("event-alert/big/{_id}/all")
//    public void setupBigCandleAlertAllFreq(@PathVariable String _id,@RequestBody String _alert){
//        final long idcontract = Long.valueOf(_id);
//        final boolean alert = Boolean.valueOf(_alert);
//        appController.getGenerators().get(idcontract).getProcessors().forEach((freq, processor) -> {
//            if(freq>1){
//                processor.setAlertAudioBigVolume(alert);
//            }
//        });
//    }


//        @PostMapping("/register")
//    public ResponseEntity register(@RequestBody UserView model) {
////        System.out.println(model.getUsername());
////        System.out.println(this.userRepository.findByUsername(model.getUsername()));
//        if (this.userRepository.findByUsername(model.getUsername()) == null) {
//            User user = new User(model.getUsername(), passwordEncoder.encode(model.getPassword()),"STUDENT","");
//            this.userRepository.save(user);
//            return new ResponseEntity(new CustomHttpResponse("Registration Succesfull!", HttpStatus.OK.value()), HttpStatus.OK);
//        } else {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User already exists");        }
//    }
}
