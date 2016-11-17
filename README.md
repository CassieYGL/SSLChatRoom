# SSLChatRoom

The chat system consists of two main distributed components: chat server and chat client, which may run on different hosts in the network.
Chat clients are Java programs which can connect to a chat server.

The chat server is a Java application which can accept multiple incoming TCP connections. It maintains a list of current chat rooms. 
Chat clients can move between chat rooms. Messages (in JSON format) sent by a chat client are broadcast to all clients currently 
connected to the same chat room.

There is a series of functions including creating new users, username change, joining room, display room details, creating new room,
deleting rooms, kick users etc. Each function has a correpsonding protocol to intepret the JSON messages. 

The SSL protocol is used for the main security mechanism. It consists of authentication certificate, data encryption, message integrity checking.
