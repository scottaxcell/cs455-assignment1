package cs455.node;

import cs455.ServerThread;
import cs455.transport.Message;
import cs455.util.Utils;

public class Registry implements Node {
    private int port = 50700;
    private ServerThread serverThread;

    private Registry() {

    }

    private void start() {
        serverThread = ServerThread.of(port, this);
        serverThread.start();
    }

    private static Registry of(String[] args) {
        // TODO handle args - port-number
        return new Registry();
    }

    @Override
    public void onMessage(Message message) {
        // TODO handle messages
    }

    private static void printHelpAndExit() {
        Utils.out("USAGE: java cs455.node.Registry <port-number>\n");
        System.exit(-1);
    }

    public static void main(String[] args) {
//        if (args.length != 1)
//            printHelpAndExit();

        Registry registry = Registry.of(args);
        registry.start();
    }
}
