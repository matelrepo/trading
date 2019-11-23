//package io.matel.app.macro.config;
//
//import com.opencsv.CSVReader;
//import io.matel.app.macro.domain.MacroUpdate;
//import io.matel.app.macro.domain.MacroUpdateRepo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.Reader;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//@Configuration
//public class GetterMacroCsv {
//
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    LocalDateTime lastDateTime;
//
//    @Autowired
//    MacroUpdateRepo macroUpdateRepo;
//
//    private List<MacroUpdate> oneByOne(Reader reader) throws Exception {
//        MacroUpdate lastUpdate = macroUpdateRepo.findTopByOrderByRefreshed();
//        if(lastUpdate != null) {
//            lastDateTime = lastUpdate.getRefreshed();
//            System.out.println(lastDateTime);
//        }else{
//            lastDateTime = null;
//        }
//        List<MacroUpdate> list = new ArrayList<>();
//        CSVReader csvReader = new CSVReader(reader);
//        String[] line;
//        int cpt=0; //header
//        while ((line = csvReader.readNext()) != null) {
//            if(cpt>0) {
//                MacroUpdate update = new MacroUpdate(line[0], line[1], line[2], LocalDateTime.parse(line[3], formatter), LocalDate.parse(line[4]), LocalDate.parse(line[5]));
//                if(lastDateTime== null || update.getRefreshed().isAfter(lastDateTime)) {
//                    list.add(update);
//                }
//            }
//            cpt++;
//        }
//        Collections.sort(list, (u1, u2) -> u1.getRefreshed().compareTo(u2.getRefreshed()));
//        Collections.reverse(list);
//        macroUpdateRepo.saveAll(list);
//
//
//
//        System.out.println(list.size());
//
//        reader.close();
//        csvReader.close();
//        return list;
//    }
//
//    public String readCsvLineByLine() throws Exception {
//        Reader reader = Files.newBufferedReader(Paths.get(
//                ClassLoader.getSystemResource("SGE_metadata.csv").toURI()));
//        return oneByOne(reader).toString();
//    }
//
//    public LocalDateTime getLastDateTime() {
//        return lastDateTime;
//    }
//
//    public void setLastDateTime(LocalDateTime lastDateTime) {
//        this.lastDateTime = lastDateTime;
//    }
//}
