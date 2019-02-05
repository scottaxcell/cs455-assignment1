package cs455.util;

import java.util.Random;

public class Utils {
    private static boolean debug = false;

    public static void out(Object o) {
        System.out.print(o);
    }

    public static void info(Object o) {
        System.out.println("\nINFO: " + o);
    }

    public static void debug(Object o) {
        if (debug)
            System.out.println("DEBUG: " + o);
    }

    public static void error(Object o) {
        System.err.println("\nERROR: " + o);
    }

    public static byte intToByte(int i) {
        return (byte) i;
    }

    public static int generateRandomWeight() {
        return new Random().nextInt(10) + 1;
    }
}
