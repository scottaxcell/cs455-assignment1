package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MessagingNodesList implements Event {
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
         * Event Type: MESSAGING_NODES_LIST
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
        for (String node : nodes) {
            dataOutputStream.writeInt(node.length());
            dataOutputStream.write(node.getBytes());
        }
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    public String[] getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "MessagingNodesList{" +
            "nodes=" + Arrays.toString(nodes) +
            '}';
    }
}
