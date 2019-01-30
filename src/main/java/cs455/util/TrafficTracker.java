package cs455.util;

public class TrafficTracker {
    private int sendTracker;
    private int receiveTracker;
    private int relayTracker;
    private long sendSummation;
    private long receiveSummation;

    public static TrafficTracker of() {
        return new TrafficTracker();
    }

    public synchronized void incrementSendTracker() {
        sendTracker++;
    }

    public synchronized void incrementReceiveTracker() {
        receiveTracker++;
    }

    public synchronized void incrementRelayTracker() {
        relayTracker++;
    }

    public synchronized void addSendSummation(int number) {
        sendSummation += number;
    }

    public synchronized void addReceiveSummation(int number) {
        receiveSummation += number;
    }

    public int getSendTracker() {
        return sendTracker;
    }

    public int getReceiveTracker() {
        return receiveTracker;
    }

    public int getRelayTracker() {
        return relayTracker;
    }

    public long getSendSummation() {
        return sendSummation;
    }

    public long getReceiveSummation() {
        return receiveSummation;
    }

    public void reset() {
        sendTracker = 0;
        receiveTracker = 0;
        relayTracker = 0;
        sendSummation = 0;
        receiveSummation = 0;
    }
}
