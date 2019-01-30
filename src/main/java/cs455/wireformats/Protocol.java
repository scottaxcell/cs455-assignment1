package cs455.wireformats;

public interface Protocol {
    int REGISTER_REQUEST = 0;
    int REGISTER_RESPONSE = 1;
    int DEREGISTER_REQUEST = 2;
    int DEREGISTER_RESPONSE = 3;
    int MESSAGING_NODES_LIST = 4;
    int HANDSHAKE = 5;
    int LINK_WEIGHTS = 6;
    int TASK_INITIATE = 7;
    int MESSAGE = 8;
    int TASK_COMPLETE = 9;
    int PULL_TRAFFIC_SUMMARY = 10;
    int TRAFFIC_SUMMARY = 11;
}
