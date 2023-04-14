package utils;

import java.util.Arrays;

/**
 * The {@code Utils} class provides utility methods for various operations such as integer to byte array conversion,
 * byte array to integer conversion, integer array to byte array conversion, integer comparison, and matrix transposition.
 */
public class Utils {
    
    /** 
     * A private constructor that prevents the class from being instantiated.
     * 
     * @throws IllegalStateException if someone tries to instantiate the class.
    */
    private Utils() {
        throw new IllegalStateException("Utility Class");
    }

    //courtesy of: https://javadeveloperzone.com/java-basic/java-convert-int-to-byte-array/
    /**
     * It takes an integer and returns a byte array of length 4
     * 
     * @param data The integer to convert to bytes
     * @return The return value is a byte array.
     */
    public static byte[] intToBytes(int data) {
        return new byte[] {
            (byte)((data >> 24) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 8) & 0xff),
            (byte)((data >> 0) & 0xff),
        };
    }


    /**
     * It takes a byte array and returns the integer value of the byte array
     * 
     * @param data The byte array to convert to an int.
     * @return The return value is the integer value of the byte array.
     */
    public static int byteArrayToInt(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        return (int) (
                (0xff & data[0]) << 24  |
                (0xff & data[1]) << 16  |
                (0xff & data[2]) << 8   |
                (0xff & data[3]) << 0);
    }

     /**
     * It takes two integers, converts them to byte arrays, and then combines them into a single byte
     * array
     * 
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return A byte array of size 8.
     */
    public static byte[] intsToByteArray(int x, int y) {
        byte[] byteArray = new byte[2*Integer.BYTES];
        System.arraycopy(intToBytes(x), 0, byteArray, 0, 4);
        System.arraycopy(intToBytes(y), 0, byteArray, 4, 4);
        return byteArray;
    }


    /**
     * Converts a 2D integer array to byte array.
     * 
     * @param intArray The 2D integer array to convert.
     * @return The byte array.
     * @throws NullPointerException if the input array is null.
     * @throws IllegalArgumentException if the input array is empty.
     */
    public static byte[] intArrayToByteArray(int[][] intArray) {
        if(intArray == null)
            throw new NullPointerException("Input array must not be null");
        
        if(intArray.length == 0)
            throw new IllegalArgumentException("Input array must be non empty");
        
        byte[] byteArray = new byte[2*(intArray.length)*(Integer.BYTES)];
        
        Arrays.fill(byteArray, (byte) 0);
        for(int i = 0; i < byteArray.length; i+= 2*Integer.BYTES)
            System.arraycopy(intsToByteArray(intArray[i/(2*Integer.BYTES)][0], intArray[i/(2*Integer.BYTES)][1]), 0, byteArray, i, 2*Integer.BYTES);

        return byteArray;
    }

    /**
     * Compares two integers and returns the result of the comparison.
     * 
     * @param a The first number to compare.
     * @param b The second number to compare.
     * @return The method is returning the result of the comparison of the two integers.
     */
    public static int compare(int a, int b) {
        return a < b ? -1 : a > b ? 1 : 0;
    }

    /**
     * Matrix transposition method.
     * 
     * @param m the matrix to be transposed
     * @return The transpose of the matrix.
     */
    public static int[][] transpose(int[][] m) {
        int[][] k = new int[m[0].length][m.length];

        for(int i = 0; i < m[0].length; ++i)
            for(int j =0; j< m.length;++j)
                k[i][j] = m[j][i];
        
        return k;
    }


    
}
