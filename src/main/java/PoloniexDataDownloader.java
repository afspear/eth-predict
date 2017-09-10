import com.google.gson.Gson;
import eu.verdelhan.ta4j.TimeSeries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class PoloniexDataDownloader extends DataDownloader{
    private  String url = "https://poloniex.com/public?command=returnChartData&currencyPair=USDT_ETH&start=1435699200&end=9999999999&period=14400";
    private String csvFileName = "allPoloniexData.csv";
    private  Gson gson = new Gson();


    public static void main(String[] args) {
        PoloniexDataDownloader poloniexDataDownloader = new PoloniexDataDownloader();
        try {
            poloniexDataDownloader.getData();
            TimeSeriesCreator creator = new TimeSeriesCreator();
            TimeSeries timeseries = creator.createTimeSeries(poloniexDataDownloader.csvFileName);
            IndicatorAdder.addIndicators(timeseries);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getData () throws IOException {

        if(Files.exists(Paths.get(csvFileName)))
            Files.delete(Paths.get(csvFileName));
        Files.createFile(Paths.get(csvFileName));

        PoloniexDataDownloader poloniexDataDownloader = new PoloniexDataDownloader();
        String json = poloniexDataDownloader.download(url);
        List<Map> data = gson.fromJson(json, List.class);
        List<String> keys = new ArrayList<>();
        data
          .stream()
          .findFirst()
          .ifPresent(map -> {
              StringJoiner joiner = new StringJoiner(",");
              keys.addAll(map.keySet());
              keys.forEach(o -> joiner.add(o.toString()));
              writeToFile(joiner.toString());


          });

        data
          .stream()
          .forEach(map -> {
              StringJoiner joiner = new StringJoiner(",");
              keys.forEach(s -> {

                  String value;

                  if (s.equals("date")){
                      int unixTimeStamp = ((Double)map.get(s)).intValue();
                      Date date = new Date((long)unixTimeStamp*1000);
                      value = new SimpleDateFormat("yyyy-MM-dd:HH:mm").format(date);

                  }
                  else {
                      value = map.get(s).toString();
                  }


                  joiner.add(value);
              });

              writeToFile(joiner.toString());
          });



    }

    private void writeToFile(String line) {
        try {
            Files.write(Paths.get(csvFileName), (line + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
