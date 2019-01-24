package cs455.transport;

import cs455.wireformats.Protocol;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MessageFactory {
    private static final int SIZE_OF_INT = 4;

    public static Message getMessageFromData(byte[] data) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        int protocol = dataInputStream.readInt();
        switch (protocol) {
            case Protocol.REGISTER_REQUEST:
                return createRegisterRequest(data.length, dataInputStream);
            default:
                throw new RuntimeException("received an unknown message");
        }
    }

    private static RegisterRequest createRegisterRequest(int dataLength, DataInputStream dataInputStream) throws IOException {
        /**
         * Message Type (int): REGISTER_REQUEST
         * IP address (String)
         * Port number (int)
         */
        int ipStringLength = dataLength - SIZE_OF_INT * 2;
        byte[] ipBytes = new byte[ipStringLength];
        dataInputStream.readFully(ipBytes, 0, ipStringLength);
        String ip = new String(ipBytes);
        int port = dataInputStream.readInt();
        return RegisterRequest.of(ip, port);
    }
}
