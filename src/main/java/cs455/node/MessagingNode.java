package cs455.node;

import cs455.dijkstra.RoutingCache;
import cs455.transport.TcpConnection;
import cs455.transport.TcpServer;
import cs455.util.Link;
import cs455.util.TrafficTracker;
import cs455.util.Utils;
import cs455.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MessagingNode implements Node {
    private static final int NUM_MESSAGES_TO_SEND = 5;
    private String registryIp;
    private int registryPort;
    private TcpConnection registryTcpConnection;
    private TcpServer tcpServer;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, TcpConnection> connectedNodes = new HashMap<>();
    private RoutingCache routingCache;
    private TrafficTracker trafficTracker = TrafficTracker.of();

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
            RegisterRequest request = RegisterRequest.of(tcpServer.getIp(), tcpServer.getPort(), registryTcpConnection.getSocket());
            registryTcpConnection.send(request.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connectToRegistry() {
        try {
            Socket socket = new Socket(registryIp, registryPort);
            registryTcpConnection = TcpConnection.of(socket, this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(Event event) {
        executor.execute(() -> handleEvent(event));
    }

    private void handleEvent(Event event) {
        int protocol = event.getProtocol();
        switch (protocol) {
            case Protocol.REGISTER_RESPONSE:
                handleRegisterResponse(event);
                break;
            case Protocol.DEREGISTER_RESPONSE:
                handleDeregisterResponse(event);
                break;
            case Protocol.MESSAGING_NODES_LIST:
                handleMessagingNodesList(event);
                break;
            case Protocol.HANDSHAKE:
                handleHandshake(event);
                break;
            case Protocol.LINK_WEIGHTS:
                handleLinkWeights(event);
                break;
            case Protocol.TASK_INITIATE:
                handleTaskInitiative(event);
                break;
            case Protocol.MESSAGE:
                handleMessage(event);
                break;
            case Protocol.PULL_TRAFFIC_SUMMARY:
                handlePullTrafficSummary(event);
                break;
            default:
                throw new RuntimeException(String.format("received an unknown event with protocol %d", protocol));
        }
    }

    private void handlePullTrafficSummary(Event event) {
        if (!(event instanceof PullTrafficSummary)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        PullTrafficSummary pullTrafficSummary = (PullTrafficSummary) event;
        Utils.debug("received: " + pullTrafficSummary);

        TrafficSummary trafficSummary = TrafficSummary.of(tcpServer.getIp(), tcpServer.getPort(),
            trafficTracker.getSendTracker(), trafficTracker.getSendSummation(),
            trafficTracker.getReceiveTracker(), trafficTracker.getReceiveSummation(),
            trafficTracker.getRelayTracker());

        try {
            registryTcpConnection.send(trafficSummary.getBytes());
            Utils.debug(String.format("sent [%s]: %s", registryTcpConnection.getSocket().getRemoteSocketAddress(), trafficSummary));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        trafficTracker.reset();
    }

    private void handleMessage(Event event) {
        if (!(event instanceof Message)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        Message message = (Message) event;
        Utils.debug("received: " + message);

        int payload = message.getPayload();
        String destination = message.getDestination();
        if (getName().equals(destination)) {
            trafficTracker.incrementReceiveTracker();
            trafficTracker.addReceiveSummation(payload);
        }
        else {
            String nextHop = routingCache.getNextHop(destination);
            TcpConnection tcpConnection = connectedNodes.get(nextHop);
            message = Message.of(payload, nextHop);
            trafficTracker.incrementRelayTracker();
            try {
                tcpConnection.send(message.getBytes());
                Utils.debug(String.format("sent [%s]: %s", tcpConnection.getSocket().getRemoteSocketAddress(), message));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleTaskInitiative(Event event) {
        if (!(event instanceof TaskInitiate)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        TaskInitiate taskInitiate = (TaskInitiate) event;
        Utils.debug("received: " + taskInitiate);

        int numRounds = taskInitiate.getNumRounds();
        List<String> nodes = routingCache.getAllOtherNodes();
        for (int i = 0; i < numRounds; i++) {
            int randomIndex = new Random().nextInt(nodes.size());
            String randomNode = nodes.get(randomIndex);
            String nextHop = routingCache.getNextHop(randomNode);
            TcpConnection tcpConnection = connectedNodes.get(nextHop);

            for (int j = 0; j < NUM_MESSAGES_TO_SEND; j++) {
                int payload = new Random().nextInt();
                Message message = Message.of(payload, randomNode);
                trafficTracker.incrementSendTracker();
                trafficTracker.addSendSummation(payload);
                try {
                    tcpConnection.send(message.getBytes());
                    Utils.debug(String.format("sent [%s]: %s", tcpConnection.getSocket().getRemoteSocketAddress(), message));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        TaskComplete taskComplete = TaskComplete.of(tcpServer.getIp(), tcpServer.getPort());
        try {
            registryTcpConnection.send(taskComplete.getBytes());
            Utils.debug(String.format("sent [%s]: %s", registryTcpConnection.getSocket().getRemoteSocketAddress(), taskComplete));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLinkWeights(Event event) {
        if (!(event instanceof LinkWeights)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        LinkWeights linkWeights = (LinkWeights) event;
        Utils.debug("received: " + linkWeights);

        List<Link> links = new ArrayList<>();
        for (String link : linkWeights.getLinks()) {
            String[] split = link.split(" ");
            String source = split[0];
            String sink = split[1];
            int weight = Integer.parseInt(split[2]);
            links.add(Link.of(source, sink, weight));
        }
        routingCache = RoutingCache.of(getName(), links.toArray(new Link[links.size()]));
        Utils.info("Link weights are received and processed. Ready to send messages.");
    }

    private void handleHandshake(Event event) {
        if (!(event instanceof Handshake)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        Handshake handshake = (Handshake) event;
        Utils.debug("received: " + handshake);
        Socket socket = handshake.getSocket();
        String address = String.format("%s:%d", handshake.getIp(), handshake.getPort());
        TcpConnection tcpConnection = TcpConnection.of(socket, this);
        connectedNodes.put(address, tcpConnection);
    }

    private void handleMessagingNodesList(Event event) {
        if (!(event instanceof MessagingNodesList)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        MessagingNodesList messagingNodesList = (MessagingNodesList) event;
        Utils.debug("received: " + messagingNodesList);

        for (String node : messagingNodesList.getNodes()) {
            String[] split = node.split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);

            try {
                Socket socket = new Socket(ip, port);
                TcpConnection tcpConnection = TcpConnection.of(socket, this);
                connectedNodes.put(node, tcpConnection);
                Handshake handshake = Handshake.of(tcpServer.getIp(), tcpServer.getPort(), tcpConnection.getSocket());
                tcpConnection.send(handshake.getBytes());
                Utils.debug(String.format("sent [%s]: %s", tcpConnection.getSocket().getRemoteSocketAddress(), handshake));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.info(String.format("All connections are established. Number of connections: %d", connectedNodes.size()));
    }

    private void handleRegisterResponse(Event event) {
        if (!(event instanceof RegisterResponse)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        // TODO print if failure
        RegisterResponse response = (RegisterResponse) event;
        Utils.debug("received: " + response);
    }

    private void handleDeregisterResponse(Event event) {
        if (!(event instanceof DeregisterResponse)) {
            Utils.error("event of " + event.getClass() + " unexpected");
            return;
        }

        DeregisterResponse response = (DeregisterResponse) event;
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
                printShortestPath();
            }
            else if (input.startsWith("exit-overlay"))
                executor.execute(this::sendDeregistrationRequest);
        }
    }

    private void printShortestPath() {
        Utils.out(routingCache.getShortestPathsForPrinting());
    }

    private void sendDeregistrationRequest() {
        try {
            DeregisterRequest request = DeregisterRequest.of(tcpServer.getIp(), tcpServer.getPort(), registryTcpConnection.getSocket());
            registryTcpConnection.send(request.getBytes());
            Utils.debug(String.format("sent [%s]: %s", registryTcpConnection.getSocket().getRemoteSocketAddress(), request));
        }
        catch (IOException e) {
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

    private String getName() {
        return String.format("%s:%d", tcpServer.getIp(), tcpServer.getPort());
    }
}
