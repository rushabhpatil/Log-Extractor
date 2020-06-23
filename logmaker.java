import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class logmaker {
    /**
     * Function creates log file with ',' separator and 'n' and Row contains ISO
     * 8601 first element. File created have 16.8GB size to mimic large log file for
     * checking performance of LogExtractor.
     */
    public static void main(String args[]) throws IOException {

        try {
            File trial = new File("trial3.log");
            trial.createNewFile();

        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
        DateTimeFormatter isoformat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());

        ZonedDateTime date = ZonedDateTime.now();
        System.out.println(isoformat.format(date));
        File temp = new File("trial3.log");
        String randomdata = "";
        for (Integer i = 1; i <= 15; i++) {
            randomdata += "----------X" + Integer.toString(i);
        }
        try {
            FileWriter myWriter = new FileWriter(temp, true);
            String line;
            for (Integer i = 1; i <= 75006625; i++) {

                line = "";
                line = isoformat.format(date) + "," + "rowNUmber" + Integer.toString(i) + "," + randomdata + "," + "n";

                System.out.println(line);
                myWriter.append(line);
                date = date.plusMinutes(2);
                if (i % 10000 == 0) {
                    System.out.println(Integer.toString(i / 10000) + "/7500");
                    myWriter.close();
                    myWriter = new FileWriter(temp, true);
                }
            }
            myWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}