package modelTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.Utils;

/**
 * The {@code App} class is the main class of this application, responsible for
 * generating test files and measuring the time it takes to generate them.
 * 
 * @author nr
 * 
 */
public class App {
    
    /* The Singleton instance of the App class. */
    private static App app = null;

    /* The array of test values to use when generating test files. */
    protected static final int[] TEST_VALUES = {50, 100, 200, 500, 800, 1000, 2000, 5000, 10000, 50000, 100000, 200000};
    
    /* The list of TestGenerator objects used to generate the test files. */
    private List<TestGenerator> generators;

    /* The list of threads that run the generators */
    private List<Thread> threads;

    /**
     * Constructs a new `App` object.
     */
    public App() {
        generators = new ArrayList<>();
        threads = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of the `App` class. If the instance has not yet been
     * created, it will be created and returned.
     *
     * @return The singleton instance of the `App` class.
     */
    public static App getInstance() {
        return app == null ? new App() : app;
    }

    /**
     * Clears the generators and threads lists
     */
    public void resetThreads() {
        generators.clear();
        threads.clear();
        TestGenerator.getTestStructureList().clear();
    }

    /**
     * The main method of the {@code App} class, which generates test files and measures the time
     * it takes to generate them.
     *
     * @param args The command-line arguments to the application.
     * @throws InterruptedException If one of the threads is interrupted while waiting for
     *                              another thread to finish.
     */
    public static void main(String[] args) throws InterruptedException {
        app = App.getInstance();

        
        long totTime2 = app.createTestFiles(55);
        long totTime = app.createTestFiles(27);
        System.out.println("Runtime: "+(totTime + totTime2)/1000+ " seconds");
    
    }

    /**
     * Creates a bunch of threads that each create a file with a certain amount of data in it.
     * Once the threads have finished running, it prints out the results of the tests.
     *
     * @param dataBytes The size of the file to be created.
     * @return The time it took to create the test files and run the tests.
     * @throws InterruptedException If one of the threads is interrupted while waiting for
     *                              another thread to finish.
     */
    public synchronized long createTestFiles(int dataBytes) throws InterruptedException {
        resetThreads();
        long start = System.currentTimeMillis();
       
        // Creating a new TestGenerator and Thread for each value in the TEST_VALUES array.
        for(int i = 0; i< TEST_VALUES.length; ++i) {
            generators.add(new TestGenerator(dataBytes, TEST_VALUES[i], 10));
            threads.add(new Thread(generators.get(i)));
        }

        //Starting all the threads.
        for(int i=0; i< threads.size(); ++i)
            threads.get(i).start();
        
        // Waiting for the threads to finish before continuing
        for(int i = 0; i<threads.size(); ++i)
            threads.get(i).join();

        // Sorting the `TestStructure` list by the number of records.
        Collections.sort(TestGenerator.getTestStructureList(), (g1, g2) -> Utils.compare(g1.numOfRecords(), g2.numOfRecords()));

        System.out.println("\n\n|| Data Byte Length |"+"| Number of Records |"+"| disk accesses method A |"+ "| disk accesses method B |"+ "| disk accesses method C ||");
        for(int i = 0; i < app.generators.size(); i++) {
            System.out.printf("||%18d||%19d||%24.1f||%24.1f||%24.1f||\n", 
            TestGenerator.getTestStructureList().get(i).dataBytes(), 
            TestGenerator.getTestStructureList().get(i).numOfRecords(), 
            TestGenerator.getTestStructureList().get(i).discAccessCountA(),
            TestGenerator.getTestStructureList().get(i).discAccessCountB(), 
            TestGenerator.getTestStructureList().get(i).discAccessCountC());
        }

        System.out.println("\n\n|| Data Byte Length |"+"| Number of Records |"+"| runtime method A |"+ "| runtime method B |"+ "| runtime method C ||");
        for(int i = 0; i < app.generators.size(); i++) {
            System.out.printf("||%18d||%19d||%13d (ns)||%13d (ns)||%13d (ns)||\n", 
            TestGenerator.getTestStructureList().get(i).dataBytes(), 
            TestGenerator.getTestStructureList().get(i).numOfRecords(), 
            TestGenerator.getTestStructureList().get(i).runtimeA(), 
            TestGenerator.getTestStructureList().get(i).runtimeB(), 
            TestGenerator.getTestStructureList().get(i).runtimeC());
        }

        resetThreads();
        long end = System.currentTimeMillis();
        return (end - start);
    }

    public List<TestGenerator> getGenerators() {
        return generators;
    }

    public List<Thread> getThreads() {
        return threads;
    }
    
}
