package cs455;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataSender {
    private Socket socket;
    private DataOutputStream dataOutputStream;

    private DataSender(Socket socket) throws IOException {
        this.socket = socket;
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public static DataSender of(Socket socket) throws IOException {
        return new DataSender(socket);
    }

    public synchronized void send(byte[] data) throws IOException {
        int dataLength = data.length;
        dataOutputStream.writeInt(dataLength);
        dataOutputStream.write(data, 0, dataLength);
        dataOutputStream.flush();
    }

    public Socket getSocket() {
        return socket;
    }
}
