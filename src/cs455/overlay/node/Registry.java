package cs455.overlay.node;

import cs455.overlay.Utils;

public class Registry {
    private ServerThread serverThread;

    private Registry() {

    }

    private void start() {
        serverThread = ServerThread.of(50700);
        serverThread.start();
    }

    private static Registry of(String[] args) {
        // TODO handle args - port-number
        return new Registry();
    }

    private static void printHelpAndExit() {
        Utils.out("USAGE: java cs455.overlay.node.Registry <port-number>\n");
        System.exit(-1);
    }
    public static void main(String[] args) {
//        if (args.length != 1)
//            printHelpAndExit();

        Registry registry = Registry.of(args);
        registry.start();
    }
}
