package model;

public class Record {

    private int key;
    private String data;
    private int dataLength;
    private int recordLength;

    public Record(int key, String data) {
        this.key = key;
        this.data = data;
        this.dataLength = data.length();
        this.recordLength = data.length()+Integer.BYTES;
    }


    /* Getters-Setters*/
    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }


    public int getRecordLength() {
        return recordLength;
    }

}
