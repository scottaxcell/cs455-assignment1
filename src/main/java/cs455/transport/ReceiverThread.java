package cs455.transport;

import cs455.node.Node;
import cs455.wireformats.Message;
import cs455.wireformats.MessageFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ReceiverThread extends Thread {
    private Socket socket;
    private DataInputStream dataInputStream;
    private Node node;

    private ReceiverThread(Socket socket, Node node) throws IOException {
        this.socket = socket;
        this.node = node;
        dataInputStream = new DataInputStream(socket.getInputStream());
        setName("Receiver");
    }

    public static ReceiverThread of(Socket incomingSocket, Node node) throws IOException {
        return new ReceiverThread(incomingSocket, node);
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
                Message message = MessageFactory.getMessageFromData(data, socket);
                node.onMessage(message);
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
