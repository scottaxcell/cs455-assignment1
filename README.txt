Scott Axcell
CS455 Homework 1: Programming Component
02-04-2019

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
 - MessagingNode.java :
 - Registry.java

wireformats
 - TrafficSummary.java
 - RegisterResponse.java
 - PullTrafficSummary.java
 - MessagingNodesList.java
 - Handshake.java
 - DeregisterResponse.java
 - TaskComplete.java
 - RegisterRequest.java
 - LinkWeights.java
 - Status.java
 - EventFactory.java
 - TaskInitiate.java
 - DeregisterRequest.java
 - Event.java
 - Protocol.java
 - Message.java

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
 - RoutingCache.java
