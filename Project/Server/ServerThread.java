package Project.Server;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import Project.Common.PayloadType;
import Project.Common.PrivateMessagePayload;
import Project.Common.RoomResultsPayload;
import Project.Common.Payload;

import Project.Common.ConnectionPayload;
import Project.Common.LoggerUtil;

import Project.Common.RollPayload;

/**
 * A server-side representation of a single client.
 * This class is more about the data and abstracted communication
 */
public class ServerThread extends BaseServerThread {
    public static final long DEFAULT_CLIENT_ID = -1;
    private Room currentRoom;
    private long clientId;
    private String clientName;
    private Consumer<ServerThread> onInitializationComplete; // callback to inform when this object is ready

    /**
     * Wraps the Socket connection and takes a Server reference and a callback
     * 
     * @param myClient
     * @param server
     * @param onInitializationComplete method to inform listener that this object is
     *                                 ready
     */
    protected ServerThread(Socket myClient, Consumer<ServerThread> onInitializationComplete) {
        Objects.requireNonNull(myClient, "Client socket cannot be null");
        Objects.requireNonNull(onInitializationComplete, "callback cannot be null");
        info("ServerThread created");
        // get communication channels to single client
        this.client = myClient;
        this.clientId = ServerThread.DEFAULT_CLIENT_ID;// this is updated later by the server
        this.onInitializationComplete = onInitializationComplete;

    }

    public void setClientName(String name) {
        if (name == null) {
            throw new NullPointerException("Client name can't be null");
        }
        this.clientName = name;
        onInitialized();
    }

// Rev/11-23-2024
    private List<String> mutedClients = new ArrayList<>();
    
    public void MuteManager(String clientName) {
        this.clientName = clientName.toLowerCase().trim();
        load(); // Load the mute list during initialization
    }

    public List<String> getMutedClients() {
        return this.mutedClients;
    }

    public void mute(String name) {
        name = name.trim().toLowerCase();
        if (!isMuted(name)) {
            mutedClients.add(name);
            sendMessageToClient(name, "You have been muted by " + this.clientName);
            save();
        }
    }

    public void unmute(String name) {
        name = name.trim().toLowerCase();
        if (isMuted(name)) {
            mutedClients.remove(name);
            sendMessageToClient(name, "You have been unmuted by " + this.clientName);
            save();
        }
    }

    public boolean isMuted(String name) {
        name = name.trim().toLowerCase();
        return mutedClients.contains(name);
    }

    private void save() {
        try {
            Path filePath = Paths.get(this.clientName + ".html");
            Files.write(filePath, mutedClients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        try {
            Path filePath = Paths.get(this.clientName + ".html");
            if (Files.exists(filePath)) {
                mutedClients = Files.readAllLines(filePath).stream()
                                   .map(String::toLowerCase)
                                   .collect(Collectors.toList());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToClient(String target, String message) {
        ServerThread targetClient = currentRoom.getClientByName(target);
        if (targetClient != null) {
            targetClient.processPayload(new Payload()); // Example usage
        } else {
            System.err.println("Target client not found: " + target);
        }
    }
    

    public String getClientName() {
        return clientName;
    }

    public long getClientId() {
        return this.clientId;
    }

    protected Room getCurrentRoom() {
        return this.currentRoom;
    }

    protected void setCurrentRoom(Room room) {
        if (room == null) {
            throw new NullPointerException("Room argument can't be null");
        }
        currentRoom = room;
    }

    @Override
    protected void onInitialized() {
        onInitializationComplete.accept(this); // Notify server that initialization is complete
    }

    @Override
    protected void info(String message) {
        LoggerUtil.INSTANCE.info(String.format("ServerThread[%s(%s)]: %s", getClientName(), getClientId(), message));
    }

    @Override
    protected void cleanup() {
        currentRoom = null;
        super.cleanup();
    }

    @Override
    protected void disconnect() {
        // sendDisconnect(clientId, clientName);
        super.disconnect();
    }

    // handle received message from the Client
    @Override
    protected void processPayload(Payload payload) {
        try {
            switch (payload.getPayloadType()) {
                case CLIENT_CONNECT:
                    ConnectionPayload cp = (ConnectionPayload) payload;
                    setClientName(cp.getClientName());
                    break;
                case MESSAGE:
                    if (currentRoom == null) {
                        sendMessage("Error: You are not in a room.");
                        return;
                    }
                    String message = payload.getMessage();
                    if (message.startsWith("@")) {
                        int spaceIndex = message.indexOf(' ');
                        if (spaceIndex > 1) {
                            String targetId = message.substring(1, spaceIndex).trim(); // Use client ID here
                            String privateMessage = message.substring(spaceIndex + 1).trim();
                            currentRoom.handlePrivateMessage(this, targetId, privateMessage);
                        } else {
                            sendMessage("Error: Invalid private message format. Use @clientId <message>.");
                        }
                    } else {
                        currentRoom.sendMessage(this, message); // For public messages
                    }
                    break;                
                case ROOM_CREATE:
                    currentRoom.handleCreateRoom(this, payload.getMessage());
                    break;
                case ROOM_JOIN:
                    currentRoom.handleJoinRoom(this, payload.getMessage());
                    break;
                case ROOM_LIST:
                    currentRoom.handleListRooms(this, payload.getMessage());
                    break;
                case DISCONNECT:
                    currentRoom.disconnect(this);
                    break;
                case ROLL:
                    RollPayload rollPayload = (RollPayload) payload;
                    currentRoom.handleRoll(this, rollPayload.getDice(), rollPayload.getSides(), rollPayload.getTotal()); // - Rev/11-16-2024
                    sendMessage("ROLL: " + rollPayload.getDice() + "," + rollPayload.getSides() + " and got a " + rollPayload.getTotal());
                    break;
                case FLIP:
                    currentRoom.handleFlip(this); // - Rev/11/-16-2024
                    sendMessage("FLIP: ");
                    break;
                case MUTE:
                    String muteTarget = payload.getMessage().trim().toLowerCase(); // Extract the target username
                    if (muteTarget.isEmpty()) {
                        sendMessage("Error: Mute command requires a valid username."); // Send error if empty
                    } else {
                        mute(muteTarget);
                        sendMessage("You have muted: " + muteTarget); // Confirmation message
                        LoggerUtil.INSTANCE.info("User " + getClientName() + " muted " + muteTarget); // Log the action
                    }
                    break; // - Rev/11-25-2024
                case UNMUTE: 
                    String unmuteTarget = payload.getMessage().trim().toLowerCase(); // Extract the target username
                    if (unmuteTarget.isEmpty()) {
                        sendMessage("Error: Unmute command requires a valid username."); // Send error if empty
                    } else {
                        unmute(unmuteTarget);
                        sendMessage("You have unmuted: " + unmuteTarget); // Confirmation message
                        LoggerUtil.INSTANCE.info("User " + getClientName() + " unmuted " + unmuteTarget); // Log the action
                    }
                    break; // - Rev/11-25-2024
                case PRIVATE_MESSAGE:
                    PrivateMessagePayload pmp = (PrivateMessagePayload) payload;
                    if (currentRoom != null) {
                        currentRoom.handlePrivateMessage(this, pmp.getTargetId(), pmp.getMessage());
                    } else {
                        sendMessage("Error: You are not in a room.");
                    }
                    break;
                default:
                    currentRoom.sendMessage(this, payload.toString());
                    LoggerUtil.INSTANCE.info("Unhandled payload type: " + payload.getPayloadType());
                    break;
            }
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Could not process Payload: " + payload,e);
        
        }
    }

    // send methods to pass data back to the Client

    public boolean sendRooms(List<String> rooms) {
        RoomResultsPayload rrp = new RoomResultsPayload();
        rrp.setRooms(rooms);
        return send(rrp);
    }

    public boolean sendClientSync(long clientId, String clientName) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        cp.setConnect(true);
        cp.setPayloadType(PayloadType.SYNC_CLIENT);
        return send(cp);
    }

    /**
     * Overload of sendMessage used for server-side generated messages
     * 
     * @param message
     * @return @see {@link #send(Payload)}
     */
    public boolean sendMessage(String message) {
        return sendMessage(ServerThread.DEFAULT_CLIENT_ID, message);
    }

    /**
     * Sends a message with the author/source identifier
     * 
     * @param senderId
     * @param message
     * @return @see {@link #send(Payload)}
     */
    public boolean sendMessage(long senderId, String message) {
        Payload p = new Payload();
        p.setClientId(senderId);
        p.setMessage(message);
        p.setPayloadType(PayloadType.MESSAGE);
        return send(p);
    }

    public void processMuteCommands(String command) {
        String[] parts = command.split(" ", 2);
        String action = parts[0].toLowerCase();
        String target = parts.length > 1 ? parts[1].trim() : null;
    
        if (action.equals("/mute") && target != null) {
            mute(target);
        } else if (action.equals("/unmute") && target != null) {
            unmute(target);
        }
    }
    

    /**
     * Tells the client information about a client joining/leaving a room
     * 
     * @param clientId   their unique identifier
     * @param clientName their name
     * @param room       the room
     * @param isJoin     true for join, false for leaivng
     * @return success of sending the payload
     */
    public boolean sendRoomAction(long clientId, String clientName, String room, boolean isJoin) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.ROOM_JOIN);
        cp.setConnect(isJoin); // <-- determine if join or leave
        cp.setMessage(room);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    /**
     * Tells the client information about a disconnect (similar to leaving a room)
     * 
     * @param clientId   their unique identifier
     * @param clientName their name
     * @return success of sending the payload
     */
    public boolean sendDisconnect(long clientId, String clientName) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.DISCONNECT);
        cp.setConnect(false);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    /**
     * Sends (and sets) this client their id (typically when they first connect)
     * 
     * @param clientId
     * @return success of sending the payload
     */
    public boolean sendClientId(long clientId) {
        this.clientId = clientId;
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.CLIENT_ID);
        cp.setConnect(true);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    // end send methods
}