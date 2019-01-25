package cs455.node;

import cs455.util.Utils;

import java.util.*;

public class Overlay {
    private int cr = 4; // default of 4
    private List<String> nodes = new ArrayList<>();
    private Map<String, List<String>> connections = new HashMap<>();
    private static final int NUM_RETRIES = 5;

    private Overlay() {
    }

    public static Overlay of() {
        return new Overlay();
    }

    public boolean setup(String[] nodes, int cr) {
        return setup(nodes, cr, 0);
    }

    private boolean setup(String[] nodes, int cr, int numRetries) {
        if (numRetries >= NUM_RETRIES)
            return false;

        numRetries++;

        this.connections = new HashMap<>();
        this.nodes = Arrays.asList(nodes);
        this.cr = cr;

        for (String node : nodes)
            connectNode(node);

        if (!checkNumberOfLinks())
            return setup(nodes, cr, numRetries);
        if (!checkForPartition())
            return setup(nodes, cr, numRetries);
        if (!checkNodeConnectsToItself())
            return setup(nodes, cr, numRetries);

        return true;
    }

    private boolean checkNodeConnectsToItself() {
        long numSelfConnections = connections.entrySet().stream()
            .filter(e -> e.getValue().contains(e.getKey()))
            .count();
        if (numSelfConnections > 0) {
            Utils.error("overlay is not valid. 1 or more nodes connect directly to themselves.");
            return false;
        }
        return true;
    }

    private void connectNode(String node) {
        int numConnections = 0;
        for (int i = 1; i <= cr; i++) {
            if (numConnections >= cr)
                return;

            // get random connection, if same node, try again
            int nodeIndex = nodes.indexOf(node);
            while (nodeIndex == nodes.indexOf(node))
                nodeIndex = getRandomNodeIndex();

            // check connection doesn't exist other way round
            String otherNode = nodes.get(nodeIndex);
            if (connections.containsKey(otherNode)) {
                if (connections.get(otherNode).contains(node)) {
                    numConnections++;
                    connections.computeIfAbsent(node, l -> new ArrayList<>()).add(otherNode);
                    continue;
                }
            }

            connections.computeIfAbsent(node, l -> new ArrayList<>()).add(otherNode);
            numConnections++;
        }
    }

    private boolean checkNumberOfLinks() {
        long numBadConnections = connections.entrySet().stream()
            .filter(e -> e.getValue().size() != cr)
            .count();
        if (numBadConnections > 0) {
            Utils.error(String.format("overlay is not valid. 1 or more nodes do not have %d connections.", cr));
            return false;
        }
        return true;
    }

    private boolean checkForPartition() {
        for (String source : nodes) {
            for (String sink : nodes) {
                if (source.equals(sink))
                    continue;

                if (!isSinkReachable(source, sink)) {
                    Utils.error(String.format("source %s cannot reach sink %s", source, sink));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSinkReachable(String source, String sink) {
        Dfs dfs = new Dfs();
        return dfs.isSinkReachable(source, sink);
    }

    private int getRandomNodeIndex() {
        return new Random().nextInt(nodes.size());
    }

    private final class Dfs {
        private Queue<String> queue = new ArrayDeque<>();
        private List<String> visited = new ArrayList<>();

        boolean isSinkReachable(String source, String sink) {
            queue.add(source);

            while (queue.size() > 0) {
                source = queue.poll();
                for (String node : connections.get(source)) {
                    if (node.equals(sink))
                        return true;

                    if (!visited.contains(node)) {
                        visited.add(node);
                        queue.add(node);
                    }
                }
            }
            return false;
        }
    }
}
