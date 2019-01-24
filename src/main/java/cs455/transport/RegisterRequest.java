package cs455.transport;

import cs455.wireformats.Protocol;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterRequest implements Message {
    private String ip;
    private int port;

    private RegisterRequest(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static RegisterRequest of(String ip, int port) {
        return new RegisterRequest(ip, port);
    }

    @Override
    public int getProtocol() {
        return Protocol.REGISTER_REQUEST;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Message Type (int): REGISTER_REQUEST
         * IP address (String)
         * Port number (int)
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
}
