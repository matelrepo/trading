package io.matel.app.dailycon;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DaylisCsvUploader {

    @Autowired
    DailysRepo dailysRepo;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");


    public void readCsvLineByLine() throws Exception {
        Reader reader = Files.newBufferedReader(Paths.get(
                ClassLoader.getSystemResource("USE_20190101_20191122.csv").toURI()));
         oneByOne(reader);
    }

    private void oneByOne(Reader reader) throws Exception {
        List<Dailys> dailysList = new ArrayList<>();
        CSVReader csvReader = new CSVReader(reader);
        String[] line;
        while ((line = csvReader.readNext()) != null) {
                LocalDate date = parseLocalDate(line[1]);
                Dailys dailys = new Dailys(line[0], date, Double.parseDouble(line[2]), Double.parseDouble(line[3]), Double.parseDouble(line[4]),
                        Double.parseDouble(line[5]), Double.parseDouble(line[6]));
//                System.out.println(stock.toString());
            dailysList.add(dailys);
//            System.out.println(stock.toString());
            if(dailysList.size()>20000) {
                            System.out.println("Saving");
                dailysRepo.saveAll(dailysList);
                dailysList.clear();
            }

        }
//        stocksRepo.saveAll(stocks);
        reader.close();
        csvReader.close();
    }

    private LocalDate parseLocalDate(String date) {
        LocalDate dateFrom = null;
        try {
            dateFrom = LocalDate.parse(date, formatter);
            return dateFrom;
        } catch (DateTimeParseException e) {
            e.getMessage();
            return null;
        }
    }



}
