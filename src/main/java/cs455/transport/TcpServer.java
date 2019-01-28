package cs455.transport;

import cs455.node.Node;
import cs455.util.Utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpServer implements Runnable {
    private ServerSocket serverSocket;
    private Node node;

    private TcpServer(int port, Node node) {
        this.node = node;
        try {
            serverSocket = new ServerSocket(port);
            Utils.debug("TcpServer started on " + getIp() + ":" + getPort());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TcpServer of(int port, Node node) {
        return new TcpServer(port, node);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                TcpConnection tcpConnection = TcpConnection.of(socket, node);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIp() throws UnknownHostException {
        return serverSocket.getInetAddress().getLocalHost().getHostName();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
