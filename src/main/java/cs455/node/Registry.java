package cs455.node;

import cs455.DataSender;
import cs455.ServerThread;
import cs455.transport.Message;
import cs455.transport.RegisterRequest;
import cs455.util.Utils;
import cs455.wireformats.Protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


public class Registry implements Node {
    private int port = 50700;
    private ServerThread serverThread;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, DataSender> registeredNodes = new HashMap<>();
    private static final int MAX_NODE_ID = 256; // TODO figure this out

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
            default:
                throw new RuntimeException("received an unknown message");
        }
    }

    private void handleRegisterRequest(Message message) throws IOException {
        if (!(message instanceof RegisterRequest)) {
            Utils.error("message of " + message.getClass() + " unexpected");
            return;
        }

        RegisterRequest request = (RegisterRequest) message;
        Socket socket = request.getSocket();
        DataSender dataSender = DataSender.of(socket);
        String uniqueAddress = String.format("%s:%d", request.getIp(), request.getPort());

        synchronized (registeredNodes) {
            registeredNodes.entrySet().stream()
                .filter(e -> e.getKey().equals(uniqueAddress))
                .findFirst()
                .ifPresent(e -> {
                    sendRegisterResponse(dataSender, 1, "Registration request failed. Node previously registered.");
                    return;
                });

            if (!request.getIp().equals(socket.getInetAddress().getHostAddress())) {
                sendRegisterResponse(dataSender, 1, "Registration request failed. Mismatch in IP.");
                return;
            }

            registeredNodes.put(uniqueAddress, dataSender);
            String info = String.format("Registration request successful. The number of messaging nodes currently constituting the overlay is (%s).", registeredNodes.size());
            sendRegisterResponse(dataSender, 0, info);
        }
    }

    private void sendRegisterResponse(DataSender dataSender, int status, String info) {
        // TODO
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
