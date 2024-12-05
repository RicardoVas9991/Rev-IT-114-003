package Project.Common;

public enum PayloadType {
    CLIENT_CONNECT, // client requesting to connect to server (passing of initialization data [name])
    CLIENT_ID,  // server sending client id
    SYNC_CLIENT,  // silent syncing of clients in room
    DISCONNECT,  // distinct disconnect action
    ROOM_CREATE,
    ROOM_JOIN, // join/leave room based on boolean
    MESSAGE, // sender and message,
    ROOM_LIST, // client: query for rooms, server: result of query
    ROLL, 
    FLIP, // Add other types as needed  - rev/11-14-2024  
    CONNECT,
    PRIVATE_MESSAGE, // Added for private messaging - Rev/11-23-2024
    COMMAND,
    MUTE,        // Added for mute functionality - Rev/11-23-2024
    UNMUTE;       // Added for unmute functionality - Rev/11-23-2024
}