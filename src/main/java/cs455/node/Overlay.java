package cs455.node;

import cs455.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class Overlay {
    private int cr;
    private List<String> nodes = new ArrayList<>();
    private Map<String, List<String>> connections = new HashMap<>();
    private List<Link> links = new ArrayList<>();

    private Overlay(Set<String> nodes, int cr) {
        this.nodes.addAll(nodes);
        nodes.stream()
            .forEach(n -> connections.computeIfAbsent(n, l -> new ArrayList<>()));
        this.cr = cr;
    }

    public static Overlay of(Set<String> nodes, int cr) {
        return new Overlay(nodes, cr);
    }

    public boolean setup() {
        if (!isConnectionRequirementValid(nodes.size(), cr))
            return false;

        createKRegularGraph();

        if (!checkNumberOfLinks())
            return false;
        if (!checkForPartition())
            return false;
        if (!checkNodeConnectsToItself())
            return false;

        return true;
    }

    private void dumpOverlay() {
        Utils.out("NODES:\n");
        for (String node : nodes) {
            Utils.out(node + "\n");
        }
        Utils.out("\nCONNECTIONS:\n");
        for (Map.Entry<String, List<String>> entry : connections.entrySet()) {
            Utils.out(entry.getKey()+"\n");
            List<String> sinks = entry.getValue();
            Collections.sort(sinks);
            for (String sink : sinks) {
                Utils.out("  " + sink +"\n");
            }
        }
        Utils.out("\nLINKS:\n");
        List<String> printMe = new ArrayList<>();
        for (Link link : links) {
            printMe.add(link.getSource() + " -> " + link.getSink() + "\n");
        }
        Collections.sort(printMe);
        for (String s : printMe) {
            Utils.out(s);
        }
    }

    private void linkNodeXStepsAway(int index, int steps) {
        int neighborIndex = getNeighborIndexXStepsAway(index, steps);
        String source = nodes.get(index);
        String sink = nodes.get(neighborIndex);
        addLink(Link.of(source, sink));
    }

    private int getNeighborIndexXStepsAway(int index, int steps) {
        int neighborIndex = index;
        for (int step = 0; step < steps; step++) {
            neighborIndex++;
            if (neighborIndex == nodes.size())
                neighborIndex = 0;
        }
        return neighborIndex;
    }

    private void createKRegularGraph() {
        Collections.shuffle(nodes);

        if (cr == 1) {
            constructLinearTopology();
            for (Link link : links)
                addSink(link.getSource(), link.getSink());
            return;
        }

        if (isEven(cr)) {
            // If k=2m is even, put all the vertices around a circle, and join
            // each to its m nearest neighbors on either side
            int m = cr / 2;
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = 1; j <= m; j++)
                    linkNodeXStepsAway(i, j);
            }
        }
        else {
            // If k=2m+1 is odd, and n is even, put the vertices on a circle,
            // join each to its m nearest neighbors on each side, and also to the vertex directly opposite
            int m = (cr - 1) / 2;
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = 1; j <= m; j++)
                    linkNodeXStepsAway(i, j);
                linkNodeXStepsAway(i, nodes.size() / 2);
            }
        }

        for (Link link : links) {
            addSink(link.getSource(), link.getSink());
            addSink(link.getSink(), link.getSource());
        }
    }

    private boolean isEven(int i) {
        return i % 2 == 0;
    }

     private void constructLinearTopology() {
        for (int i = 0; i < nodes.size(); i++) {
            String source = nodes.get(i);
            if (i == nodes.size() - 1) {
                String sink = nodes.get(0);
                addLink(Link.of(source, sink));
            }
            else {
                String sink = nodes.get(i + 1);
                addLink(Link.of(source, sink));
            }
        }
    }

    private boolean isConnectionRequirementValid(int n, int k) {
        // k-regular graph conditions
        // n >= k+1
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

        private void addLink(Link link) {
        if (!links.contains(link))
            links.add(link);
    }

    private void addSink(String source, String sink) {
        if (!connections.get(source).contains(sink))
            connections.get(source).add(sink);
    }

    private int getNumberOfSinks(String source) {
        return connections.get(source).size();
    }

    private List<String> getValidSinks(String source) {
        return connections.entrySet().stream()
            .filter(e -> !e.getKey().equals(source))
            .filter(e -> e.getValue().size() < cr)
            .filter(e -> !getConnectedSinks(source).contains(e.getKey()))
            .map(e -> e.getKey())
            .collect(Collectors.toList());
    }

    private List<String> getConnectedSinks(String source) {
        return connections.get(source);
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

    private int getRandomNodeIndex(int maxInt) {
        return new Random().nextInt(maxInt);
    }

    public String[] getNodeConnections(String node) {
        return links.stream()
            .filter(l -> l.getSource().equals(node))
            .map(l -> l.getSink())
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

     @Override
    public String toString() {
        return "Overlay{" +
            "cr=" + cr +
            ", nodes=" + nodes +
            ", connections=" + connections +
            ", links=" + links +
            '}';
    }
}
