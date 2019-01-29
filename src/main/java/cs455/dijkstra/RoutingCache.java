package cs455.dijkstra;

import cs455.util.Link;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoutingCache {
    private final Dijkstra dijkstra;
    private Map<String, String> nextHop = new HashMap<>();

    private RoutingCache(String startNode, Link[] links) {
        this.dijkstra = new Dijkstra(links);
        dijkstra.computeShortestPath(startNode);
        dijkstra.initializeNextHop();
    }

    public static RoutingCache of(String startNode, Link[] links) {
        return new RoutingCache(startNode, links);
    }

    public String getNextHop(String sink) {
        return nextHop.get(sink);
    }

    private class Dijkstra {
        private Map<String, List<Link>> graph = new HashMap<>();
        private Set<String> settledNodes = new HashSet<>();
        private Set<String> unsettledNodes = new HashSet<>();
        private Map<String, String> predecessors = new HashMap<>();
        private Map<String, Integer> distance = new HashMap<>();

        private Dijkstra(Link[] links) {
            initializeGraph(links);
        }

        private void initializeGraph(Link[] links) {
            Stream.of(links)
                .forEach(this::addLinkToGraph);
        }

        private void addLinkToGraph(Link link) {
            graph.computeIfAbsent(link.getSource(), l -> new ArrayList<>()).add(link);
        }

        void computeShortestPath(String source) {
            distance.put(source, 0);
            unsettledNodes.add(source);

            while (unsettledNodes.size() > 0) {
                String node = getMinimum(unsettledNodes);
                settledNodes.add(node);
                unsettledNodes.remove(node);
                findMinimalDistances(node);
            }
        }

        void initializeNextHop() {
            for (String node : graph.keySet()) {
                List<String> path = dijkstra.getPath(node);
                if (path == null)
                    throw new RuntimeException("expected to find a path");

                if (path.size() < 2)
                    throw new RuntimeException("expected to find sink in path");

                nextHop.put(node, path.get(1));
            }
        }

        List<String> getPath(String target) {
            List<String> path = new LinkedList<>();
            String step = target;
            if (predecessors.get(step) == null)
                return null;

            path.add(step);
            while (predecessors.get(step) != null) {
                step = predecessors.get(step);
                path.add(step);
            }

            Collections.reverse(path);
            return path;
        }

        private void findMinimalDistances(String node) {
            for (String target : getNeighbors(node)) {
                if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
                    distance.put(target, getShortestDistance(node) + getDistance(node, target));
                    predecessors.put(target, node);
                    unsettledNodes.add(target);
                }
            }
        }

        private int getDistance(String node, String target) {
            return graph.get(node).stream()
                .filter(l -> l.getSink().equals(target))
                .map(Link::getWeight)
                .findFirst().get();
        }

        private List<String> getNeighbors(String node) {
            return graph.get(node).stream()
                .map(Link::getSink)
                .filter(this::isNotSettled)
                .collect(Collectors.toList());
        }

        private boolean isNotSettled(String node) {
            return !settledNodes.contains(node);
        }

        private String getMinimum(Set<String> unsettledNodes) {
            String minimum = null;
            for (String node : unsettledNodes) {
                if (minimum == null)
                    minimum = node;
                else if (getShortestDistance(node) < getShortestDistance(minimum))
                    minimum = node;
            }
            return minimum;
        }

        private int getShortestDistance(String node) {
            Integer d = distance.get(node);
            return d == null ? Integer.MAX_VALUE : d;
        }
    }

}
