package io.matel.app.controller;


import io.matel.app.AppController;
import io.matel.app.AppLauncher;
import io.matel.app.config.Global;
import io.matel.app.config.connection.activeuser.ActiveUserEvent;
import io.matel.app.config.connection.user.UserRepository;
import io.matel.app.config.tools.MailService;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.state.GeneratorState;
import io.matel.app.state.ProcessorState;
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
    ContractController contractController;

    @Autowired
    MailService mailService;


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

    @GetMapping("/contracts/{type}/{category_}/{filter}")
    public List<ContractBasic> getContracts(@PathVariable String type, @PathVariable String category_ ,@PathVariable String filter){
        List<ContractBasic> contracts = null;
        final String category = category_.toUpperCase();
        System.out.println(category);
        if(category_.equals("DAILY")){
            if(contractController.getDailyContracts().size()>0){
                contracts = contractController.getDailyContracts().stream().filter(con -> con.getCategory().equals(category)).filter(name->name.getSymbol().startsWith(filter.toUpperCase())).limit(10).collect(Collectors.toList());
            System.out.println(contracts.size());
            }else {
//                contracts = contractController.findByActiveAndTypeOrderByIdcontract(true, type).stream().filter(con -> con.getCategory().equals(category)).collect(Collectors.toList());
            }
        }else{
            if(contractController.getContracts().size()>0){
                contracts = contractController.getContracts().stream().filter(con -> con.getCategory().equals(category)).collect(Collectors.toList());
            }else {
                contracts = contractController.findByActiveAndTypeOrderByIdcontract(true, type).stream().filter(con -> con.getCategory().equals(category)).collect(Collectors.toList());
            }
        }

        LOGGER.info("Sending (" + contracts.size() + ") contracts " + type );
        return contracts;
    }


    @GetMapping("/histo-candles/{id}/{code}/{frequency}")
    public List<Candle> getHistoricalCandles(@PathVariable String id, @PathVariable String code, @PathVariable String frequency){
        long idcontract = Long.valueOf(id);
        int freq = Integer.valueOf(frequency);
        return appController.getCandlesByIdContractByFreq(idcontract, code, freq,null,false, Global.MAX_LENGTH_CANDLE);
    }


    @GetMapping("/quote-histo/{id}")
    public GeneratorState getGeneratorState(@PathVariable String id){
        long idcontract = Long.valueOf(id);
        return appController.getGenerators().get(idcontract).getGeneratorState();
    }

    @GetMapping("/processor-state/{id}")
    public List<ProcessorState> getProcessorStates(@PathVariable String id){
        long idcontract = Long.valueOf(id);
        System.out.println(appController.getGenerators().get(idcontract).getDatabase().getSaverController().getProcessorStateBuffer().size());
        return appController.getGenerators().get(idcontract).getDatabase().getSaverController().getProcessorStateBuffer();
    }

    @PostMapping("/disconnect-all/{save_}")
    public void disconnectAll(@PathVariable String save_){
        boolean save = Boolean.valueOf(save_);
        appController.disconnectAllMarketData(save);
    }

    @PostMapping("/send-email")
    public void sendEmail(){
        mailService.sendMessage(null, null);
    }

    @PostMapping("/contracts/clone/{id}")
    public void cloneContract(@PathVariable String id){
        int idcontract = Integer.valueOf(id);
        contractController.cloneContract(idcontract);
    }

    @PostMapping("/contracts/remove/{id}")
    public void removeContract(@PathVariable String id){
        int idcontract = Integer.valueOf(id);
        contractController.removeContract(idcontract);
    }

    @PostMapping("/connect-all")
    public void connectALL(){
        appController.connectAllMarketData();
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

    @PostMapping("/speed-multiplier/{id}/{_speed}")
    public void increaseSpeedMultiplier(@PathVariable String id, @PathVariable String _speed){
        long idcontract = Long.valueOf(id);
        int speed = Integer.valueOf(_speed);
        appController.getGenerators().get(idcontract).getGeneratorState().setSpeedMultiplier(speed);
    }

    @PostMapping("/activate-email")
    public void setupGlobalAlert(@RequestBody String _email){
        boolean email = Boolean.valueOf(_email);
        Global.send_email = email;
    }


    @PostMapping("/save-contract/{adj}")
    public void saveContract(@RequestBody ContractBasic _contract, @PathVariable String adj){
        double adjustment = Double.valueOf(adj);
        contractController.saveContract(_contract);
        appController.getGenerators().get(_contract.getIdcontract()).setContract(_contract);
        contractController.initContracts(false);
        if(adjustment !=0){
            appController.saveNow();
            int result = appController.getDatabase().updateCandles(_contract.getIdcontract(), adjustment);
            mailService.sendMessage("(" +_contract.getIdcontract() +") " + _contract.getSymbol() + " adjustment",
                    "UPDATE trading.data20 set close = close + " + adjustment +";", true);
            LOGGER.info("Maintenance update database for contract " + _contract.getIdcontract() +" >>> " + result + " entries");
        }
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
