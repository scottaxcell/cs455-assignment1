package cs455.util;

import cs455.wireformats.TrafficSummary;

import java.util.ArrayList;
import java.util.List;

public class StatisticsCollectorAndDisplay {
    private int numNodes;
    private List<TrafficSummary> trafficSummaries = new ArrayList<>();
    private int numSent;
    private int numReceived;
    private long sentSum;
    private long receiveSum;

    private StatisticsCollectorAndDisplay(int numNodes) {
        this.numNodes = numNodes;
    }

    public static StatisticsCollectorAndDisplay of(int numNodes) {
        return new StatisticsCollectorAndDisplay(numNodes);
    }

    public synchronized void addTrafficSummary(TrafficSummary trafficSummary) {
        trafficSummaries.add(trafficSummary);
        numSent += trafficSummary.getNumSent();
        numReceived += trafficSummary.getNumReceived();
        sentSum += trafficSummary.getSentSum();
        receiveSum += trafficSummary.getReceivedSum();

        if (trafficSummaries.size() == numNodes)
            printSummary();
    }

    private void printSummary() {
        StringBuilder sb = new StringBuilder("TRAFFIC SUMMARY\n===============\n");
        sb.append("Node\t# Sent\t# Received\tSent Sum\tReceived Sum\t# Relayed\n");

        for (TrafficSummary trafficSummary : trafficSummaries) {
            sb.append(trafficSummary.getNode() + "\t");
            sb.append(trafficSummary.getNumSent() + "\t");
            sb.append(trafficSummary.getNumReceived() + "\t");
            sb.append(trafficSummary.getSentSum() + "\t");
            sb.append(trafficSummary.getReceivedSum() + "\t");
            sb.append(trafficSummary.getNumRelayed() + "\n");
            Utils.out(sb.toString());
        }

        sb = new StringBuilder("Sum\t");
        sb.append(numSent + "\t");
        sb.append(numReceived + "\t");
        sb.append(sentSum + "\t");
        sb.append(receiveSum + "\n");
        Utils.out(sb.toString());
    }
}
