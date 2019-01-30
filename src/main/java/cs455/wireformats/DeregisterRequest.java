package cs455.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DeregisterRequest implements Event {
    private String ip;
    private int port;
    private Socket socket;

    private DeregisterRequest(String ip, int port, Socket socket) {
        this.ip = ip;
        this.port = port;
        this.socket = socket;
    }

    public static DeregisterRequest of(String ip, int port, Socket socket) {
        return new DeregisterRequest(ip, port, socket);
    }

    public Socket getSocket() {
        return socket;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int getProtocol() {
        return Protocol.DEREGISTER_REQUEST;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Event Type (int): DEREGISTER_REQUEST
         * Node IP address (String)
         * Node Port number (int)
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.write(ip.getBytes());
        dataOutputStream.writeInt(port);
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    @Override
    public String toString() {
        return "DeregisterRequest{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            ", socket=" + socket +
            '}';
    }
}
