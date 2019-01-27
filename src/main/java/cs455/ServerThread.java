package cs455;

import cs455.node.Node;
import cs455.util.Utils;

import javax.sound.midi.Receiver;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {
    private String ip;
    private int port;
    private List<ReceiverThread> receiverThreads = new ArrayList<>();
    private Node node;

    private ServerThread(int port, Node node) {
        this.port = port;
        this.node = node;
        setName("Server");
    }

    public static ServerThread of(int port, Node node) {
        return new ServerThread(port, node);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            ip = Inet4Address.getLocalHost().getHostAddress();
            port = serverSocket.getLocalPort();
            Utils.debug("ServerThread started on " + getIp() + ":" + getPort());
            Utils.debug("ServerThread started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
            Utils.debug("ServerThread started on " + serverSocket.getLocalSocketAddress());

            while (!interrupted()) {
                Socket incomingSocket = serverSocket.accept();
                ReceiverThread receiverThread = ReceiverThread.of(incomingSocket, node);
                addReceiverThread(receiverThread);
                receiverThread.start();
                // TODO should I use a thread pool here to limit how many threads are created?
            }

            receiverThreads.stream()
                .forEach(this::closeReceiverThreadSocket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeReceiverThreadSocket(ReceiverThread receiverThread) {
        try {
            receiverThread.getSocket().close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void addReceiverThread(ReceiverThread receiverThread) {
        receiverThreads.add(receiverThread);
    }
}
