package io.matel.app.controller;


import io.matel.app.AppController;
import io.matel.app.AppLauncher;
import io.matel.app.GeneratorState;
import io.matel.app.connection.activeuser.ActiveUserEvent;
import io.matel.app.connection.user.UserRepository;
import io.matel.app.domain.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@CrossOrigin
public class HttpController {

    private static final Logger LOGGER = LogManager.getLogger(ActiveUserEvent.class);


//    private UserRepository userRepository;
//    private PasswordEncoder passwordEncoder;
    private WsController wsController;
    private AppController appController;
    private SaverController saverController;

    @Autowired
    CandleRepository candleRepository;

    @Autowired
    TickRepository tickRepository;

    @Autowired
    ContractRepository contractRepository;


    public HttpController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          WsController wsController,
                          AppController appController,
                          SaverController saverController,
                          AppLauncher appLauncher) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
        this.wsController = wsController;
        this.appController = appController;
        this.saverController = saverController;
    }

    @GetMapping("/contracts")
    public List<ContractBasic> getContracts(){
        List<ContractBasic> contracts = contractRepository.findByActive(true);
        LOGGER.info("Sending (" + contracts.size() + ") contracts" );
        return contracts;
    }

    @GetMapping("/histo-candles/{id}/{frequency}")
    public List<Candle> getHistoricalCandles(@PathVariable String id, @PathVariable String frequency){
        long idcontract = Long.valueOf(id);
        int freq = Integer.valueOf(frequency);
        if (freq == 1)
            saverController.saveNow(idcontract);
        List<Candle> candles = candleRepository.findTop100ByIdcontractAndFreqOrderByTimestampDesc(idcontract, freq);
//        LOGGER.info("Sending historical prices for contract (" + idcontract + ") - freq: " + freq );
        return candles;
    }

    @PostMapping("/contract/{id}/{factor}")
    public boolean updateContract(@PathVariable String id, @PathVariable String factor, @RequestBody ContractBasic con) throws InterruptedException {
        long idcontract = Long.valueOf(id);
        double adjfactor = Double.valueOf(factor);
        appController.getGenerators().get(idcontract).disconnectMarketData(true);
        ContractBasic contract = appController.getGenerators().get(idcontract).getContract();
        contract.setExpiration(con.getExpiration());
        contract.setFirstNotice(con.getFirstNotice());
        System.out.println(contract.toString());
        contractRepository.save(contract);
        saverController.saveNow(contract.getIdcontract());
        Thread t1 = new Thread(()->{
            String message = "Updating ticks (" + idcontract + ") with factor: " + adjfactor;
            Ticket ticket = new Ticket(message, "pending");
            LOGGER.info(message);
            wsController.sendNotification(ticket);
            tickRepository.updateHistoricalTicks(adjfactor, idcontract);
            message = "Ticks updated (" + idcontract + ") with factor: " + adjfactor;
            ticket = new Ticket(message, "completed");
            LOGGER.info(message);
            wsController.sendNotification(ticket);
        });

        t1.start();

       Thread t2 = new Thread(()->{
            String message = "Updating candles (" + idcontract + ") with factor: " + adjfactor;
            Ticket ticket = new Ticket(message, "pending");
            LOGGER.info(message);
            wsController.sendNotification(ticket);
           candleRepository.updateHistoricalCandles(adjfactor, idcontract);
            message = "Candles updated (" + idcontract + ") with factor: " + adjfactor;
            ticket = new Ticket(message, "completed");
            LOGGER.info(message);
            wsController.sendNotification(ticket);
        });

        t2.start();

        t1.join();
        t2.join();
        appController.loadHistoricalCandles(idcontract,true);
        LOGGER.info("Task completed");
        return true;
    }

    @PostMapping("/connect/{id}")
    public Map<Long, GeneratorState> connectMarketData(@PathVariable String id, @RequestBody String connect) throws ExecutionException, InterruptedException {
        long idcontract = Long.valueOf(id);
        saverController.saveBatchTicks();
        if(Boolean.valueOf(connect)) {
            LOGGER.info("Connect data for contract " + idcontract);
            appController.getGenerators().get(idcontract).connectMarketData();
        }else{
            LOGGER.info("Disconnect data for contract " + idcontract);
            appController.getGenerators().get(idcontract).disconnectMarketData(true);
        }
        return appController.getGeneratorsState();
    }

    //    @PostMapping("/register")
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