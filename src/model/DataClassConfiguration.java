package model;
/**
 * This class is used to define the size of a page, record, key, and data
 */
public class DataClassConfiguration {
    
    /* Defining the size of a page. */
    public static final int PAGE_SIZE = 256;

    /* A constant that is used to define the size of a record. */
    public int RECORD_SIZE;
    
    /* Defining the size of the key. */
    public static final int KEY_SIZE = Integer.BYTES;
    
    /* Defining the size of the data.*/
    public int DATA_SIZE;

    
    public DataClassConfiguration(int dataSize) {
        DATA_SIZE = dataSize;
        RECORD_SIZE = DATA_SIZE + KEY_SIZE;
    }

}   
