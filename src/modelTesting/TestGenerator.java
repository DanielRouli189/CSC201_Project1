package modelTesting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *  <p>
 *  The {@code TestGenerator} class is a utility class created for generating
 *  the necessary tests required for the first exercise of the course
 *  CSC201 - Data Structures and Algorithms. More specifically, three tests
 *  are created: a linear search on a file with keys and data, a linear search
 *  on the index file that contains the keys and their position in the original file,
 *  and a binary search on the index file.
 *  </p>
 *  <p>
 *  Each instance of this class is a separate thread which runs in parallel with 
 *  other instances of this class. The reasons behind this design choice are merely 
 *  educational and not necessarily functional, even though a performance boost with
 *  respect to time is to be expected.
 *  </p>
 * 
 *  @author nrouli
 *  @since 2023-03
 *  
 */
public class TestGenerator implements Runnable {

    /* The length of the data string (27 or 55 bytes) */
    private int dataBytes;
    
    /* A random number generator used to generate random integers.*/
    private static Random RNG = new Random();

    /* The  number of records to be generated. */
    private int numOfRecords;

    /* A TestStructure object that contains performance information about the test. */
    private TestStructure testResults;
   
    /* The number of repetitions to make for random searches */
    private int counts;
    
    /* A list to hold the performance metrics of the file structures. */
    private static List<TestStructure> testStructureList = new ArrayList<>();

    /* Linear search stopwatch */
    private long startLinearSearch;
    private long stopLinearSearch;

    /* Linear search in index file stopwatch */
    private long startLinearIndexSearch;
    private long stopLinearIndexSearch;

    /* Binary search stopwatch */
    private long startBinarySearch;
    private long stopBinarySearch;

    /**
     * Constructs a TestGenerator object with the specified parameters.
     * 
     * @param dataBytes The number of bytes to be generated for each record.
     * @param numOfRecords The number of records to be generated.
     * @param counts The number of times to search for a record in each file.
     */
    public TestGenerator(int dataBytes, int numOfRecords, int counts) {
        resetTimers();
        this.dataBytes = dataBytes;
        this.numOfRecords = numOfRecords;
        this.counts = counts;
        testResults = new TestStructure(0,0,0,0,0,0,0,0);        
    }

    /**
     * This function resets the timers to 0
     */
    public void resetTimers() {
        startBinarySearch = 0;
        stopBinarySearch = 0;
        startLinearSearch = 0;
        stopLinearSearch = 0;
        startLinearIndexSearch = 0;
        stopLinearIndexSearch = 0;
    }

    /**
     * Runs the test in a thread.
     */
    @Override
    public void run() {
        try {
            DataGenerator dg = new DataGenerator(dataBytes, numOfRecords);
            if(dg.getRf().getFile() == null || dg.getRf().getIndexFile() == null){
                dg.generateRecords();
                dg.fillFile();
                dg.getRf().split();
            }
            
            testStructureList.add(makeTest(dg, this.counts));
            dg.getRf().close();
            dg.getRf().getFile().deleteOnExit();
            dg.getRf().getIndexFile().getFile().deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a TestStructure object containing performance metrics about the test.
     * 
     * @param dg The DataGenerator object.
     * @param counts The number of times to search for a record in each file.
     * @return The TestStructure object containing information about the test.
     */
    public TestStructure makeTest(DataGenerator dg, int counts) throws IOException {
        float randomLinearSearch = randomSearchCounter(dg, counts);
        float randomIndexSearch = randomSearchCounterIndexFile(dg, counts);
        float randomBinarySearch = randomBinarySearchCounter(dg, counts);

        testResults = new TestStructure(dataBytes, numOfRecords, randomLinearSearch, 
                          stopLinearSearch, randomIndexSearch,
                          stopLinearIndexSearch, randomBinarySearch,
                          stopBinarySearch);
        resetTimers();
        return testResults;
    }

    /**
     * Returns the average number of disk accesses made by the searchFile function in the RFile class
     * for a random serial search in the original file.
     * 
     * @param dg The DataGenerator object.
     * @param counts The number of times to search for a record in each file.
     * @return The average number of disk accesses.
     */
    public synchronized float randomSearchCounter(DataGenerator dg, int counts) throws IOException {
        float result = 0;
        int[] randomInts = (numOfRecords >= 50 && numOfRecords <= 1000) ?
                            RNG.ints(1, 2*numOfRecords+1).limit(counts).toArray() :
                            RNG.ints(1, 2*numOfRecords+1).distinct().limit(counts).toArray();
        
        
        for(int i = 0; i < counts; ++i){
            dg.getRf().getClass().getName();
            dg.getClass().getName();
            startLinearSearch = System.nanoTime();
            result += dg.getRf().searchFile(randomInts[i]);
            stopLinearSearch += (System.nanoTime() - startLinearSearch);
        }

        stopLinearSearch /= counts;
        return result/counts;
    }

    /**
     * Returns the average number of disk accesses made by the searchFile function in the RFile class
     * for a random serial search in the index file.
     * 
     * @param dg The DataGenerator object.
     * @param counts The number of times to search for a record in each file.
     * @return The average number of disk accesses.
     */
    public synchronized float randomSearchCounterIndexFile(DataGenerator dg, int counts) throws IOException {
        float result = 0;
        int[] randomInts = (numOfRecords >= 50 && numOfRecords <= 1000) ?
                            RNG.ints(1, 2*numOfRecords+1).limit(counts).toArray() :
                            RNG.ints(1, 2*numOfRecords+1).distinct().limit(counts).toArray();
        
        
        for(int i = 0; i < counts; ++i) {
            dg.getRf().getClass().getName();
            dg.getClass().getName();
            startLinearIndexSearch = System.nanoTime();
            result += dg.getRf().getIndexFile().searchFile(randomInts[i]);
            stopLinearIndexSearch += (System.nanoTime() - startLinearIndexSearch);
        }

        stopLinearIndexSearch /= counts;
        return result/counts;
    }

    /**
     * Returns the average number of disk accesses made by the searchFile function in the RFile class
     * for a random serial search in the original file.
     * 
     * @param dg The DataGenerator object.
     * @param counts The number of times to search for a record in each file.
     * @return The average number of disk accesses.
     */
    public synchronized float randomBinarySearchCounter(DataGenerator dg, int counts) throws IOException {
        dg.getRf().sort();
        float result = 0;
        int[] randomInts = (numOfRecords >= 50 && numOfRecords <= 1000) ?
                            RNG.ints(1, 2*numOfRecords+1).limit(counts).toArray() :
                            RNG.ints(1, 2*numOfRecords+1).distinct().limit(counts).toArray();
        
        for(int i = 1; i <= counts; ++i) {
            startBinarySearch = System.nanoTime();
            result += dg.getRf().binarySearch2(randomInts[i-1]);
            stopBinarySearch += System.nanoTime() - startBinarySearch;
        }

        stopBinarySearch /= counts;
        return result/counts;
    }

    /*------------------Getters------------------*/
    public int getDataBytes() {
        return dataBytes;
    }

    public int getNumOfRecords() {
        return numOfRecords;
    }

    public TestStructure getTestResults() {
        return testResults;
    }

    public static List<TestStructure> getTestStructureList() {
        return testStructureList;
    }

    public int getCounts() {
        return counts;
    }
    
}
