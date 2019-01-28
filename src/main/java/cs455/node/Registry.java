package cs455.node;

import cs455.transport.TcpSender;
import cs455.transport.TcpServer;
import cs455.util.OverlayCreator;
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


public class Registry implements Node {
    public static final String LOOPBACK_IP = "127.0.0.1";
    private int port = 50700;
    private TcpServer tcpServer;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, TcpSender> registeredNodes = new HashMap<>();
    private OverlayCreator overlayCreator;

    private Registry() {
    }

    private void start() {
        tcpServer = TcpServer.of(port, this);
        Thread thread = new Thread(tcpServer);
        thread.start();
        handleCmdLineInput();
    }

    private static Registry of(String[] args) {
        // TODO handle args - port-number
        return new Registry();
    }

    @Override
    public void onMessage(Message message) {
        executor.execute(() -> {
            try {
                handleMessage(message);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleMessage(Message message) throws IOException {
        int protocol = message.getProtocol();
        switch (protocol) {
            case Protocol.REGISTER_REQUEST:
                handleRegisterRequest(message);
                break;
            case Protocol.DEREGISTER_REQUEST:
                handleDeregisterRequest(message);
                break;
            default:
                throw new RuntimeException(String.format("received an unknown message with protocol %d", protocol));
        }
    }

    private void handleDeregisterRequest(Message message) throws IOException {
        if (!(message instanceof DeregisterRequest)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        DeregisterRequest request = (DeregisterRequest) message;
        Utils.debug("received: " + request);
        Socket socket = request.getSocket();
        TcpSender tcpSender = TcpSender.of(socket);
        String address = String.format("%s:%d", request.getIp(), request.getPort());

        if (!request.getIp().equals(socket.getInetAddress().getHostAddress())) {
            if (socket.getInetAddress().getHostAddress().equals(LOOPBACK_IP)) {
                // handle scenario where messaging node exists on the same machine
                if (!request.getIp().equals(getIp())) {
                    sendMismatchedIpDeregisterResponse(tcpSender);
                    return;
                }
            }
            else {
                sendMismatchedIpDeregisterResponse(tcpSender);
                return;
            }
        }

        synchronized (registeredNodes) {
            if (!registeredNodes.containsKey(address)) {
                sendNodeNotKnownDeregisteredResponse(tcpSender);
                return;
            }

            registeredNodes.remove(address);
            sendSuccessDeregisterResponse(tcpSender);
        }
    }

    private String getIp() throws UnknownHostException {
        return Inet4Address.getLocalHost().getHostAddress();
    }

    private void sendSuccessDeregisterResponse(TcpSender tcpSender) {
        String info = String.format("Deregistration request successful. The number of messaging nodes currently constituting the overlayCreator is (%s).", registeredNodes.size());
        sendDeregisterResponse(tcpSender, Status.SUCCESS, info);
    }

    private void sendNodeNotKnownDeregisteredResponse(TcpSender tcpSender) {
        sendDeregisterResponse(tcpSender, Status.FAILURE, "Deregistration request failed. Node not previously registered.");
    }

    private void sendMismatchedIpDeregisterResponse(TcpSender tcpSender) {
        sendDeregisterResponse(tcpSender, Status.FAILURE, "Deregistration request failed. Mismatch in IP.");
    }

    private void sendDeregisterResponse(TcpSender tcpSender, int status, String info) {
        DeregisterResponse response = DeregisterResponse.of(status, info);
        try {
            tcpSender.send(response.getBytes());
            Utils.debug(String.format("sent [%s]: %s", tcpSender.getSocket().getRemoteSocketAddress(), response));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRegisterRequest(Message message) throws IOException {
        if (!(message instanceof RegisterRequest)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        RegisterRequest request = (RegisterRequest) message;
        Utils.debug("received: " + request);
        Socket socket = request.getSocket();
        TcpSender tcpSender = TcpSender.of(socket);
        String address = String.format("%s:%d", request.getIp(), request.getPort());

        if (!request.getIp().equals(socket.getInetAddress().getHostAddress())) {
            if (socket.getInetAddress().getHostAddress().equals(LOOPBACK_IP)) {
                // handle scenario where messaging node exists on the same machine
                if (!request.getIp().equals(getIp())) {
                    sendMismatchedIpRegisterResponse(tcpSender);
                    return;
                }
            }
            else {
                sendMismatchedIpRegisterResponse(tcpSender);
                return;
            }
        }

        synchronized (registeredNodes) {
            if (registeredNodes.containsKey(address)) {
                sendNodeAlreadyRegisteredResponse(tcpSender);
                return;
            }

            registeredNodes.put(address, tcpSender);
            sendSuccessRegisterResponse(tcpSender);
        }
    }

    private void sendNodeAlreadyRegisteredResponse(TcpSender tcpSender) {
        sendRegisterResponse(tcpSender, Status.FAILURE, "Registration request failed. Node previously registered.");
    }

    private void sendSuccessRegisterResponse(TcpSender tcpSender) {
        String info = String.format("Registration request successful. The number of messaging nodes currently constituting the overlayCreator is (%s).", registeredNodes.size());
        sendRegisterResponse(tcpSender, Status.SUCCESS, info);
    }

    private void sendMismatchedIpRegisterResponse(TcpSender tcpSender) {
        sendRegisterResponse(tcpSender, Status.FAILURE, "Registration request failed. Mismatch in IP.");
    }

    private void sendRegisterResponse(TcpSender tcpSender, int status, String info) {
        RegisterResponse response = RegisterResponse.of(status, info);
        try {
            tcpSender.send(response.getBytes());
            Utils.debug(String.format("sent [%s]: %s", tcpSender.getSocket().getRemoteSocketAddress(), response));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCmdLineInput() {
        String input;
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter(Pattern.compile("[\\r\\n;]+"));

        Utils.out("Registry\n========\n");

        while (true) {
            Utils.out("$ ");

            input = scanner.next();

            if (input.startsWith("list-messaging-nodes")) {
                // TODO
            }
            else if (input.startsWith("list-weights")) {
                // TODO
            }
            else if (input.startsWith("setup-overlay")) {
                int numConnections = Integer.parseInt(input.split(" ")[1]);
                setupOverlay(numConnections);
            }
            else if (input.startsWith("send-overlay-link-weights")) {
                // TODO
            }
            else if (input.startsWith("start")) {
                // TODO
            }
        }
    }

    private void setupOverlay(int cr) {
        overlayCreator = OverlayCreator.of(registeredNodes.keySet(), cr);
        if (!overlayCreator.setup()) {
            Utils.error("failed to generate the overlay");
            return;
        }

        for (String node : registeredNodes.keySet()) {
            executor.execute(() -> sendMessagingNodesList(node));
        }
    }

    private void sendMessagingNodesList(String node) {
        String[] nodes = overlayCreator.getNodeConnections(node);
        MessagingNodesList messagingNodesList = MessagingNodesList.of(nodes);
        try {
            registeredNodes.get(node).send(messagingNodesList.getBytes());
            Utils.debug(String.format("sent [%s]: %s", registeredNodes.get(node).getSocket().getRemoteSocketAddress(), messagingNodesList));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
