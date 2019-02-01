package cs455.transport;

import cs455.node.Node;
import cs455.wireformats.Event;
import cs455.wireformats.EventFactory;

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
            catch (SocketException se){
                se.printStackTrace();
                break;
            }
            catch (IOException ioe) {
                // TODO socket disconnects
                ioe.printStackTrace();
                break;
            }
        }
    }
}
