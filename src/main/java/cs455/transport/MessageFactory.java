package cs455.transport;

import cs455.wireformats.Protocol;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageFactory {
    private static final int SIZE_OF_INT = 4;
    private static final int SIZE_OF_BYTE = 1;

    public static Message getMessageFromData(byte[] data, Socket socket) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        int protocol = dataInputStream.readInt();
        switch (protocol) {
            case Protocol.REGISTER_REQUEST:
                return createRegisterRequest(data.length, dataInputStream, socket);
            case Protocol.REGISTER_RESPONSE:
                return createRegisterResponse(data.length, dataInputStream);
            case Protocol.DEREGISTER_REQUEST:
                return createDeregisterRequest(data.length, dataInputStream, socket);
            case Protocol.DEREGISTER_RESPONSE:
                return createDeregisterResponse(data.length, dataInputStream);
            case Protocol.MESSAGING_NODES_LIST:
                return createMessagingNodesList(data.length, dataInputStream);
            case Protocol.HANDSHAKE:
                return createHandshake(data.length, dataInputStream, socket);
            default:
                throw new RuntimeException("received an unknown message");
        }
    }

    private static Handshake createHandshake(int dataLength, DataInputStream dataInputStream, Socket socket) throws IOException {
        /**
         * Message Type (int): HANDSHAKE
         * IP address (String)
         * Port number (int)
         */
        int ipLength = dataLength - SIZE_OF_INT * 2;
        byte[] ipBytes = new byte[ipLength];
        dataInputStream.readFully(ipBytes, 0, ipLength);
        String ip = new String(ipBytes);
        int port = dataInputStream.readInt();
        return Handshake.of(ip, port, socket);
    }

    private static Message createMessagingNodesList(int dataLength, DataInputStream dataInputStream) throws IOException {
        /**
         * Message Type: MESSAGING_NODES_LIST
         * Number of peer messaging nodes: X
         * Messaging node1 Info
         * Messaging node2 Info
         * .....
         * Messaging nodeX Info
         *
         * where nodeX Info: node_hostname:portnum
         */
        int numNodes = dataInputStream.readInt();
        int nodesLength = dataLength - SIZE_OF_INT * 2;
        byte[] nodesBytes = new byte[nodesLength];
        dataInputStream.readFully(nodesBytes, 0, nodesLength);
        String nodes = new String(nodesBytes);
        return MessagingNodesList.of(nodes.split("\\n"));
    }

    private static Message createDeregisterResponse(int dataLength, DataInputStream dataInputStream) throws IOException {
        /**
         * Message Type (int): DEREGISTER_RESPONSE
         * Status Code (byte): SUCCESS or FAILURE
         * Additional Info (String):
         */
        int status = dataInputStream.read();
        int infoLength = dataLength - SIZE_OF_INT - SIZE_OF_BYTE;
        byte[] infoBytes = new byte[infoLength];
        dataInputStream.readFully(infoBytes, 0, infoLength);
        String info = new String(infoBytes);
        return DeregisterResponse.of(status, info);
    }

    private static DeregisterRequest createDeregisterRequest(int dataLength, DataInputStream dataInputStream, Socket socket) throws IOException {
        /**
         * Message Type (int): DEREGISTER_REQUEST
         * Node IP address (String)
         * Node Port number (int)
         */
        int ipLength = dataLength - SIZE_OF_INT * 2;
        byte[] ipBytes = new byte[ipLength];
        dataInputStream.readFully(ipBytes, 0, ipLength);
        String ip = new String(ipBytes);
        int port = dataInputStream.readInt();
        return DeregisterRequest.of(ip, port, socket);
    }

    private static Message createRegisterResponse(int dataLength, DataInputStream dataInputStream) throws IOException {
        /**
         * Message Type (int): REGISTER_RESPONSE
         * Status Code (byte): SUCCESS or FAILURE
         * Additional Info (String):
         */
        int status = dataInputStream.read();
        int infoLength = dataLength - SIZE_OF_INT - SIZE_OF_BYTE;
        byte[] infoBytes = new byte[infoLength];
        dataInputStream.readFully(infoBytes, 0, infoLength);
        String info = new String(infoBytes);
        return RegisterResponse.of(status, info);
    }

    private static RegisterRequest createRegisterRequest(int dataLength, DataInputStream dataInputStream, Socket socket) throws IOException {
        /**
         * Message Type (int): REGISTER_REQUEST
         * IP address (String)
         * Port number (int)
         */
        int ipLength = dataLength - SIZE_OF_INT * 2;
        byte[] ipBytes = new byte[ipLength];
        dataInputStream.readFully(ipBytes, 0, ipLength);
        String ip = new String(ipBytes);
        int port = dataInputStream.readInt();
        return RegisterRequest.of(ip, port, socket);
    }
}
