package cs455.overlay.node;

import cs455.overlay.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ReceiverThread extends Thread {
    private Socket socket;
    private DataInputStream dataInputStream;

    private ReceiverThread(Socket socket) throws IOException {
        this.socket = socket;
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public static ReceiverThread of(Socket incomingSocket) throws IOException {
        return new ReceiverThread(incomingSocket);
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
            } catch (SocketException e) {
                Utils.debug(e.getMessage());
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (data != null) {
                Utils.debug("ReceiverThread received: " + data.toString());
                ByteArrayInputStream baistream = new ByteArrayInputStream(data);
                DataInputStream din = new DataInputStream(new BufferedInputStream(baistream));
                byte[] recevied = new byte[1024];
                try {
                    din.readFully(recevied);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Utils.debug("translated: " + new String(recevied));

            }
        }
    }
}
