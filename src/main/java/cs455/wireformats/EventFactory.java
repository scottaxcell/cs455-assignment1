package cs455.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class EventFactory {
    private static final int SIZE_OF_INT = 4;
    private static final int SIZE_OF_BYTE = 1;

    public static Event getMessageFromData(byte[] data, Socket socket) throws IOException {
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
                return createMessagingNodesList(dataInputStream);
            case Protocol.HANDSHAKE:
                return createHandshake(data.length, dataInputStream, socket);
            case Protocol.LINK_WEIGHTS:
                return createLinkWeights(dataInputStream);
            case Protocol.TASK_INITIATE:
                return createTaskInitiative(dataInputStream);
            case Protocol.MESSAGE:
                return createMessage(dataInputStream);
            case Protocol.TASK_COMPLETE:
                return createTaskComplete(dataInputStream);
            default:
                throw new RuntimeException("received an unknown message");
        }
    }

    private static TaskComplete createTaskComplete(DataInputStream dataInputStream) throws IOException {
        /**
         * Event Type (int): HANDSHAKE
         * IP address (String)
         * Port number (int)
         */
        int ipLength = dataInputStream.readInt();
        byte[] ipBytes = new byte[ipLength];
        dataInputStream.readFully(ipBytes, 0, ipLength);
        String ip = new String(ipBytes);
        int port = dataInputStream.readInt();
        return TaskComplete.of(ip, port);
    }

    private static Message createMessage(DataInputStream dataInputStream) throws IOException {
        /**
         * Event Type (int): MESSAGE
         * Payload (int)
         * Destination node (String)
         */
        int payload = dataInputStream.readInt();
        int destinationLength = dataInputStream.readInt();
        byte[] destinationBytes = new byte[destinationLength];
        dataInputStream.readFully(destinationBytes, 0, destinationLength);
        String destination = new String(destinationBytes);
        return Message.of(payload, destination);
    }

    private static TaskInitiate createTaskInitiative(DataInputStream dataInputStream) throws IOException {
        /**
         * Event Type: TASK_INITIATE
         * Rounds: X
         */
        int numRounds = dataInputStream.readInt();
        return TaskInitiate.of(numRounds);
    }

    private static Handshake createHandshake(int dataLength, DataInputStream dataInputStream, Socket socket) throws IOException {
        /**
         * Event Type (int): HANDSHAKE
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

    private static Event createLinkWeights(DataInputStream dataInputStream) throws IOException {
        /**
         * Event Type: Link_Weights
         * Number of links: L
         * Linkinfo1
         * Linkinfo2
         * ...
         * LinkinfoL
         *
         * where LinkinfoL: hostnameA:portnumA hostnameB:portnumB weight
         */
        List<String> linkInfos = new ArrayList<>();
        int numLinks = dataInputStream.readInt();
        for (int i = 0; i < numLinks; i++) {
            int linkInfoLength = dataInputStream.readInt();
            byte[] linkInfoBytes = new byte[linkInfoLength];
            dataInputStream.readFully(linkInfoBytes, 0, linkInfoLength);
            String linkInfo = new String(linkInfoBytes);
            linkInfos.add(linkInfo);
        }
        return LinkWeights.of(linkInfos.toArray(new String[linkInfos.size()]));
    }

    private static Event createMessagingNodesList(DataInputStream dataInputStream) throws IOException {
        /**
         * Event Type: MESSAGING_NODES_LIST
         * Number of peer messaging nodes: X
         * Messaging node1 Info
         * Messaging node2 Info
         * .....
         * Messaging nodeX Info
         *
         * where nodeX Info: node_hostname:portnum
         */
        List<String> nodeInfos = new ArrayList<>();
        int numNodes = dataInputStream.readInt();
        for (int i = 0; i < numNodes; i++) {
            int nodeInfoLength = dataInputStream.readInt();
            byte[] nodeInfoBytes = new byte[nodeInfoLength];
            dataInputStream.readFully(nodeInfoBytes, 0, nodeInfoLength);
            String nodeInfo = new String(nodeInfoBytes);
            nodeInfos.add(nodeInfo);
        }
        return MessagingNodesList.of(nodeInfos.toArray(new String[nodeInfos.size()]));
    }

    private static Event createDeregisterResponse(int dataLength, DataInputStream dataInputStream) throws IOException {
        /**
         * Event Type (int): DEREGISTER_RESPONSE
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
         * Event Type (int): DEREGISTER_REQUEST
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

    private static Event createRegisterResponse(int dataLength, DataInputStream dataInputStream) throws IOException {
        /**
         * Event Type (int): REGISTER_RESPONSE
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
         * Event Type (int): REGISTER_REQUEST
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
