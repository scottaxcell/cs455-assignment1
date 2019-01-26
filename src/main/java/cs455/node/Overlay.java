package cs455.node;

import cs455.util.Utils;

import java.util.*;

public class Overlay {
    private int cr = 4; // default of 4
    private List<String> nodes = new ArrayList<>();
    private Map<String, List<String>> connections = new HashMap<>();
    private List<Link> links = new ArrayList<>();

    private Overlay(Set<String> nodes, int cr) {
        this.nodes.addAll(nodes);
        this.cr = cr;
    }

    public static Overlay of(Set<String> nodes, int cr) {
        return new Overlay(nodes, cr);
    }

    public boolean setup() {
        if (!isConnectionRequirementValid(nodes.size(), cr))
            return false;

        constructLinearTopology();

        for (String node : nodes)
            connectNode(node);

        if (!checkNumberOfLinks())
            return false;
        if (!checkForPartition())
            return false;
        if (!checkNodeConnectsToItself())
            return false;

        return true;
    }

    private void constructLinearTopology() {
        for (int i = 0; i < nodes.size(); i++) {
            String source = nodes.get(i);
            if (i == nodes.size() - 1) {
                // last node connects to first node
                String sink = nodes.get(0);
                connections.computeIfAbsent(source, l -> new ArrayList<>()).add(sink);
                links.add(Link.of(source, sink));
            }
            else {
                // connect to next node
                String sink = nodes.get(i + 1);
                connections.computeIfAbsent(source, l -> new ArrayList<>()).add(sink);
                links.add(Link.of(source, sink));
            }
        }
    }

    private boolean isConnectionRequirementValid(int n, int k) {
        // k-regular graph conditions
        // n = k+1
        // nk is even
        if ((n < (k + 1)) || (((n * k) % 2) != 0)) {
            Utils.error("overlay is not valid. k-regular graph conditions not met.");
            return false;
        }
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

    private void connectNode(String source) {
        // already created a linear topology so decrement by 1
        int requiredNumConnections = cr - 1;
        int numConnections = 0;
        for (int i = 1; i <= requiredNumConnections; i++) {
            if (numConnections >= requiredNumConnections)
                return;

            // get random connection, if same node, try again
            int nodeIndex = nodes.indexOf(source);
            while (nodeIndex == nodes.indexOf(source))
                nodeIndex = getRandomNodeIndex();

            // check connection doesn't exist other way round
            String sink = nodes.get(nodeIndex);
            if (connections.containsKey(sink)) {
                if (connections.get(sink).contains(source)) {
                    numConnections++;
                    connections.computeIfAbsent(source, l -> new ArrayList<>()).add(sink);
                    continue;
                }
            }

            connections.computeIfAbsent(source, l -> new ArrayList<>()).add(sink);
            links.add(Link.of(source, sink));
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

    public String[] getNodeConnections(String node) {
        return links.stream()
            .filter(l -> l.getSource().equals(node))
            .toArray(size -> new String[size]);
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
