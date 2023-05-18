package modelTesting;

import java.util.Random;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.Record;
import utils.Utils;
import model.DataClassConfiguration;
import model.RFile;


/**
 * The {@code DataGenerator} class generates a list of records with random keys
 * and random data strings of fixed length, where the number of records and the
 * length of the data strings are specified by the constructor.
 *
 */
public class DataGenerator {

    /* The length of the data string in bytes.*/
    private int dataBytes; 
    
    /* The amount of records to be created in the file. */
    private int numOfRecords; 
    
    /* DataClassConfiguration Object */
    private DataClassConfiguration DCC = null;
    
    /* The list of records */
    private List<Record> recordList;
    
    /* RFile Object */
    private RFile rf = null;

    /* A buffer that is used to store the data that will be written to the file. */
    private final byte[] buffer = new byte[DataClassConfiguration.PAGE_SIZE];

    /* A constant that is used to calculate the number of records that can fit in a page. */
    private int RECORDS_IN_PAGE;

    /**
	 * Constructor for the DataGenerator class.
	 * 
	 * @param dataBytes The length of the data strings in bytes.
	 * @param numOfRecords The amount of records to be created in the file.
	 * @throws IOException If there is an I/O error.
	 * @throws IllegalArgumentException If either the dataBytes or numOfRecords argument is negative.
     * 
	 */
    public DataGenerator(int dataBytes, int numOfRecords) throws IOException {
        if(dataBytes < 0)
            throw new IllegalArgumentException("The length of the data string is negative");

        if(numOfRecords < 0)
            throw new IllegalArgumentException("You cannot create a negative amount of Records");
        
        this.dataBytes = dataBytes;
        this.RECORDS_IN_PAGE = DataClassConfiguration.PAGE_SIZE/(this.dataBytes+Integer.BYTES);
        this.numOfRecords = numOfRecords;
        DCC = instantiateDCC(dataBytes);
        rf = instantiateRFile(dataBytes, numOfRecords);
        recordList = new ArrayList<>(numOfRecords);
    }

    /**
     * Create a DataClassConfiguration Singleton object.
     * 
     * @param dataBytes The number of bytes of data that will be stored in the DataClass.
     * @return A new instance of DataClassConfiguration.
     */
    protected DataClassConfiguration instantiateDCC(int dataBytes){
        return (DCC == null) ? new DataClassConfiguration(dataBytes) : DCC;
    }


    /**
     * Create an RFile Singleton object.
     * 
     * @param dataBytes the size of the data in bytes
     * @param numOfRecords number of records to be inserted
     * @return The RFile object.
     */
    protected RFile instantiateRFile(int dataBytes, int numOfRecords) throws IOException {
        return (rf == null) ? new RFile("test_"+dataBytes+"-bytes"+"_"+numOfRecords+".bin", "rw", DCC.RECORD_SIZE) : rf;
    }

    /**
     * It generates a list of records with random keys and random data string of fixed length.
     * The number of records is specified by the {@code numOfRecords} field, and the length
     * of the string data is specified by the {@code dataBytes} field.
     * 
     * @return A list of records, where each record contains a unique key and a random string data of fixed length.
     */
    protected List<Record> generateRecords() {
        Random RNG = new Random();
        String[] data = new String[numOfRecords];

        int[] keys = RNG.ints(1, 2*numOfRecords+1).distinct().limit(numOfRecords).toArray();
        
        for(int i = 0 ; i < numOfRecords ; ++i)
            data[i] = RandomString.getAlphaNumericString(dataBytes);

        for(int i = 0; i< numOfRecords ; ++i)
            setRecord(new Record(keys[i], data[i]), i);
        
        return recordList;
    }

    /**
     * This function writes the data from the recordList to the file
     * 
     * @return The number of records that were written to the file.
     */
    protected int fillFile() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(dataBytes);
        long pos = 0;
        rf.getRaf().seek(pos);
        
        for(int i = 0; i < recordList.size(); ++i) {
            byte[] key = Utils.intToBytes(getRecord(i).getKey());
            bb.put(getRecord(i).getData().getBytes(StandardCharsets.US_ASCII));
            byte[] data = bb.array();
            System.arraycopy(key, 0, buffer, (i%RECORDS_IN_PAGE)*(dataBytes+Integer.BYTES), Integer.BYTES);
            System.arraycopy(data, 0, buffer, (i%RECORDS_IN_PAGE)*(dataBytes+Integer.BYTES) + Integer.BYTES, dataBytes);
            bb.clear();
            if(i%RECORDS_IN_PAGE == 0 && i!=0) {
                rf.getRaf().write(buffer);
                Arrays.fill(buffer, (byte) 0);
            }
        }

        rf.getRaf().write(buffer);
        Arrays.fill(buffer, (byte) 0);
        recordList.clear();
        return 0;
    }

    public DataClassConfiguration getDCC() {
        return DCC;
    }

    public int getDataBytes() {
        return dataBytes;
    }

    public void setDataBytes(int dataBytes) {
        this.dataBytes = dataBytes;
    }

    public int getNumOfRecords() {
        return numOfRecords;
    }

    public void setNumOfRecords(int numOfRecords) {
        this.numOfRecords = numOfRecords;
    }

    /**
     * This function returns the record at the specified index
     * 
     * @throws IndexOutOfBoundsException
     * @param index the index of the record to be returned
     * @return The record at the index.
     */
    public Record getRecord(int index) {
        if(index < 0 || index >= numOfRecords) 
            throw new IndexOutOfBoundsException("Array index out of bounds");

        return recordList.get(index);
    }

    /**
     * This function sets the record at the given index to the given record
     * 
     * @throws IndexOutOfBoundsException
     * @param r The record to be set
     * @param index the index of the record to be set
     */
    public void setRecord(Record r, int index) {
        if(index < 0)
            throw new IndexOutOfBoundsException("Array index out of bounds");
        
        this.recordList.add(index, r);
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }

    /**
     * This function removes a record from the recordList at the specified index
     * 
     * @param index The index of the record to be removed.
     * @throws IndexOutOfBoundsException
     */
    public void removeRecord(int index) {
        if(index < 0 || index >= recordList.size())
            throw new IndexOutOfBoundsException("Array index out of bounds");
        
        this.recordList.remove(index);
    }

    public RFile getRf() {
        return rf;
    }   
}
