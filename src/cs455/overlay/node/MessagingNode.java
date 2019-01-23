package cs455.overlay.node;

import cs455.overlay.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MessagingNode {
    private String registryIp;
    private int registryPort;
    private ServerThread serverThread;
    private ReceiverThread registryReceiverThread;

    private MessagingNode(String registryIp, int registryPort) {
        this.registryIp = registryIp;
        this.registryPort = registryPort;
    }

    private static MessagingNode of(String registryHost, int registryPort) {
        return new MessagingNode(registryHost, registryPort);
    }

    private void start() {
        serverThread = ServerThread.of(0);
        serverThread.start();

        connectToRegistry();
    }

    private void connectToRegistry() {
        try (Socket registrySocket = new Socket(registryIp, registryPort)) {
            registryReceiverThread = ReceiverThread.of(registrySocket);
            registryReceiverThread.start();

            // test message
            String msg = "from messagingnode to registry, how did I do?";
            DataOutputStream dout = new DataOutputStream(registrySocket.getOutputStream());
            byte[] data = msg.getBytes();
            int dataLength = data.length;
            dout.writeInt(dataLength);
            dout.write(data, 0, dataLength);
            dout.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHelpAndExit() {
        Utils.out("USAGE: java cs455.overlay.node.MessagingNode <registry-host> <registry-port>\n");
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length != 2)
            printHelpAndExit();

        String registryIp = args[0];
        int registryPort = Integer.parseInt(args[1]);

        MessagingNode messagingNode = MessagingNode.of(registryIp, registryPort);
        messagingNode.start();
    }
}
