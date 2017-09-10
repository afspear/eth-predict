import au.com.bytecode.opencsv.CSVReader;
import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimeSeriesCreator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm");

    public TimeSeries createTimeSeries (String csvDataFileName) {
        List<Tick> ticks = new ArrayList<>();
        try {
            FileInputStream fileInputStreamn = new FileInputStream(new File(csvDataFileName));

            CSVReader csvReader = new CSVReader(new InputStreamReader(fileInputStreamn, Charset.forName("UTF-8")), ',', '"', 1);

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                ZonedDateTime date = LocalDateTime.parse(line[0], DATE_FORMAT).atZone(ZoneId.systemDefault());
                double high = Double.parseDouble(line[1]);
                double low = Double.parseDouble(line[2]);
                double open = Double.parseDouble(line[3]);
                double close = Double.parseDouble(line[4]);
                double volume = Double.parseDouble(line[5]);


                ticks.add(
                  new BaseTick(
                    Duration.ofHours(4),
                    date,
                    Decimal.valueOf(open),
                    Decimal.valueOf(high),
                    Decimal.valueOf(low),
                    Decimal.valueOf(close),
                    Decimal.valueOf(volume)
                  )
                );

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        TimeSeries timeSeries = new TimeSeries("eth_ticks", ticks);
        return timeSeries;

    }




}
