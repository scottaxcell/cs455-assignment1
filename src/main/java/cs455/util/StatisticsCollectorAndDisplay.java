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
    private static final String[] columnHeaders = new String[]{"Node", "# Sent", "# Received", "Sent Sum", "Received Sum", "# Relayed"};

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

        int[] justify = getColumnJustifications();

        sb.append(String.format("%-"+justify[0]+"s  ", "Node"));
        sb.append(String.format("%"+justify[1]+"s  ", "# Sent"));
        sb.append(String.format("%"+justify[2]+"s  ", "# Received"));
        sb.append(String.format("%"+justify[3]+"s  ", "Sent Sum"));
        sb.append(String.format("%"+justify[4]+"s  ", "Received Sum"));
        sb.append(String.format("%"+justify[5]+"s\n", "# Relayed"));
        Utils.out(sb.toString());

        sb = new StringBuilder();
        for (TrafficSummary trafficSummary : trafficSummaries) {
            sb.append(String.format("%-"+justify[0]+"s  ", trafficSummary.getNode()));
            sb.append(String.format("%"+justify[1]+"s  ", trafficSummary.getNumSent()));
            sb.append(String.format("%"+justify[2]+"s  ", trafficSummary.getNumReceived()));
            sb.append(String.format("%"+justify[3]+"s  ", trafficSummary.getSentSum()));
            sb.append(String.format("%"+justify[4]+"s  ", trafficSummary.getReceivedSum()));
            sb.append(String.format("%"+justify[5]+"s\n", trafficSummary.getNumRelayed()));
            Utils.out(sb.toString());
        }

        sb = new StringBuilder();
        sb.append(String.format("%-"+justify[0]+"s  ", "Sum"));
        sb.append(String.format("%"+justify[1]+"s  ", numSent));
        sb.append(String.format("%"+justify[2]+"s  ", numReceived));
        sb.append(String.format("%"+justify[3]+"s  ", sentSum));
        sb.append(String.format("%"+justify[4]+"s\n", receiveSum));
        Utils.out(sb.toString());
    }

    private int[] getColumnJustifications() {
        int[] justify = new int[6];

        if (columnHeaders[0].length() > justify[0])
            justify[0] = columnHeaders[0].length();

        if (columnHeaders[1].length() > justify[1])
            justify[1] = columnHeaders[1].length();

        if (columnHeaders[2].length() > justify[2])
            justify[2] = columnHeaders[2].length();

        if (columnHeaders[3].length() > justify[3])
            justify[3] = columnHeaders[3].length();

        if (columnHeaders[4].length() > justify[4])
            justify[4] = columnHeaders[4].length();

        if (columnHeaders[5].length() > justify[5])
            justify[5] = columnHeaders[5].length();

        for (TrafficSummary ts : trafficSummaries) {
            if (ts.getNode().length() > justify[0])
                justify[0] = ts.getNode().length();

            if (String.valueOf(ts.getNumSent()).length() > justify[1])
                justify[1] = String.valueOf(ts.getNumSent()).length();

            if (String.valueOf(ts.getNumReceived()).length() > justify[2])
                justify[2] = String.valueOf(ts.getNumReceived()).length();

            if (String.valueOf(ts.getSentSum()).length() > justify[3])
                justify[3] = String.valueOf(ts.getSentSum()).length();

            if (String.valueOf(ts.getReceivedSum()).length() > justify[4])
                justify[4] = String.valueOf(ts.getReceivedSum()).length();

            if (String.valueOf(ts.getNumRelayed()).length() > justify[5])
                justify[5] = String.valueOf(ts.getNumRelayed()).length();
        }

        if (String.valueOf(numSent).length() > justify[1])
            justify[1] = String.valueOf(numSent).length();

        if (String.valueOf(numReceived).length() > justify[2])
            justify[2] = String.valueOf(numReceived).length();

        if (String.valueOf(sentSum).length() > justify[3])
            justify[3] = String.valueOf(sentSum).length();

        if (String.valueOf(receiveSum).length() > justify[4])
            justify[4] = String.valueOf(receiveSum).length();

        return justify;
    }
}
