package model;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import utils.Utils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 *
 *   The {@code RFile} class represents a file abstraction, where you can perform operations
 *   of searching for a specific key, splitting the file into an index file and sorting the
 *   index file.
 *   
 *   @author nrouli
 *   @since 2023-03
 */
public class RFile implements Closeable {
    
    /* The name of the RFile */
    private String filename;

    /* The file associated with the RFile */
    private final File file;

    /* The RandomAccessFile object used to read from and write to the file */
    private RandomAccessFile raf = null;
    
    /* The size of the record in the file (31 or 59 bytes)*/
    private int recordSize;

    /* The index file associated with the RFile */
    private RFile indexFile;

    /* data buffer */
    private final byte[] buffer = new byte[DataClassConfiguration.PAGE_SIZE];

    /*List of keys and indices taken from the Index File */
    private List<FIndex> keyIndex;

    /**
     * Creates a new instance of the {@code RFile} class with the specified file name, mode, and record size.
     * 
     * @param filename the file name to be read
     * @param mode the mode to be used
     * @param recordSize the size of the record
     * @throws IOException if an I/O error occurs
     */
    public RFile(String filename, String mode, int recordSize) throws IOException {
        this.filename = filename;
        this.recordSize = recordSize;
        file = new File("files/"+this.filename);
        raf = instantiateRAF(file, mode);
        keyIndex = new ArrayList<>();
    }

    /**
     * Instantiate a new RandomAccessFile with the given file and mode.
     * 
     * @param file The file to be read.
     * @param mode The mode to be used.
     * @return A new RandomAccessFile object.
     * @throws IOException if an I/O error occurs.
     */
    protected RandomAccessFile instantiateRAF(File file, String mode) throws IOException {
        return raf == null ? new RandomAccessFile(file, mode) : raf;
    }


    /**
     * Instantiates a new RFile with the given filename, mode, and record size.
     * 
     * @param filename the filename to be read
     * @param mode the mode to be used
     * @param recordSize the size of the record
     * @return a new RFile object
     * @throws IOException if an I/O error occurs
     */
    protected RFile instantiateRFile(String filename, String mode, int recordSize) throws IOException {
        return indexFile == null ? new RFile(filename, mode, recordSize) : indexFile;
    }

    /**
     * <p>
     * Searches the file page by page and checks if the elements are in the page. 
     * It returns the total disk accesses.
     * </p>
     * <p>
     * This function is tested on {@link modelTesting.TestGenerator#randomSearchCounter randomSearchCounter}
     * and also in {@link modelTesting.TestGenerator#randomSearchCounterIndexFile randomIndexSearchCounter}
     * </p>
     * @param key the unique key of a record
     * @return an array of two integers. The first integer is either 0 or 1, depending on whether the elements
     *         are found in the file. The second integer is the total number of disk accesses
     * @throws IOException if an I/O error occurs
     */
    public int searchFile(int key) throws IOException {
        int diskAccesses = 0;
        int pos = 0;
        byte[] page = new byte[DataClassConfiguration.PAGE_SIZE];

        raf.seek(0);
        while(pos < raf.length()) {
            Arrays.fill(page, (byte) 0);
            raf.read(page);
            diskAccesses++;
            if(isInPage(key, page)) {
                return diskAccesses;
            }
            pos+= DataClassConfiguration.PAGE_SIZE;
            raf.seek(pos);
        }

        return diskAccesses;
    }
    
    /**
     * It takes a key and a record, and returns true if the key is in the record
     * 
     * @param key the key to search for
     * @param rec the record to be checked
     * @return The key is being returned.
     */
    public boolean isInRecord(int key, byte[] rec) {
        byte[] buf = Arrays.copyOfRange(rec, 0, Integer.BYTES);
        return key == Utils.byteArrayToInt(buf);
    }

    /**
     * This function takes a key and a page as input, and returns true if the key is in the page, and
     * false otherwise
     * 
     * @param key the key to search for
     * @param page the page to search
     * @return A boolean value.
     */
    public boolean isInPage(int key, byte[] page) {
        for(int pos = 0; pos < page.length; pos += recordSize){
            byte[] rec = Arrays.copyOfRange(page, pos, pos+recordSize);
            if(isInRecord(key, rec)) return true;
        }

        return false;
    }

    /**
     * It reads the index file and performs a binary search on it to find the key.
     * 
     * @param key the key to search for
     * @return The number of disk accesses.
     */
    public int binarySearch(int key) throws IOException {
        int diskAccesses = 0;
        int pos = 0;
        byte[] keyBytes = new byte[Integer.BYTES];
        
        indexFile = instantiateRFile("IndexFiles/indexFile_"+this.filename, "rw", recordSize);
        int EOF = (int) indexFile.getRaf().length();
        indexFile.getRaf().seek(0);
        
        while(pos <= EOF) {
            int mid = (pos + EOF)/2;
            
            if(mid%(2*Integer.BYTES) != 0)
                mid += (mid%(2*Integer.BYTES));
            
            indexFile.getRaf().seek(mid);
            indexFile.getRaf().read(keyBytes);
            diskAccesses++;
            
            if(Utils.byteArrayToInt(keyBytes) == key)
                return diskAccesses;
            else if(Utils.byteArrayToInt(keyBytes) < key)
                pos = mid + 2*Integer.BYTES;
            else
                EOF = mid - 2*Integer.BYTES;
        }

        return diskAccesses;
    }

    /**
     * Binary search algorithm implementation on file pages.
     * This function is tested on {@link modelTesting.TestGenerator#randomBinarySearchCounter randomBinarySearchCounter}
     * 
     * @param key
     * @return the number of disk accesses in the index file.
     * @throws IOException
     */
    public int binarySearch2(int key) throws IOException {
        int diskAccesses = 0;
        int pos = 0;
        indexFile = instantiateRFile("IndexFiles/indexFile_"+this.filename, "rw", recordSize);
        int EOF = (int) indexFile.getRaf().length();

        while(pos <= EOF) {
            Arrays.fill(buffer, (byte) 0);
            int mid = (pos + EOF)/2;
            
            keyIndex.clear();
            indexFile.getRaf().seek(mid);
            indexFile.getRaf().read(buffer);
            diskAccesses++;
            readPage(buffer);

            if(keyIndex.isEmpty())
                return diskAccesses;
            if(binarySearchList(key) == 1)
                return diskAccesses;
            else if(keyIndex.get(keyIndex.size()-1).key() < key)
                pos = mid + DataClassConfiguration.PAGE_SIZE;
            else if(keyIndex.get(0).key() > key)
                EOF = mid - DataClassConfiguration.PAGE_SIZE;
            else if(inRange(key))
                break;
   
        }


        return diskAccesses;
    }
    
    /**
     * Checks if the given key is in range of keys in a list.
     * 
     * @param key the key to search for
     * @return The keyIndex is being returned.
     */
    private boolean inRange(int key) {
        return keyIndex.get(0).key() < key && keyIndex.get(keyIndex.size()-1).key() > key;
    }

    /**
     * Performs binary search algorithm in the list {@link model.RFile#keyIndex keyIndex}
     * for a given key.
     * 
     * @param key the key to search for
     * @return The index of the key in the keyIndex list.
     */
    private int binarySearchList(int key) {
        int left = 0;
        int right = keyIndex.size()-1;
        int mid = 0;

        while(left <= right) {
            mid = (left+right)/2;
            if(keyIndex.get(mid).key() == key)
                return 1;
            else if(keyIndex.get(mid).key() < key)
                left = mid + 1;
            else
                right = mid - 1;
        } 

        return 0;
    }

    /**
     * It reads a file, extracts the keys from the records, and writes them to a new file.
     *
     *@return A new RFile object representing the index file.
     *@throws IOException if there is an I/O error while accessing the files.
     */
    public RFile split() throws IOException {
        int pos = 0;
        
        assert keyIndex.isEmpty();

        byte[] page = new byte[DataClassConfiguration.PAGE_SIZE];
        indexFile = instantiateRFile("IndexFiles/indexFile_"+this.filename, "rw", recordSize);
        
        raf.seek(0);
        indexFile.getRaf().seek(0);
        while(pos < raf.length()) {
            raf.read(page);
            extractInts(page, pos);
            pos += DataClassConfiguration.PAGE_SIZE;
            raf.seek(pos);
        }

        putInts();
        return indexFile;
    }

    /**
     * Writes the key and index values from the {@link model.RFile#keyIndex keyIndex}
     * list to the index file. This method uses an array buffer to minimize the number
     * of disk writes required.
     * <p>
     * Used in {@link model.RFile#split() split} and {@link model.RFile#sort() sort}.
     * </p>
     *
     * @throws IOException if an I/O error occurs while writing to the index file
     */
    public void putInts() throws IOException{
        int index=0;

        indexFile.getRaf().seek(0);
        while(!keyIndex.isEmpty()) {
            System.arraycopy(Utils.intsToByteArray(keyIndex.get(0).key(), keyIndex.get(0).index()),0, buffer, index, 2*Integer.BYTES);
            index+= 2*Integer.BYTES;
            if(buffer.length - index < 2*Integer.BYTES) {
                index = 0;
                indexFile.getRaf().write(buffer);
                Arrays.fill(buffer, (byte) 0);
            }
            keyIndex.remove(0);
        }

        indexFile.getRaf().write(buffer);
        Arrays.fill(buffer, (byte) 0);
    }

    /**
     * Sorts the index file based on the key in each record.
     * 
     * @throws IOException If there is an error reading or writing to the index file.
     *
     */
    public void sort() throws IOException {
        int pos = 0;
        long EOF = indexFile.raf.length();
        byte[] page = new byte[DataClassConfiguration.PAGE_SIZE];

        assert keyIndex.isEmpty();

        indexFile.getRaf().seek(0);
        while(pos < EOF) {
            indexFile.getRaf().read(page);
            readPage(page);
            pos += DataClassConfiguration.PAGE_SIZE;
            indexFile.getRaf().seek(pos);
        }

        Collections.sort(keyIndex, (k1, k2) -> Utils.compare(k1.key(), k2.key()));  
        putInts();
    }

    /**
     * It reads a page of data from the Index File into 
     * {@link model.RFile#keyIndex keyIndex} list.
     * <p>
     * Used in {@link model.RFile#sort() sort} and {@link model.RFile#binarySearch2(int key) binary search}
     * </p>
     * 
     * @param page the byte array that contains the data
     * @return A list of FIndex objects.
     */
    public List<FIndex> readPage(byte[] page) {
        if(page == null) throw new NullPointerException("page should not be null");

        int pos = 0;
        for(int i = 0; i < page.length/(2*Integer.BYTES); i++) {
            if(Utils.byteArrayToInt(Arrays.copyOfRange(page, pos, pos+Integer.BYTES)) != 0)
                keyIndex.add(new FIndex(Utils.byteArrayToInt(Arrays.copyOfRange(page, pos, pos + Integer.BYTES)),
                                        Utils.byteArrayToInt(Arrays.copyOfRange(page, pos + Integer.BYTES, pos + 2*Integer.BYTES))));
            pos += 2*Integer.BYTES;
        }
        return keyIndex;
    }

    /**
     * It takes a byte array and an integer, and returns a list of Keys and Indices.
     * 
     * @param page the byte array of the page.
     * @param fileIndex the position of the key in the file.
     * @return A List of {@code FIndex} objects.
     */
    private List<FIndex> extractInts(byte[] page, int fileIndex) {
        if(page == null) throw new NullPointerException("page should not be null");

        int pos = 0;
        for(int i = 0; i < page.length/recordSize; i++) {
            if(Utils.byteArrayToInt(Arrays.copyOfRange(page, pos, pos+Integer.BYTES)) != 0)
                keyIndex.add(new FIndex(Utils.byteArrayToInt(Arrays.copyOfRange(page, pos, pos + Integer.BYTES)), pos + fileIndex));

            pos += recordSize;
        }
        return keyIndex;
    }

    /**
     * This function closes the file
     * 
     * <blockquote>
     * <pre>
     *  raf.close();
     *  getIndexFile().getRaf().close();
     * </pre>
     * </blockquote>
     * 
     */
    @Override
    public void close() throws IOException {
        this.raf.close();
        this.getIndexFile().getRaf().close();
    }

    public File getFile() {
        return file;
    }

    public RandomAccessFile getRaf() {
        return raf;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public String getFilename() {
        return filename;
    }

    public RFile getIndexFile() {
        return indexFile;
    }

}
