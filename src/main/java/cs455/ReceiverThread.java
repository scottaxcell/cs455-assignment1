package cs455;

import cs455.node.Node;
import cs455.transport.Message;
import cs455.transport.MessageFactory;
import cs455.util.Utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

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
            byte[] data = null;
            try {
                int dataLength = dataInputStream.readInt();
                data = new byte[dataLength];
                dataInputStream.readFully(data, 0, dataLength);
            } catch (EOFException e) {
                Utils.debug(e.getMessage());
                break;
            } catch (SocketException e) {
                Utils.debug(e.getMessage());
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (data != null) {
                try {
                    Message message = MessageFactory.getMessageFromData(data);
                    node.onMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
