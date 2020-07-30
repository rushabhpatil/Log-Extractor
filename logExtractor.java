import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

/**
 * Logextractor is a class to extract recording from list of files in given path
 * with in each file, All rows are sorted by Timestamp, first element of row is
 * ISO 8601 timestamp and row elements are comma-separated values and single
 * character 'n' as endline character.
 */
public class logExtractor {
    /** Hold is random-access pointer of queried file in setquery() function. */
    public RandomAccessFile hold, initialpos;
    /** Size in Bytes of file queried in setquery() function. */
    public Long len;

    public ZonedDateTime t_init, t_end;
    public Boolean fileEndFlag = false;
    public String appendrecord = "";
    DateTimeFormatter isoformat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());

    Pattern isoregex = Pattern.compile("\\d{4}-\\d{2}-\\d{2}.*");

    /**
     * Find string from given RandomAccessfile address to next ','.
     * 
     * @param FileAddress: RandomAccessFile
     * @return String
     */
    public String findnextsection(RandomAccessFile rafile) {
        fileEndFlag = false;
        String ans = "";
        while (true) {
            try {
                char temp = (char) rafile.readByte();
                if (temp == ',') {
                    return ans;
                } else {
                    ans += temp;
                }

            } catch (EOFException e) {
                fileEndFlag = true;
                return ans;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * set File to be queried for records between time t_i to t_e
     * 
     * @param FileAddress: RandomAccessFile
     * @param Starttime:   String
     * @param Endtime:     String
     * @return void
     */
    public void setquery(RandomAccessFile f1, String t_i, String t_e) {
        hold = f1;
        t_init = ZonedDateTime.parse(t_i, isoformat);
        t_end = ZonedDateTime.parse(t_e, isoformat);
    }

    /**
     * Check if Given Timestamp is between given boundaries, If yes- return same
     * timestamp else return ""
     * 
     * @param Timestamp: String
     * @return String
     */

    public String requiredstamp(String str) {
        ZonedDateTime dt = ZonedDateTime.parse(str, isoformat);
        if ((t_init.isBefore(dt) && t_end.isAfter(dt)) || t_init.equals(dt) || t_end.equals(dt)) {
            return str;
        } else {
            return "";
        }
    }

    /**
     * Checks if given string contain Date stamp using regular expression.
     * 
     * @param Section: String
     * @return Boolean
     */
    public Boolean matchisoformat(String str) {
        Matcher m = isoregex.matcher(str);
        return m.matches();
    }

    /**
     * Given random address of file, Function checks next row timestamp for given
     * boundaries
     * 
     * @param Address: RandomAccessfile
     * @return Boolean
     */
    public Boolean checkfirststamp(RandomAccessFile pos) {
        fileEndFlag = false;
        findnextsection(pos);
        while (true) {
            String part = findnextsection(pos);
            if (matchisoformat(part.substring(1))) {
                if (requiredstamp(part.substring(1)) != "") {
                    return true;
                } else {
                    return false;
                }
            } else if (fileEndFlag) {
                return false;
            }

        }
    }

    /**
     * Compare date if it is greater than t_init
     * 
     * @param Date: String
     * @return String
     */
    public String dategreater(String str) {
        ZonedDateTime dt = ZonedDateTime.parse(str, isoformat);
        if ((t_init.isBefore(dt)) || t_init.equals(dt)) {
            return str;
        } else {
            return "";
        }
    }

    /**
     * Given random address of file, Function checks next row timestamp for greater
     * than t_init boundaries
     * 
     * @param Address: RandomAccessfile
     * @return Boolean
     */
    public Boolean checkgreater(RandomAccessFile pos) {
        fileEndFlag = false;
        findnextsection(pos);

        while (true) {
            String part = findnextsection(pos);
            if (matchisoformat(part.substring(1))) {
                if (dategreater(part.substring(1)) != "") {
                    return true;
                } else {
                    return false;
                }
            } else if (fileEndFlag) {
                return false;
            }

        }
    }

    /**
     * Print sequentially all valid rows after given address. Given line separator
     * is 'n'.
     * 
     * @param Address: RandomAccessFile
     * @return void
     */
    public void printseq(RandomAccessFile rafile) {
        Boolean prev = false;
        Boolean curr = false;
        fileEndFlag = false;
        String ans = "";
        while (true) {
            String part = findnextsection(rafile);
            if (fileEndFlag == true) {
                System.out.println(ans);
                return;
            }
            if (matchisoformat(part)) {

                String temp = requiredstamp(part);
                if (temp != "") {
                    prev = true;
                    curr = true;
                    ans += (part + ",");
                } else {
                    if (prev)
                        return;
                    else {
                        curr = false;
                        continue;
                    }
                }
            }
            if (matchisoformat(part.substring(1))) {
                String temp = requiredstamp(part.substring(1));
                if (temp != "") {
                    prev = true;
                    curr = true;
                    System.out.println(ans);
                    ans = "";
                    ans += (temp + ",");
                } else {
                    if (ans != "")
                        System.out.println(ans);
                    ans = "";
                    if (prev)
                        return;
                    else {
                        curr = false;
                        continue;
                    }
                }
            } else if (fileEndFlag) {
                System.out.println(ans);
                return;
            } else {
                if (curr)
                    ans += (part + ",");
            }

        }
    }

    /**
     * Given file with time sorted rows in setquery. Extracts the rows required
     * using binary search algorithm on file address itself.
     * 
     * @param Packetsize: Long
     */
    public void findrecordsinfile(Long packetsize) {
        // If initial record is required record, just print sequentially untill
        // timestamp outside given range
        RandomAccessFile initialpos = hold;
        String initstamp = findnextsection(initialpos);
        if (matchisoformat(initstamp) && requiredstamp(initstamp) != "") {
            printseq(hold);
        } else {
            Long size;
            try {
                size = hold.length();
                Long lo = (long) 1;
                Long hi = size / packetsize;
                // Long packetsize = (long) 1;
                if (hi != 0) {
                    packetsize = size / hi;
                }
                Long mid = lo;
                while (lo < hi) {
                    mid = (lo + hi) / 2;
                    initialpos = hold;
                    try {
                        initialpos.seek((mid - 1) * packetsize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (checkgreater(initialpos)) {
                        hi = mid - 1;
                    } else {
                        lo = mid + 1;
                    }
                }
                lo = mid - 1;
                initialpos = hold;
                try {
                    initialpos.seek((lo - 1) * packetsize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                printseq(initialpos);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Initiate LogExtractor. First Uses binary search to find given file. Then
     * given records extracted using findrecordsinfile() efficiently. Next files
     * than target files also checked for remaining records.
     * 
     * @param Starttime: ISO 8601 timestamp
     * @param Endtime:   ISO 8601 Timestamp
     * @param FolderPath
     */
    public static void main(String args[]) {
        /*
         * File[] fileList = new File(args[2]).listFiles(); Integer n = fileList.length;
         * logExtractor lge = new logExtractor(); RandomAccessFile targetfile,
         * processrafile, temp; Integer lo = 0, hi = n - 1; Long packetsize =
         * Math.round(Math.pow(10, 6)); // For approx 1MB packet try { while (lo < hi) {
         * 
         * Integer mid = (lo + hi) / 2; RandomAccessFile midrafile = new
         * RandomAccessFile(fileList[mid], "r"); if (lge.checkfirststamp(midrafile)) {
         * hi = mid - 1; } else { lo = mid; } targetfile = new
         * RandomAccessFile(fileList[lo], "r"); lge.setquery(targetfile, args[0],
         * args[1]); lge.findrecordsinfile(packetsize); } } catch (FileNotFoundException
         * e) {
         * 
         * e.printStackTrace(); }
         * 
         * // Print all records sequentially from next files if they satisfy time //
         * boundaries. Integer idx = 1; try { while (true) {
         * 
         * processrafile = new RandomAccessFile(fileList[lo + idx], "r"); temp =
         * processrafile; String firstrowtime = lge.findnextsection(temp);
         * 
         * if (lge.requiredstamp(firstrowtime) != "") { lge.setquery(processrafile,
         * args[0], args[1]); lge.findrecordsinfile(packetsize); } else { break; }
         * idx++;
         * 
         * } } catch (FileNotFoundException e) {
         * 
         * e.printStackTrace(); }
         */
        // Sample Code for testing code on single file.

        try {
            RandomAccessFile myfile = new RandomAccessFile("trial3.log", "r");
            String t1 = "2020-07-23T12:02:23.178+0530";
            String t2 = "2020-07-23T12:10:23.178+0530";
            logExtractor lge = new logExtractor();
            lge.setquery(myfile, t1, t2);
            lge.findrecordsinfile((long) 1000000);

            try {
                System.out.println(myfile.length());

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
