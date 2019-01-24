package cs455.node;

import cs455.DataSender;
import cs455.ServerThread;
import cs455.transport.Message;
import cs455.transport.RegisterRequest;
import cs455.transport.RegisterResponse;
import cs455.util.Utils;
import cs455.wireformats.Protocol;
import cs455.wireformats.Status;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


public class Registry implements Node {
    public static final String LOOPBACK_IP = "127.0.0.1";
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
                break;
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
        Utils.debug("received: " + request);
        Socket socket = request.getSocket();
        DataSender dataSender = DataSender.of(socket);
        String address = String.format("%s:%d", request.getIp(), request.getPort());

        synchronized (registeredNodes) {
            if (registeredNodes.containsKey(address)) {
                sendNodeAlreadyRegisteredResponse(dataSender);
                return;
            }

            if (!request.getIp().equals(socket.getInetAddress().getHostAddress())) {
                if (socket.getInetAddress().getHostAddress().equals(LOOPBACK_IP)) {
                    // handle scenario where messaging node exists on the same machine
                    if (!request.getIp().equals(InetAddress.getLocalHost().getHostAddress())) {
                        sendMismatchedIpRegisterResponse(dataSender);
                        return;
                    }
                }
                else {
                    sendMismatchedIpRegisterResponse(dataSender);
                    return;
                }
            }

            registeredNodes.put(address, dataSender);
            sendSuccessRegisterResponse(dataSender);
        }
    }

    private void sendNodeAlreadyRegisteredResponse(DataSender dataSender) {
        sendRegisterResponse(dataSender, Status.FAILURE, "Registration request failed. Node previously registered.");
    }

    private void sendSuccessRegisterResponse(DataSender dataSender) {
        String info = String.format("Registration request successful. The number of messaging nodes currently constituting the overlay is (%s).", registeredNodes.size());
        sendRegisterResponse(dataSender, Status.SUCCESS, info);
    }

    private void sendMismatchedIpRegisterResponse(DataSender dataSender) {
        sendRegisterResponse(dataSender, Status.FAILURE, "Registration request failed. Mismatch in IP.");
    }

    private void sendRegisterResponse(DataSender dataSender, int status, String info) {
        RegisterResponse response = RegisterResponse.of(status, info);
        try {
            dataSender.send(response.getBytes());
            Utils.debug("sent: " + response);
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
