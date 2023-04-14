package modelTesting;

/* A record class that contains the necessary performance metrics. */
public record TestStructure (int dataBytes, int numOfRecords,
                             float discAccessCountA, long runtimeA,
                             float discAccessCountB, long runtimeB,
                             float discAccessCountC, long runtimeC){}
