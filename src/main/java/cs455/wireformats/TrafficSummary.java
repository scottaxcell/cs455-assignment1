package cs455.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Event {
    private String ip;
    private int port;
    private int numMessagesSent;
    private long sentSummation;
    private int numMessagesReceived;
    private long receivedSummation;
    private int numMessagesRelayed;

    private TrafficSummary(String ip, int port, int numMessagesSent, long sentSummation, int numMessagesReceived, long receivedSummation, int numMessagesRelayed) {
        this.ip = ip;
        this.port = port;
        this.numMessagesSent = numMessagesSent;
        this.sentSummation = sentSummation;
        this.numMessagesReceived = numMessagesReceived;
        this.receivedSummation = receivedSummation;
        this.numMessagesRelayed = numMessagesRelayed;
    }

    public static TrafficSummary of(String ip, int port, int numMessagesSent, long sentSummation, int numMessagesReceived, long receivedSummation, int numMessagesRelayed) {
        return new TrafficSummary(ip, port, numMessagesSent, sentSummation, numMessagesReceived, receivedSummation, numMessagesRelayed);
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

        dataOutputStream.writeInt(numMessagesSent);
        dataOutputStream.writeLong(sentSummation);
        dataOutputStream.writeInt(numMessagesReceived);
        dataOutputStream.writeLong(receivedSummation);
        dataOutputStream.writeInt(numMessagesRelayed);

        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }
}
