//package io.matel.app;
//
//import com.opencsv.CSVReader;
//import io.matel.app.config.Global;
//import io.matel.app.controller.ContractController;
//import io.matel.app.domain.Candle;
//import io.matel.app.domain.ContractBasic;
//import io.matel.app.domain.Tick;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Component
//public class MacroWriterXML {
//    private static final Logger LOGGER = LogManager.getLogger(MacroWriterXML.class);
//   // private LocalDateTime lastDateTime;
//    //private final HttpClient httpClient = HttpClient.newBuilder().build();
//
//    @Autowired
//    ContractController contractController;
//    @Autowired
//    AppController appController;
//    @Autowired
//    Global global;
//
//    private DateTimeFormatter formatterLocalDateTime1 = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
////    private DateTimeFormatter formatterLocalDateTime2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
////    private DateTimeFormatter formatterLocalDate1 = DateTimeFormatter.ofPattern("dd-MMM-yy");
////    private DateTimeFormatter formatterLocalDate2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//
//
//    public void start() throws Exception {
//        List<Candle> candles = readCsvLineByLine();
//
//    }
//
//    public List<Candle> readCsvLineByLine() throws Exception {
//      //  Path path = Paths.get("/home/matel/Documents/scripts/sge/SGE_metadata.csv");
//        Path path = Paths.get("C:\\Users\\user\\IdeaProjects\\trading\\src\\main\\resources\\NASDAQ_20200102.csv");
//        Reader reader = Files.newBufferedReader(path);
//        return oneByOne(reader);
//    }
//
//    private List<Candle> oneByOne(Reader reader) throws Exception {
//        List<Candle> candles = new ArrayList<>();
//        CSVReader csvReader = new CSVReader(reader);
//        String[] line;
//        Set<String> empty = new HashSet<>();
//        long maxId = contractController.getLastIdContract(contractController.getDailyContracts());
//        int count =0;
//        ContractBasic previousContract = new ContractBasic();
//        double lastPrice =0;
//        while ((line = csvReader.readNext()) != null) {
//            if (line.length==1) {
//                continue;
//            }
//                LocalDateTime date = parseLocalDate(line[1]);
//            try {
//                ContractBasic contract = contractController.getDailyContractsBySymbol().get(line[0]);
//                if(line[0].equals(previousContract.getSymbol())){
//                }else{
//                    if(appController.getGenerators().get(previousContract.getIdcontract())!= null) {
//                        System.out.println(appController.getDatabase().getSaverController().getInsertCandlesBuffer().size());
//                        System.out.println(appController.getGenerators().get(previousContract.getIdcontract())
//                                .getDatabase().getSaverController().getInsertCandlesBuffer().size());
//
//                        appController.getGenerators().get(previousContract.getIdcontract())
//                                .getDatabase().getSaverController()
//                                .saveNow(appController.getGenerators().get(previousContract.getIdcontract()), true);
//                    }
//                    contractController.createGenerator(contract);
//                        contractController.createProcessor(contract);
//
//                }
//
//                for(int i = 0; i<4; i++) {
//                    if (Double.parseDouble(line[2 + i]) != lastPrice) {
//                        Tick tick = new Tick(0, contract.getIdcontract(),
//                                date.atZone(Global.ZONE_ID), Double.parseDouble(line[2 + i]));
//                        appController.getGenerators().get(tick.getIdcontract()).processPrice(tick, false, false, false);
//                        System.out.println(tick.toString());
//                        lastPrice = Double.parseDouble(line[2 + i]);
//                    }
//                }
//                previousContract = (ContractBasic) contract.clone();
//
//
////                Candle candle = new Candle(date.atStartOfDay(Global.ZONE_ID), Double.parseDouble(line[2]), Double.parseDouble(line[3]), Double.parseDouble(line[4]),
////                        Double.parseDouble(line[5]), contractController.getDailyContractsMap().get(line[0]).getIdcontract(), 1380);
////                candle.setId(global.getIdCandle(true));
////                candles.add(candle);
//            }catch(NullPointerException e){
//                count++;
//                ContractBasic newContract = new ContractBasic(++maxId, "", "STK", "SMART", "USD", line[0], 0.02, 2, "1", null, null, "TRADES", 0, "ETF");
//                newContract.setType("DAILYCON");
//                contractController.getDailyContractsBySymbol().put(line[0], newContract);
////                contractController.saveContract(newContract);
////                Candle candle = new Candle(date.atStartOfDay(Global.ZONE_ID), Double.parseDouble(line[2]), Double.parseDouble(line[3]), Double.parseDouble(line[4]),
////                        Double.parseDouble(line[5]), maxId, 1380);
////                candle.setId(global.getIdCandle(true));
////                candles.add(candle);
//            }
//        }
////        appController.getDatabase().saveCandles(candles);
//        if(count==0){
//            LOGGER.info(">> No new Daily Contract reported");
//        }else {
//            LOGGER.info(">> (" + count + ") new Daily Contract reported");
//        }
//        reader.close();
//        csvReader.close();
//        return candles;
//    }
//
//
//    private LocalDateTime parseLocalDate(String date) {
//        LocalDateTime dateFrom = null;
//        try {
//            dateFrom = LocalDateTime.parse(date, formatterLocalDateTime1);
//            return dateFrom;
//        } catch (DateTimeParseException e1) {
//            e1.printStackTrace();
////            try {
////                dateFrom = LocalDate.parse(date, formatterLocalDate1);
////                LOGGER.warn("Parsing success >>> " + date);
////                return dateFrom;
////            } catch (DateTimeParseException e2) {
////                LOGGER.warn(e2.getMessage() + " >>> " + date);
//               return null;
////            }
//        }
//    }
//
////    private LocalDateTime parseLocalDateTime(String date) {
////        LocalDateTime refreshed = null;
////        try {
////            refreshed = LocalDateTime.parse(date, formatterLocalDateTime2);
////            return refreshed;
////        } catch (DateTimeParseException e1) {
////            try {
////                refreshed = LocalDateTime.parse(date, formatterLocalDateTime1);
////                LOGGER.warn("Parsing success >>> " + date);
////                return refreshed;
////            } catch (DateTimeParseException e2) {
////                LOGGER.warn(e2.getMessage() + " >>> " + date);
////                return null;
////            }
////        }
////    }
//
//    public void quoteListByDatePeriod(String token, String exchange, String date, String period){
//        try{
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            System.out.println("Requesting results " + LocalDateTime.now());
//            String str ="http://ws.eoddata.com/data.asmx/QuoteListByDatePeriod?Token=" + token + "&Exchange="+ exchange +"&QuoteDate=" + date +"&Period=" + period;
//            System.out.println(str);
//            Document doc = dBuilder.parse(str);
//            doc.getDocumentElement().normalize();
//            System.out.println("End " + LocalDateTime.now());
//            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
//            System.out.println("----------------------------");
//            NodeList nList = doc.getElementsByTagName("RESPONSE");
//
//            for (int temp = 0; temp < nList.getLength(); temp++) {
//                Node nNode = nList.item(temp);
//
//                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//                    Element eElement = (Element) nNode;
//                    System.out.println("EOD Token >> "
//                            + eElement.getAttribute("Token"));
//                    token = eElement.getAttribute("Token");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String getToken() {
//        String  token="";
//        try{
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//        Document doc = dBuilder.parse("http://ws.eoddata.com/data.asmx/Login?Username=matel&Password=Maq1!rrille");
//        doc.getDocumentElement().normalize();
//        NodeList nList = doc.getElementsByTagName("LOGINRESPONSE");
//
//        for (int temp = 0; temp < nList.getLength(); temp++) {
//            Node nNode = nList.item(temp);
//
//            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//                Element eElement = (Element) nNode;
//                System.out.println("EOD Token >> "
//                        + eElement.getAttribute("Token"));
//                token = eElement.getAttribute("Token");
//            }
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//        return token;
//    }
//
//}