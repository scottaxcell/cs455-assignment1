package cs455.node;

import cs455.DataSender;
import cs455.ReceiverThread;
import cs455.ServerThread;
import cs455.transport.*;
import cs455.util.Utils;
import cs455.wireformats.Protocol;
import cs455.wireformats.Status;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MessagingNode implements Node {
    private String registryIp;
    private int registryPort;
    private ServerThread serverThread;
    private ReceiverThread registryReceiverThread;
    private DataSender registrySender;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

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
        handleCmdLineInput();
    }

    private void sendRegisterRequest() {
        // connectToRegistry() must be called before this, registrySender assumed to be valid
        // TODO appears serverThread is not up and running at this point, need to wait for it
        try {
            Thread.sleep(500);
            RegisterRequest registerRequest = RegisterRequest.of(Inet4Address.getLocalHost().getHostAddress(), serverThread.getPort(), registryReceiverThread.getSocket());
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
        executor.execute(() -> handleMessage(message));
    }

    private void handleMessage(Message message) {
        int protocol = message.getProtocol();
        switch (protocol) {
            case Protocol.REGISTER_RESPONSE:
                handleRegisterResponse(message);
                break;
            case Protocol.DEREGISTER_RESPONSE:
                handleDeregisterResponse(message);
                break;
            default:
                throw new RuntimeException("received an unknown message");
        }
    }

    private void handleRegisterResponse(Message message) {
        if (!(message instanceof RegisterResponse)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        // TODO print if failure
        RegisterResponse response = (RegisterResponse) message;
        Utils.debug("received: " + response);
    }

    private void handleDeregisterResponse(Message message) {
        if (!(message instanceof DeregisterResponse)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        DeregisterResponse response = (DeregisterResponse) message;
        Utils.debug("received: " + response);

        // TODO print if failure
        if (response.getStatus() == Status.SUCCESS) {
            Utils.debug("shutting down..");
            System.exit(Status.SUCCESS);
        }
    }

    private void handleCmdLineInput() {
        // application loop
        String input;
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter(Pattern.compile("[\\r\\n;]+"));

        Utils.out("MessagingNode\n=============\n");

        while (true) {
            Utils.out("$ ");

            input = scanner.next();
            if (input.startsWith("print-shortest-path")) {
                // TODO
            }
            else if (input.startsWith("exit-overlay"))
                executor.execute(this::sendDeregistrationRequest);
        }
    }

    private void sendDeregistrationRequest() {
        try {
            DeregisterRequest request = DeregisterRequest.of(Inet4Address.getLocalHost().getHostAddress(), serverThread.getPort(), registryReceiverThread.getSocket());
            registrySender.send(request.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
