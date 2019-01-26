package cs455.transport;

import cs455.wireformats.Protocol;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MessagingNodesList implements Message {
    private String[] nodes;

    private MessagingNodesList(String[] nodes) {
        this.nodes = nodes;
    }

    public static MessagingNodesList of(String[] nodes) {
        return new MessagingNodesList(nodes);
    }

    @Override
    public int getProtocol() {
        return Protocol.MESSAGING_NODES_LIST;
    }

    @Override
    public byte[] getBytes() throws IOException {
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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.writeInt(nodes.length);
        if (nodes.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String node : nodes) {
                stringBuilder.append(node);
                stringBuilder.append("\n");
            }
            dataOutputStream.write(stringBuilder.toString().getBytes());
        }
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    @Override
    public String toString() {
        return "MessagingNodesList{" +
            "nodes=" + Arrays.toString(nodes) +
            '}';
    }
}
