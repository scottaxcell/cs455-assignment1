package cs455.node;

public class Link {
    private String source;
    private String sink;
    private int weight;

    private Link(String source, String sink) {
        this.source = source;
        this.sink = sink;
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
}
