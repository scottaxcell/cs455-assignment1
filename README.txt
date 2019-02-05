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
 - Node.java
 - MessagingNode.java
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
 - TrafficTracker.java
 - Link.java
 - StatisticsCollectorAndDisplay.java
 - Utils.java
 - OverlayCreator.java

transport
 - TcpReceiver.java
 - TcpServer.java
 - TcpConnection.java
 - TcpSender.java

dijkstra
 - RoutingCache.java
