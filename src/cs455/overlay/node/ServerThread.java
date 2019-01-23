package cs455.overlay.node;

import cs455.overlay.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {
    private String ip;
    private int port;
    private List<ReceiverThread> receiverThreads = new ArrayList<>();

    private ServerThread(int port) {
        this.port = port;
        setName("Server");
    }

    public static ServerThread of(int port) {
        return new ServerThread(port);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Utils.debug("ServerThread started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
            ip = serverSocket.getInetAddress().getHostAddress();

            while (!interrupted()) {
                Socket incomingSocket = serverSocket.accept();
                ReceiverThread receiverThread = ReceiverThread.of(incomingSocket);
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
}
