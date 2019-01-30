package cs455.node;

import cs455.transport.TcpSender;
import cs455.transport.TcpServer;
import cs455.util.OverlayCreator;
import cs455.util.Utils;
import cs455.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;


public class Registry implements Node {
    private int port = 50700;
    private TcpServer tcpServer;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, TcpSender> registeredNodes = new HashMap<>();
    private OverlayCreator overlayCreator;
    private int numCompletedNodes;

    private Registry() {
    }

    private void go() {
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
    public void onEvent(Event event) {
        executor.execute(() -> {
            try {
                handleEvent(event);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleEvent(Event event) throws IOException {
        int protocol = event.getProtocol();
        switch (protocol) {
            case Protocol.REGISTER_REQUEST:
                handleRegisterRequest(event);
                break;
            case Protocol.DEREGISTER_REQUEST:
                handleDeregisterRequest(event);
                break;
            case Protocol.TASK_COMPLETE:
                handleTaskComplete(event);
                break;
            default:
                throw new RuntimeException(String.format("received an unknown event with protocol %d", protocol));
        }
    }

    private void handleTaskComplete(Event event) {
        if (!(event instanceof TaskComplete)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        TaskComplete taskComplete = (TaskComplete) event;
        Utils.debug("received: " + taskComplete);

        numCompletedNodes++;
        if (numCompletedNodes == registeredNodes.size()) {
            PullTrafficSummary pullTrafficSummary = PullTrafficSummary.of();
            for (TcpSender tcpSender : registeredNodes.values()) {
                try {
                    tcpSender.send(pullTrafficSummary.getBytes());
                    Utils.debug(String.format("sent [%s]: %s", tcpSender.getSocket().getRemoteSocketAddress(), pullTrafficSummary));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleDeregisterRequest(Event event) throws IOException {
        if (!(event instanceof DeregisterRequest)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        DeregisterRequest request = (DeregisterRequest) event;
        Utils.debug("received: " + request);
        Socket socket = request.getSocket();
        TcpSender tcpSender = TcpSender.of(socket);
        String address = String.format("%s:%d", request.getIp(), request.getPort());

        if (!request.getIp().equals(socket.getInetAddress().getCanonicalHostName())) {
            if (socket.getInetAddress().getCanonicalHostName().equals("localhost")) {
                // handle scenario where messaging node exists on the same machine
                if (!request.getIp().equals(tcpServer.getIp())) {
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

    private void handleRegisterRequest(Event event) throws IOException {
        if (!(event instanceof RegisterRequest)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        RegisterRequest request = (RegisterRequest) event;
        Utils.debug("received: " + request);
        Socket socket = request.getSocket();
        TcpSender tcpSender = TcpSender.of(socket);
        String address = String.format("%s:%d", request.getIp(), request.getPort());

        if (!request.getIp().equals(socket.getInetAddress().getCanonicalHostName())) {
            if (socket.getInetAddress().getCanonicalHostName().equals("localhost")) {
                // handle scenario where messaging node exists on the same machine
                if (!request.getIp().equals(tcpServer.getIp())) {
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
                listMessagingNodes();
            }
            else if (input.startsWith("list-weights")) {
                listWeights();
            }
            else if (input.startsWith("setup-overlay")) {
                try {
                    setupOverlay(Integer.parseInt(input.split(" ")[1]));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            else if (input.startsWith("send-overlay-link-weights")) {
                sendLinkWeights();
            }
            else if (input.startsWith("start")) {
                try {
                    start(Integer.parseInt(input.split(" ")[1]));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void start(int numRounds) {
        TaskInitiate taskInitiate = TaskInitiate.of(numRounds);
        for (TcpSender tcpSender : registeredNodes.values()) {
            try {
                tcpSender.send(taskInitiate.getBytes());
                Utils.debug(String.format("sent [%s]: %s", tcpSender.getSocket().getRemoteSocketAddress(), taskInitiate));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listWeights() {
        if (overlayCreator == null) {
            Utils.error("failed to print link weights. setup-overlay <num> expected to be called first.");
            return;
        }
        overlayCreator.getLinks().stream()
            .forEach(l -> Utils.out(String.format("%s %s %d\n", l.getSource(), l.getSink(), l.getWeight())));
    }

    private void listMessagingNodes() {
        registeredNodes.keySet().stream()
            .forEach(n -> Utils.out(String.format("%s\n", n)));
    }

    private void sendLinkWeights() {
        if (overlayCreator == null) {
            Utils.error("failed to send link weights. setup-overlay <num> expected to be called first.");
            return;
        }
        String[] linkWeights = overlayCreator.getLinkWeights();
        LinkWeights msg = LinkWeights.of(linkWeights);
        for (TcpSender tcpSender : registeredNodes.values()) {
            try {
                tcpSender.send(msg.getBytes());
                Utils.debug(String.format("sent [%s]: %s", tcpSender.getSocket().getRemoteSocketAddress(), msg));
            }
            catch (IOException e) {
                e.printStackTrace();
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
        registry.go();
    }
}
