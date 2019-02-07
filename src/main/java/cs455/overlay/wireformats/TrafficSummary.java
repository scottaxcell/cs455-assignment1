package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Event {
    private String ip;
    private int port;
    private int numSent;
    private long sentSum;
    private int numReceived;
    private long receivedSum;
    private int numRelayed;

    private TrafficSummary(String ip, int port, int numSent, long sentSum, int numReceived, long receivedSum, int numRelayed) {
        this.ip = ip;
        this.port = port;
        this.numSent = numSent;
        this.sentSum = sentSum;
        this.numReceived = numReceived;
        this.receivedSum = receivedSum;
        this.numRelayed = numRelayed;
    }

    public static TrafficSummary of(String ip, int port, int numMessagesSent, long sentSummation, int numMessagesReceived, long receivedSummation, int numMessagesRelayed) {
        return new TrafficSummary(ip, port, numMessagesSent, sentSummation, numMessagesReceived, receivedSummation, numMessagesRelayed);
    }

    public int getNumSent() {
        return numSent;
    }

    public long getSentSum() {
        return sentSum;
    }

    public int getNumReceived() {
        return numReceived;
    }

    public long getReceivedSum() {
        return receivedSum;
    }

    public int getNumRelayed() {
        return numRelayed;
    }

    public String getNode() {
        return String.format("%s:%d", ip, port);
    }

    @Override
    public int getProtocol() {
        return Protocol.TRAFFIC_SUMMARY;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Message Type: TRAFFIC_SUMMARY
         * Node IP address:
         * Node Port number:
         * Number of messages sent
         * Summation of sent messages
         * Number of messages received
         * Summation of received messages
         * Number of messages relayed
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.writeInt(ip.length());
        dataOutputStream.write(ip.getBytes());
        dataOutputStream.writeInt(port);

        dataOutputStream.writeInt(numSent);
        dataOutputStream.writeLong(sentSum);
        dataOutputStream.writeInt(numReceived);
        dataOutputStream.writeLong(receivedSum);
        dataOutputStream.writeInt(numRelayed);

        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    @Override
    public String toString() {
        return "TrafficSummary{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            ", numSent=" + numSent +
            ", sentSum=" + sentSum +
            ", numReceived=" + numReceived +
            ", receivedSum=" + receivedSum +
            ", numRelayed=" + numRelayed +
            '}';
    }
}
