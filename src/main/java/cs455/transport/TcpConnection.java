package cs455.transport;

import cs455.node.Node;

import java.io.IOException;
import java.net.Socket;

public class TcpConnection {
    private final Socket socket;
    private final Node node;
    private TcpReceiver tcpReceiver;
    private TcpSender tcpSender;

    private TcpConnection(Socket socket, Node node) {
        this.socket = socket;
        this.node = node;
        try {
            tcpReceiver = TcpReceiver.of(socket, node);
            Thread thread = new Thread(tcpReceiver);
            thread.start();

            tcpSender = TcpSender.of(socket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TcpConnection of(Socket socket, Node node) {
        return new TcpConnection(socket, node);
    }

    public Socket getSocket() {
        return socket;
    }

    public void send(byte[] data) {
        try {
            tcpSender.send(data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return socket.getLocalPort();
    }
}
