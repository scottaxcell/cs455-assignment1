package cs455.node;

import java.util.Objects;
import java.util.Random;

public class Link {
    private String source;
    private String sink;
    private int weight;

    private Link(String source, String sink) {
        this.source = source;
        this.sink = sink;
        this.weight = generateRandomWeight();
    }

    private int generateRandomWeight() {
        return new Random().nextInt(10) + 1;
    }

    public static Link of(String source, String sink) {
        return new Link(source, sink);
    }

    public String getSource() {
        return source;
    }

    public String getSink() {
        return sink;
    }

    @Override
    public String toString() {
        return "Link{" +
            "source='" + source + '\'' +
            ", sink='" + sink + '\'' +
            ", weight=" + weight +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return weight == link.weight &&
            Objects.equals(source, link.source) &&
            Objects.equals(sink, link.sink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, sink, weight);
    }

    public String getInfo() {
        return String.format("%s %s %d", getSource(), getSink(), weight);
    }
}
