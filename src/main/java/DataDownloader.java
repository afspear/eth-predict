import com.google.common.collect.Lists;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataDownloader {
    private String csv = "?output=csv";
    private OkHttpClient client = new OkHttpClient();
    private String allDataFileName = "allData.csv";

    TreeMap<Date,Map<String, String>> allData = new TreeMap<>();
    SimpleDateFormat df = new SimpleDateFormat("M/d/yyyy");


    private void combineCSVData(String csv, String csvDataName) throws Exception {



        Reader in = new StringReader(csv);
        CSVParser records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

        for (CSVRecord record : records) {
            String dateString = record.get("Date(UTC)");
            String value = record.get("Value");

            Date date = df.parse(dateString);


            if (allData.get(date) == null) {
                allData.put(date, new HashMap<>());
            }
            allData.get(date);
            Map<String, String> mapForDataOnDay = allData.get(date);
            mapForDataOnDay.put(csvDataName, value);


        }
    }



    public static void main(String[] args) {


        DataDownloader dataDownloader = new DataDownloader();

        try {


            String urls = new String(Files.readAllBytes(Paths.get("urls")));
            List<String> urlsList = Arrays.asList(urls.split("\n"));

            urlsList
                    .stream()
                    .map(s -> s + "?output=csv")
                    .forEach(s -> {

                        try {
                            String dataCSV = dataDownloader.run(s);
                            URI uri = URI.create(s);
                            String path = uri.getPath();
                            String dataName = path.substring(path.lastIndexOf('/') + 1);
                            dataDownloader.combineCSVData(dataCSV, dataName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });





            Gson gson = new Gson();
            Files.write(Paths.get("allData.json"), gson.toJson(dataDownloader.allData).getBytes());


            dataDownloader.writeAllDataAsCSV();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeAllDataAsCSV () {


        //3/14/2017={blocksize=2433, gasprice=22958465248, gasused=3113154274, blocks=5961, hashrate=12624.5995, difficulty=179.845, etherprice=28.63, ethersupplygrowth=89759305.81250, uncles=407, gaslimit=4010738, blocktime=14.43, chaindatasize=130102567909, ethersupply=31214.84375
        String header = "etherprice,blocksize,gasprice,gasused,blocks,hashrate,difficulty,ethersupplygrowth,uncles,gaslimit,blocktime,chaindatasize,ethersupply,next-day-etherprice";

        try {
            Path alldataCSV = Paths.get("allData.csv");
            if(Files.exists(alldataCSV))
                Files.delete(alldataCSV);
            Files.createFile(Paths.get("allData.csv"));
            Files.write(Paths.get("allData.csv"), (header + "\n").getBytes(), StandardOpenOption.APPEND);


            NavigableSet<Date> dates = allData.descendingKeySet();
            dates.forEach(date -> {


                StringJoiner stringJoiner = new StringJoiner(",");

                Map<String, String> thisDaysData = allData.get(date);
                List<String> headers = Arrays.asList(header.split(","));
                headers
                  .stream()
                  .forEach(s -> {
                    String data = thisDaysData.get(s);
                    if (data != null)
                        stringJoiner.add(data);
                });

                String nextDayEtherPrice = Optional.ofNullable(dates.lower(date))
                        .map(date1 -> {
                            Map<String, String> map = allData.get(date1);
                            return map.get("etherprice");
                        }).orElse(" ");
                stringJoiner.add(nextDayEtherPrice);

                stringJoiner.add("\n");

                try {
                    Files.write(Paths.get("allData.csv"), stringJoiner.toString().getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });



        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
