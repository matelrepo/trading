package io.matel.app.macro.config;

import com.opencsv.CSVReader;
import io.matel.app.macro.domain.CountryPref;
import io.matel.app.macro.domain.CountryPrefRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Configuration
public class GetterCountryCsv {

    @Autowired
    CountryPrefRepo countryPrefRepo;


    public String readCsvLineByLine() throws Exception {
        Reader reader = Files.newBufferedReader(Paths.get(
                ClassLoader.getSystemResource("country_pref.csv").toURI()));

        List<CountryPref> list = new ArrayList<>();
        CSVReader csvReader = new CSVReader(reader);
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            list.add(new CountryPref(line[0], line[1], line[2]));
        }

        countryPrefRepo.saveAll(list);
        reader.close();
        csvReader.close();

        return list.toString();
    }
}
