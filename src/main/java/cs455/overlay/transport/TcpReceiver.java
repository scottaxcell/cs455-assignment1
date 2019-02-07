package cs455.overlay.transport;

import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TcpReceiver implements Runnable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private Node node;

    private TcpReceiver(Socket socket, Node node) throws IOException {
        this.socket = socket;
        this.node = node;
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public static TcpReceiver of(Socket incomingSocket, Node node) throws IOException {
        return new TcpReceiver(incomingSocket, node);
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        while (socket != null) {
            try {
                int dataLength = dataInputStream.readInt();
                byte[] data = new byte[dataLength];
                dataInputStream.readFully(data, 0, dataLength);
                Event event = EventFactory.getMessageFromData(data, socket);
                node.onEvent(event);
            }
            catch (SocketException e){
                e.printStackTrace();
                break;
            }
            catch (IOException ignore) {
                // connection drops
                break;
            }
        }
    }
}
