package pl.edu.agh.firecell.engine.utils;

public class cCodeManager {

    static {
        System.loadLibrary("libCppUtils");
    }

    public native void print();
    public native double multiplication(double a, double b);

}
