package cs455.dijkstra;

import cs455.util.Link;
import cs455.util.Utils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RoutingCacheTest {

    @Test
    public void testLinearTopology() {
        List<Link> links = new ArrayList<>();

        links.add(Link.of("host1:0", "host2:0", 4));
        links.add(Link.of("host2:0", "host1:0", 4));

        links.add(Link.of("host2:0", "host3:0", 3));
        links.add(Link.of("host3:0", "host2:0", 3));

        links.add(Link.of("host3:0", "host4:0", 2));
        links.add(Link.of("host4:0", "host3:0", 2));

        links.add(Link.of("host4:0", "host5:0", 5));
        links.add(Link.of("host5:0", "host4:0", 5));

        links.add(Link.of("host5:0", "host6:0", 2));
        links.add(Link.of("host6:0", "host5:0", 2));

        links.add(Link.of("host6:0", "host7:0", 8));
        links.add(Link.of("host7:0", "host6:0", 8));

        links.add(Link.of("host7:0", "host8:0", 4));
        links.add(Link.of("host8:0", "host7:0", 4));

        links.add(Link.of("host8:0", "host9:0", 1));
        links.add(Link.of("host9:0", "host8:0", 1));

        links.add(Link.of("host9:0", "host0:0", 1));
        links.add(Link.of("host0:0", "host9:0", 1));

        RoutingCache routingCache = RoutingCache.of("host0:0", links.toArray(new Link[links.size()]));
        Utils.out(routingCache.getShortestPathsForPrinting());
    }

    @Test
    public void testSimpleGraph() {
        List<Link> links = new ArrayList<>();

        links.add(Link.of("host1", "host2", 4));
        links.add(Link.of("host2", "host1", 4));

        links.add(Link.of("host1", "host3", 1));
        links.add(Link.of("host3", "host1", 1));

        links.add(Link.of("host2", "host3", 8));
        links.add(Link.of("host3", "host2", 8));

        links.add(Link.of("host3", "host4", 10));
        links.add(Link.of("host4", "host3", 10));

        RoutingCache routingCache = RoutingCache.of("host3", links.toArray(new Link[links.size()]));
        Utils.out(routingCache.getShortestPathsForPrinting());
    }

}