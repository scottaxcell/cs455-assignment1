Scott Axcell
CS455 Homework 1: Programming Component
02-13-2019

Description:
  Using Dijkstras Shortest Paths to Route Packets in a Network Overlay

Build:
  gradle build

Run:
  java cs455.overlay.node.Registry portnum
  Commandline inputs:
    * list-messaging-nodes: prints information about the messaging nodes to stdout
    * list-weights: prints information about the links compromising the overlay
    * setup-overlay number-of-connections: initializes network overlay
    * send-overlay-link-weights: sends link weight information to nodes in the overlay
    * start number-of-rounds: initiates sending of random messages between nodes in the overlay

  java cs455.overlay.node.MessagingNode registry-host registry-port
  Commandline inputs:
    * print-shortest-path: prints the shortest paths to connected nodes to stdout
    * exit-overlay: sends a message to remove itself from the network overlay and terminates the process

Classes:
node
 - Node.java : interface that defines the onEvent method
 - MessagingNode.java : initiates and accepts both communications and messages within the system
 - Registry.java :  maintains information about the registered messaging nodes in a registry

wireformats
 - TrafficSummary.java : transmits traffic summary data
 - RegisterResponse.java : notifies a node of registration success or failure
 - PullTrafficSummary.java : requests a traffic summary from a node
 - MessagingNodesList.java : transmitting node connections within the overlay
 - Handshake.java : initiates a connection between nodes
 - DeregisterResponse.java : notifies a node of deregistration from the registry
 - TaskComplete.java : notifies registry of completion of messaging task
 - RegisterRequest.java : notifies the registry that a node would like to register
 - LinkWeights.java : lists the links and weights between all nodes within the overlay
 - Status.java : interface that defines success of failure
 - EventFactory.java : responsible for creating a POJO provided a data from a DataInputStream
 - TaskInitiate.java : notifies a node to begin the messaging task
 - DeregisterRequest.java : notifies the registry that a node would like to deregister
 - Event.java : interface that defines methods common to all wireformats (getProtocol() and getBytes())
 - Protocol.java : interface that defines wireformat (message) types
 - Message.java : contains a payload to be used in the messaging task

util
 - TrafficTracker.java : responsible for tracking transmission statistics on a node
 - Link.java: represents a unidirectional connection between two nodes in the overlay
 - StatisticsCollectorAndDisplay.java : responsible for collating and printing transmission statistics
 - Utils.java : miscellaneous utility methods
 - OverlayCreator.java : responsible for creating the k-regular graph network overlay

transport
 - TcpReceiver.java : responsible for reading incoming data on a socket and notifying the node of the event
 - TcpServer.java : server socker responsible for spawning sockets as clients connect
 - TcpConnection.java : represents a TCP connection consisting of a send and receive socket
 - TcpSender.java : responsible for writing outgoing data to a socket

dijkstra
 - RoutingCache.java : responsible for calculating the shortest paths in the overlay using Dijkstra's algorithm

Notes for TA:
The registry and nodes within the overlay should function as expected (fingers crossed).