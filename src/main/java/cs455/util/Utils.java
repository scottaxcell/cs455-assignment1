package cs455.util;

public class Utils {
    public static void out(Object o) {
        System.out.print(o);
    }

    public static void info(Object o) {
        System.out.println("INFO: " + o);
    }

    public static void debug(Object o) {
        System.out.println("DEBUG: " + o);
    }

    public static void error(Object o) {
        System.err.println("ERROR: " + o);
    }
}
