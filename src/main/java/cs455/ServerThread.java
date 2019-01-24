package cs455;

import cs455.node.Node;
import cs455.util.Utils;

import java.io.IOException;
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
            Utils.debug("ServerThread started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
            ip = serverSocket.getInetAddress().getHostAddress();
            port = serverSocket.getLocalPort();

            while (!interrupted()) {
                Socket incomingSocket = serverSocket.accept();
                ReceiverThread receiverThread = ReceiverThread.of(incomingSocket, node);
                receiverThreads.add(receiverThread);
                receiverThread.start();
                // TODO should I use a thread pool here to limit how many threads are created?
            }

            receiverThreads.stream()
                .forEach(this::closeReceiverThreadSocket);

            Utils.debug("ServerThread interrupted on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeReceiverThreadSocket(ReceiverThread receiverThread) {
        try {
            receiverThread.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
