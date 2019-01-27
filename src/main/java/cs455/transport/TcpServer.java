package cs455.transport;

import cs455.node.Node;
import cs455.util.Utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TcpServer implements Runnable {
    private String ip;
    private int port;
    private List<TcpReceiver> tcpReceivers = new ArrayList<>();
    private Node node;

    private TcpServer(int port, Node node) {
        this.port = port;
        this.node = node;
    }

    public static TcpServer of(int port, Node node) {
        return new TcpServer(port, node);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            ip = Inet4Address.getLocalHost().getHostAddress();
            port = serverSocket.getLocalPort();
            Utils.debug("TcpServer started on " + getIp() + ":" + getPort());
            Utils.debug("TcpServer started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
            Utils.debug("TcpServer started on " + serverSocket.getLocalSocketAddress());

            while (!Thread.currentThread().interrupted()) {
                Socket incomingSocket = serverSocket.accept();
                TcpReceiver tcpReceiver = TcpReceiver.of(incomingSocket, node);
                Thread thread = new Thread(tcpReceiver);
                thread.start();
                addReceiverThread(tcpReceiver);
                // TODO should I use a thread pool here to limit how many threads are created?
            }

            tcpReceivers.stream()
                .forEach(this::closeReceiverThreadSocket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeReceiverThreadSocket(TcpReceiver tcpReceiver) {
        try {
            tcpReceiver.getSocket().close();
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

    public void addReceiverThread(TcpReceiver tcpReceiver) {
        tcpReceivers.add(tcpReceiver);
    }
}
