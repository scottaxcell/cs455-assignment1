package cs455.node;

import cs455.transport.TcpConnection;
import cs455.transport.TcpSender;
import cs455.transport.TcpServer;
import cs455.util.Utils;
import cs455.wireformats.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MessagingNode implements Node {
    private String registryIp;
    private int registryPort;
    private TcpConnection registryTcpConnection;
    private TcpServer tcpServer;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, TcpConnection> connectedNodes = new HashMap<>();

    private MessagingNode(String registryIp, int registryPort) {
        this.registryIp = registryIp;
        this.registryPort = registryPort;
    }

    private static MessagingNode of(String registryHost, int registryPort) {
        return new MessagingNode(registryHost, registryPort);
    }

    private void go() {
        tcpServer = TcpServer.of(0, this);
        Thread thread = new Thread(tcpServer);
        thread.start();

        connectToRegistry();
        sendRegisterRequest();
        handleCmdLineInput();
    }

    private void sendRegisterRequest() {
        // connectToRegistry() must be called before this, registrySender assumed to be valid
        // TODO appears tcpServer is not up and running at this point, need to wait for it
        try {
            Thread.sleep(500);
            RegisterRequest request = RegisterRequest.of(getIp(), tcpServer.getPort(), registryTcpConnection.getSocket());
            registryTcpConnection.send(request.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connectToRegistry() {
        try {
            Socket socket = new Socket(registryIp, registryPort);
            registryTcpConnection = TcpConnection.of(socket, this);
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
            case Protocol.MESSAGING_NODES_LIST:
                handleMessagingNodesList(message);
                break;
            case Protocol.HANDSHAKE:
                handleHandshake(message);
                break;
            case Protocol.LINK_WEIGHTS:
                handleLinkWeights(message);
                break;
            default:
                throw new RuntimeException(String.format("received an unknown message with protocol %d", protocol));
        }
    }

    private void handleLinkWeights(Message message) {
        if (!(message instanceof LinkWeights)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        LinkWeights linkWeights = (LinkWeights) message;
        Utils.debug("received: " + linkWeights);

        for (String link : linkWeights.getLinks()) {
            String[] split = link.split(" ");
            String source = split[0];
            String sink = split[1];
            int weight = Integer.parseInt(split[2]);

        }
        Utils.info("Link weights are received and processed. Ready to send messages.");
    }

    private void handleHandshake(Message message) {
        if (!(message instanceof Handshake)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        Handshake handshake = (Handshake) message;
        Utils.debug("received: " + handshake);
        Socket socket = handshake.getSocket();
        String address = String.format("%s:%d", handshake.getIp(), handshake.getPort());
        TcpConnection tcpConnection = TcpConnection.of(socket, this);
        connectedNodes.put(address, tcpConnection);
    }

    private void handleMessagingNodesList(Message message) {
        if (!(message instanceof MessagingNodesList)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        MessagingNodesList messagingNodesList = (MessagingNodesList) message;
        Utils.debug("received: " + messagingNodesList);

        for (String node : messagingNodesList.getNodes()) {
            String[] split = node.split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);

            try {
                Socket socket = new Socket(ip, port);
                TcpConnection tcpConnection = TcpConnection.of(socket, this);
                connectedNodes.put(node, tcpConnection);
                Handshake handshake = Handshake.of(getIp(), tcpServer.getPort(), tcpConnection.getSocket());
                tcpConnection.send(handshake.getBytes());
                Utils.debug(String.format("sent [%s]: %s", tcpConnection.getSocket().getRemoteSocketAddress(), handshake));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.info(String.format("All connections are established. Number of connections: %d", connectedNodes.size()));
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
            // TODO figure out how to close stream and socket cleanly so not to cause EOF
            // exception in registry when we exit
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
            DeregisterRequest request = DeregisterRequest.of(getIp(), tcpServer.getPort(), registryTcpConnection.getSocket());
            registryTcpConnection.send(request.getBytes());
            Utils.debug(String.format("sent [%s]: %s", registryTcpConnection.getSocket().getRemoteSocketAddress(), request));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getIp() throws UnknownHostException {
        return Inet4Address.getLocalHost().getHostAddress();
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
