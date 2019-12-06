package io.matel.app.dailycandle;

import com.opencsv.CSVReader;
import io.matel.app.AppLauncher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class DailyCandleCsvUploader {

    @Autowired
    DailyCandleRepo dailyCandleRepo;

    LocalDate dcandleDate;
    private static final Logger LOGGER = LogManager.getLogger(AppLauncher.class);


    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");


    public void start() throws Exception {
        DailyCandle dcandle = dailyCandleRepo.findTopByOrderByDateDesc();
        if (dcandle != null) {
            dcandleDate = dcandle.getDate();
        } else {
            dcandleDate = null;
        }
        LOGGER.info("Most recent refreshed dailyCandle date is " + dcandleDate);

        readCsvLineByLine();
    }


    public void readCsvLineByLine() throws Exception {
        Reader reader = Files.newBufferedReader(Paths.get(
                ClassLoader.getSystemResource("USE_20190101_20191122.csv").toURI()));
        oneByOne(reader);
    }

    private void oneByOne(Reader reader) throws Exception {
        List<DailyCandle> dailyCandleList = new ArrayList<>();
        CSVReader csvReader = new CSVReader(reader);
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            LocalDate date = parseLocalDate(line[1]);
            DailyCandle dailyCandle = new DailyCandle(line[0], date, Double.parseDouble(line[2]), Double.parseDouble(line[3]), Double.parseDouble(line[4]),
                    Double.parseDouble(line[5]), Double.parseDouble(line[6]));
            if (dcandleDate == null || (dailyCandle.getDate().isAfter(dcandleDate) )) {
                dailyCandleList.add(dailyCandle);
            }
            if (dailyCandleList.size() > 20000) {
                System.out.println("Saving");
                dailyCandleRepo.saveAll(dailyCandleList);
                dailyCandleList.clear();
            }

        }

        dailyCandleRepo.saveAll(dailyCandleList);

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
