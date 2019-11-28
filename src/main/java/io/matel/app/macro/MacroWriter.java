package io.matel.app.macro;

import com.opencsv.CSVReader;
import io.matel.app.macro.domain.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Component
public class MacroWriter {
    private static final Logger LOGGER = LogManager.getLogger(MacroWriter.class);
    private Semaphore semaphore = new Semaphore(10);

    @Autowired
    MacroUpdateRepo macroUpdateRepo;

    @Autowired
    MacroDataRepo macroDataRepo;

    @Autowired
    CountryPrefRepo countryPrefRepo;

    @Autowired
    MacroIgnoredCodeRepo macroIgnoredCodeRepo;


    private LocalDateTime lastDateTime;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    private DateTimeFormatter formatterLocalDateTime1 = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm");
    private DateTimeFormatter formatterLocalDateTime2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private DateTimeFormatter formatterLocalDate1 = DateTimeFormatter.ofPattern("d/M/yyyy");
    private DateTimeFormatter formatterLocalDate2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");



    public void start() throws Exception {
        List<CountryPref> countriesPref = countryPrefRepo.findAllByType("Primary");
        List<MacroIgnoredCode> ignoredMacroIgnoredCode = macroIgnoredCodeRepo.findAll();
        System.out.println(ignoredMacroIgnoredCode.size());

        List<String> countries = countriesPref.stream().map(c -> c.getCountry()).collect(Collectors.toList());
        List<String> ignoredItems = ignoredMacroIgnoredCode.stream().map(c -> c.getCode()).collect(Collectors.toList());


        MacroUpdate lastUpdate = macroUpdateRepo.findTopByOrderByRefreshedDesc();
        if (lastUpdate != null) {
            lastDateTime = lastUpdate.getRefreshed();
            System.out.println(lastDateTime);
        } else {
            lastDateTime = null;
        }
        LOGGER.info("Most recent refreshed date is " + lastDateTime);
        List<MacroUpdate> updates = readCsvLineByLine();
//        List<MacroUpdate> updates = macroUpdateRepo.findDistinctByCode("NZLTOUR");

        for (MacroUpdate update : updates) {
            if (countries.contains(update.getCountry()) && !ignoredItems.contains(update.getCode())) {
                semaphore.acquire();
                LOGGER.info("Requesting data for " + update.getCode());
                request(update.getCode());
            }
        }

    }

    public List<MacroUpdate> readCsvLineByLine() throws Exception {
        Reader reader = Files.newBufferedReader(Paths.get(
                ClassLoader.getSystemResource("SGE_metadata.csv").toURI()));
        return oneByOne(reader);
    }


    private LocalDate parseLocalDate(String date) {
        LocalDate dateFrom = null;
        try {
            dateFrom = LocalDate.parse(date, formatterLocalDate2);
            return dateFrom;
        } catch (DateTimeParseException e1) {
            try {
                dateFrom = LocalDate.parse(date, formatterLocalDate1);
                LOGGER.warn("Parsing success >>> " + date);
                return dateFrom;
            } catch (DateTimeParseException e2) {
                LOGGER.warn(e2.getMessage() + " >>> " + date);
                return null;
            }
        }
    }

    private LocalDateTime parseLocalDateTime(String date) {
        LocalDateTime refreshed = null;
        try {
            refreshed = LocalDateTime.parse(date, formatterLocalDateTime2);
            return refreshed;
        } catch (DateTimeParseException e1) {
            try {
                refreshed = LocalDateTime.parse(date, formatterLocalDateTime1);
                LOGGER.warn("Parsing success >>> " + date);
                return refreshed;
            } catch (DateTimeParseException e2) {
                LOGGER.warn(e2.getMessage() + " >>> " + date);
                return null;
            }
        }
    }

    private List<MacroUpdate> oneByOne(Reader reader) throws Exception {
        List<MacroUpdate> newItems = new ArrayList<>();
        CSVReader csvReader = new CSVReader(reader);
        String[] line;
        int cpt = 0; //avoid header
        while ((line = csvReader.readNext()) != null) {
            if (cpt > 0) {
                LocalDateTime refreshed = parseLocalDateTime(line[3]);
                LocalDate dateFrom = parseLocalDate(line[4]);
                LocalDate dateTo = parseLocalDate(line[5]);

                MacroUpdate update = new MacroUpdate(line[0], line[1], line[2], refreshed, dateFrom, dateTo);
                if (lastDateTime == null || (update.getRefreshed().toEpochSecond(ZoneOffset.UTC) > (lastDateTime.toEpochSecond(ZoneOffset.UTC)))) {
                    newItems.add(update);
                }
            }
            cpt++;
        }
        Collections.sort(newItems, (u1, u2) -> u1.getRefreshed().compareTo(u2.getRefreshed()));
        Collections.reverse(newItems);
        macroUpdateRepo.saveAll(newItems);
        reader.close();
        csvReader.close();
        return newItems;
    }


    public void request(String code) throws IOException, InterruptedException {
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://www.quandl.com/api/v3/datasets/SGE/" + code + ".json?api_key=e26RgxbRzqQSfQs67_ip"))
                .GET()
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    try {
                        MacroReponseHandler responseHandler = new MacroReponseHandler(macroDataRepo);
                        responseHandler.processResponse(res.body());
                        semaphore.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return res;
                });
    }


}
