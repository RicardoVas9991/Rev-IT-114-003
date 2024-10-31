// rev/11-02-2024 

package Project.Common;

public enum PayloadType {
    CLIENT_CONNECT, // client requesting to connect to server (passing of initialization data [name])
    CLIENT_ID,  // server sending client id
    SYNC_CLIENT,  // silent syncing of clients in room
    DISCONNECT,  // distinct disconnect action
    ROOM_CREATE,
    ROOM_JOIN, // join/leave room based on boolean
    MESSAGE, // sender and message,
    ROOM_LIST, // client: query for rooms, server: result of query,
    READY, // client to trigger themselves as ready, server to sync the related status of a particular client
    SYNC_READY, // quiet version of READY, used to sync existing ready status of clients in a GameRoom
    RESET_READY, // trigger to tell the client to reset their whole local list's ready status (saves network requests)
    PHASE, // syncs current phase of session (used as a switch to only allow certain logic to execute)
    MOVE, // syncs as a point/coordinate
    GRID_DIMENSION, // syncs grid dimension for server-side controlled grid building
    TURN, // used for syncing turn data
    QUESTION,         // New: Server sends question and options to clients
    ANSWER_SUBMIT,    // New: Client submits an answer
    POINT_UPDATE,     // New: Sync points after each round
    SCOREBOARD_UPDATE, // New: Send scoreboard at end of each round or session
    ANSWER
}