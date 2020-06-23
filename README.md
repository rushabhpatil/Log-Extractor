# Log-Extractor
  Log files are recorded to every operation of software to debug any issues occured in previously by looking timely records stored. These log files tend to be very huge in size and therefore, keeping old records in database is also expensive. So extracting records from archived files require special techniques.
  Lets consider each log file have timestamp at the start and have line separator as 'n'. 
## Terms Involved:
 Terms                                    |  Symbols
 -----------------------------------------|-------------
Number of files in folder                |   n
Number of rows in each file (approx.)     |  r
 Size of each file                        |                     size
Size of split file                        |                       x

## Solution Time Complexity: O(logn+(size)/x logx )
By using first row of any file, we can use binary search to find the proper position with the required logs. If any file have starting row in time boundaries as given, algorithm checks log files from its previous file until all valid records are printed. 
## Issues:	Now parsing whole 16GB file had 2 issues.

* Single character line separator ‘n’. With this, couldn’t use any built-in java stream function such as readline() which use \n or \r\n for line separator. 
In general data line separator ‘n’ may occur anywhere, but n<ISO_8601 timestamp> cannot be in a single row. On this basis, string parsing can be done (had to do it manually due to custom separator). ISO format pattern is matched with regular expression for faster processing.
* Stream readers work line by line. Linear search on 16GB file may have 100s of million lines which will be bottleneck.
For this issue, as file is stored as a single string, seeking the stream with any number of bytes(long n) can skip any number of bytes in O(1) which in turn can work as row accessor with constant overhead. With this method, seek may get address to any character in the row, algorithm finds first time stamp and check its value which is done in O(1). 
Splitting the file in smaller pieces helped in time complexity. In fact, time complexity rise as function of logx/x×size. Therefore smaller the split size, better complexity (Ideally overall time complexity would beO(log⁡(nr))). In implementation, I divided file in 1MB packets (approx. 16800 in number) and later applied binary search. 
## Time Limits:
	With Logmaker.java code attached herewith, I created 16.8GB file for performance evaluation. Searching this file will require nearly 13 binary searches (log (10000)) from 10000 files which is negligible. After we find the first valid row, we need to traverse file sequentially and print therefore no optimization there from O(required rows).
	On 16.8GB file, it required 0.78 seconds on average to find the records and print it in 0.1 sec.(command line printing time may change as per query time). With other additional time required to find target file, algorithm guarantees process in 1.1-1.3 seconds in practical scenario. 
  
 Let me know any discripancies.
