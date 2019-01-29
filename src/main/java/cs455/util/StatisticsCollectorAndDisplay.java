package cs455.util;

public class StatisticsCollectorAndDisplay {
    private int sendTracker;
    private int receiveTracker;
    private int relayTracker;
    private long sendSummation;
    private long receiveSummation;

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
}
