# cs455-assignment1

Using Dijkstra’s Shortest Paths to Route Packets in a Network Overlay

References:
* http://cs.lmu.edu/~ray/notes/javanetexamples/
* http://www.techiedelight.com/convert-inputstream-byte-array-java/
* http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html
* https://techtavern.wordpress.com/2010/11/09/how-to-prevent-eofexception-when-socket-is-closed/
* http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html

Problem I faced that would be useful in writeup:
* used a tcpconnection for connected nodes in node, 
this caused a data race condition that was hard to debug.
effectively a single socket had two threads reading from the
datainpustream and so it appeared as if corrupt messages
were being sent, but in fact it was just two threads
pulling data from a single stream so caused issues. fix
was to use tcpsender, because in reality that's all
was need as the sockets were used for sending messages only.