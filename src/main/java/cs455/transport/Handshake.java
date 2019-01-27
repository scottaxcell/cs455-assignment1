package cs455.transport;

import cs455.wireformats.Protocol;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Handshake implements Message {
    private String ip;
    private int port;
    private Socket socket;

    private Handshake(String ip, int port, Socket socket) {
        this.ip = ip;
        this.port = port;
        this.socket = socket;
    }

    public static Handshake of(String ip, int port, Socket socket) {
        return new Handshake(ip, port, socket);
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
        return Protocol.HANDSHAKE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Message Type (int): HANDSHAKE
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
        return "Handshake{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            ", socket=" + socket +
            '}';
    }
}
