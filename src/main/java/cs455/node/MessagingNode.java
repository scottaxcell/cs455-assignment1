package cs455.node;

import cs455.DataSender;
import cs455.ReceiverThread;
import cs455.ServerThread;
import cs455.transport.Message;
import cs455.transport.RegisterRequest;
import cs455.util.Utils;

import java.io.IOException;
import java.net.Socket;

public class MessagingNode implements Node {
    private String registryIp;
    private int registryPort;
    private ServerThread serverThread;
    private ReceiverThread registryReceiverThread;
    private DataSender registrySender;

    private MessagingNode(String registryIp, int registryPort) {
        this.registryIp = registryIp;
        this.registryPort = registryPort;
    }

    private static MessagingNode of(String registryHost, int registryPort) {
        return new MessagingNode(registryHost, registryPort);
    }

    private void go() {
        serverThread = ServerThread.of(0, this);
        serverThread.start();

        connectToRegistry();
        sendRegisterRequest();
    }

    private void sendRegisterRequest() {
        // connectToRegistry must be called before this, registrySender assumed to be valid
        // TODO appears serverThread is not up and running at this point, need to wait for it
        try {
            Thread.sleep(500);
            RegisterRequest registerRequest = RegisterRequest.of(serverThread.getIp(), serverThread.getPort());
            registrySender.send(registerRequest.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connectToRegistry() {
        try {
            Socket registrySocket = new Socket(registryIp, registryPort);
            registryReceiverThread = ReceiverThread.of(registrySocket, this);
            registryReceiverThread.start();
            registrySender = DataSender.of(registrySocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        // TODO handle messages
    }

    private static void printHelpAndExit() {
        Utils.out("USAGE: java cs455.node.MessagingNode <registry-host> <registry-port>\n");
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length != 2)
            printHelpAndExit();

        String registryIp = args[0];
        int registryPort = Integer.parseInt(args[1]);

        MessagingNode messagingNode = MessagingNode.of(registryIp, registryPort);
        messagingNode.go();
    }
}
