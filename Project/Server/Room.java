package Project.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


import Project.Common.LoggerUtil;

public class Room implements AutoCloseable{
    private String name;// unique name of the Room
    protected volatile boolean isRunning = false;
    private ConcurrentHashMap<Long, ServerThread> clientsInRoom = new ConcurrentHashMap<Long, ServerThread>();
    private List<ServerThread> clients = new ArrayList<>();

    public final static String LOBBY = "lobby";

    private void info(String message) {
        LoggerUtil.INSTANCE.info(String.format("Room[%s]: %s", name, message));
    }

    public Room(String name) {
        this.name = name;
        isRunning = true;
        info("created");
    }

    public String getName() {
        return this.name;
    }

    protected synchronized void addClient(ServerThread client) {
        if (!isRunning) { // block action if Room isn't running
            return;
        }
        if (clientsInRoom.containsKey(client.getClientId())) {
            info("Attempting to add a client that already exists in the room");
            return;
        }
        clientsInRoom.put(client.getClientId(), client);
        client.setCurrentRoom(this);

        // notify clients of someone joining
        sendRoomStatus(client.getClientId(), client.getClientName(), true);
        // sync room state to joiner
        syncRoomList(client);

        info(String.format("%s[%s] joined the Room[%s]", client.getClientName(), client.getClientId(), getName()));

    }

    protected synchronized void removedClient(ServerThread client) {
        if (!isRunning) { // block action if Room isn't running
            return;
        }
        // notify remaining clients of someone leaving
        // happen before removal so leaving client gets the data
        sendRoomStatus(client.getClientId(), client.getClientName(), false);
        clientsInRoom.remove(client.getClientId());
        LoggerUtil.INSTANCE.fine("Clients remaining in Room: " + clientsInRoom.size());

        info(String.format("%s[%s] left the room", client.getClientName(), client.getClientId(), getName()));

        autoCleanup();

    }

    /**
     * Takes a ServerThread and removes them from the Server
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param client
     */
    protected synchronized void disconnect(ServerThread client) {
        if (!isRunning) { // block action if Room isn't running
            return;
        }
        long id = client.getClientId();
        sendDisconnect(client);
        client.disconnect();
        // removedClient(client); // <-- use this just for normal room leaving
        clientsInRoom.remove(client.getClientId());
        LoggerUtil.INSTANCE.fine("Clients remaining in Room: " + clientsInRoom.size());
        
        // Improved logging with user data
        info(String.format("%s[%s] disconnected", client.getClientName(), id));
        autoCleanup();
    }

    protected synchronized void disconnectAll() {
        info("Disconnect All triggered");
        if (!isRunning) {
            return;
        }
        clientsInRoom.values().removeIf(client -> {
            disconnect(client);
            return true;
        });
        info("Disconnect All finished");
        autoCleanup();
    }

    /**
     * Attempts to close the room to free up resources if it's empty
     */
    private void autoCleanup() {
        if (!Room.LOBBY.equalsIgnoreCase(name) && clientsInRoom.isEmpty()) {
            close();
        }
    }

    public void close() {
        // attempt to gracefully close and migrate clients
        if (!clientsInRoom.isEmpty()) {
            sendMessage(null, "Room is shutting down, migrating to lobby");
            info(String.format("migrating %s clients", name, clientsInRoom.size()));
            clientsInRoom.values().removeIf(client -> {
                Server.INSTANCE.joinRoom(Room.LOBBY, client);
                return true;
            });
        }
        Server.INSTANCE.removeRoom(this);
        isRunning = false;
        clientsInRoom.clear();
        info(String.format("closed", name));
    }

    // send/sync data to client(s)

    /**
     * Sends to all clients details of a disconnect client
     * @param client
     */
    protected synchronized void sendDisconnect(ServerThread client) {
        info(String.format("sending disconnect status to %s recipients", clientsInRoom.size()));
        clientsInRoom.values().removeIf(clientInRoom -> {
            boolean failedToSend = !clientInRoom.sendDisconnect(client.getClientId(), client.getClientName());
            if (failedToSend) {
                info(String.format("Removing disconnected client[%s] from list", client.getClientId()));
                disconnect(client);
            }
            return failedToSend;
        });
    }

    /**
     * Syncs info of existing users in room with the client
     * 
     * @param client
     */
    protected synchronized void syncRoomList(ServerThread client) {

        clientsInRoom.values().forEach(clientInRoom -> {
            if (clientInRoom.getClientId() != client.getClientId()) {
                client.sendClientSync(clientInRoom.getClientId(), clientInRoom.getClientName());
            }
        });
    }

    /**
     * Syncs room status of one client to all connected clients
     * 
     * @param clientId
     * @param clientName
     * @param isConnect
     */
    protected synchronized void sendRoomStatus(long clientId, String clientName, boolean isConnect) {
        info(String.format("sending room status to %s recipients", clientsInRoom.size()));
        clientsInRoom.values().removeIf(client -> {
            boolean failedToSend = !client.sendRoomAction(clientId, clientName, getName(), isConnect);
            if (failedToSend) {
                info(String.format("Removing disconnected client[%s] from list", client.getClientId()));
                disconnect(client);
            }
            return failedToSend;
        });
    }

    /**
     * Sends a basic String message from the sender to all connectedClients
     * Internally calls processCommand and evaluates as necessary.
     * Note: Clients that fail to receive a message get removed from
     * connectedClients.
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param message
     * @param sender  ServerThread (client) sending the message or null if it's a
     *                server-generated message
     */
    protected synchronized void sendMessage(ServerThread sender, String message) {
        if (!isRunning) { // block action if Room isn't running
            return;
        }
        // Rev/12-09-2024
        String formatting = message.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")    
                            .replaceAll("\\*(.*?)\\*", "<i>$1</i>")        
                            .replaceAll("_(.*?)_", "<u>$1</u>")              
                            .replaceAll("#r(.*?)r#", "<font color='#FF0000'>$1</font>")        
                            .replaceAll("#b(.*?)b#", "<font color='#0000FF'>$1</font>")
                            .replaceAll("#g(.*?)g#", "<font color='#008000'>$1</font>"); 


        // Note: any desired changes to the message must be done before this section
        long senderId = sender == null ? ServerThread.DEFAULT_CLIENT_ID : sender.getClientId();

        // loop over clients and send out the message; remove client if message failed
        // to be sent
        // Note: this uses a lambda expression for each item in the values() collection,
        // it's one way we can safely remove items during iteration

        info(String.format("sending message to %s recipients: %s", clientsInRoom.size(), formatting));
        clientsInRoom.values().removeIf(client -> {
            boolean failedToSend = !client.sendMessage(senderId, formatting);
            if (failedToSend) {
                info(String.format("Removing disconnected client[%s] from list", client.getClientId()));
                disconnect(client);
            }
            return failedToSend;
        });

        for (ServerThread client : clients) {
            // Skip sending messages to clients who have muted the sender
            if (client.isMuted(sender.getClientName())) {
                LoggerUtil.INSTANCE.info("Message skipped for " + client.getClientName() + " (muted " + sender.getClientName() + ").");
                continue;
            }
            client.sendMessage(sender.getClientName() + ": " + message);
        }
    }
    // end send data to client(s)

    // receive data from ServerThread
    
    protected void handleCreateRoom(ServerThread sender, String room) {
        if (Server.INSTANCE.createRoom(room)) {
            Server.INSTANCE.joinRoom(room, sender);
        } else {
            sender.sendMessage(String.format("Room %s already exists", room));
        }
    }

    protected void handleJoinRoom(ServerThread sender, String room) {
        if (!Server.INSTANCE.joinRoom(room, sender)) {
            sender.sendMessage(String.format("Room %s doesn't exist", room));
        }
    }

    protected void handleListRooms(ServerThread sender, String roomQuery){
        sender.sendRooms(Server.INSTANCE.listRooms(roomQuery));
    }

    protected void clientDisconnect(ServerThread sender) {
        disconnect(sender);
    }
    // Rev/11-25-2024 - Show the code on the Room side that changes this format
    public void handleRoll(ServerThread sender, int dice, int sides, int total) {
        String formattedResult = String.format("**%s rolled %d dice with %d sides each and got a total of: %d**", sender.getClientName(), dice, sides, total);
        sender.sendMessage(formattedResult);
    }
    
    // Rev/11-25-2024 - Show the code on the Room side that changes this format
    public void handleFlip(ServerThread sender) {
        String result = Math.random() < 0.5 ? "Heads" : "Tails";
        String formattedResult = String.format("**%s flipped a coin and got a: %s**", sender.getClientName(), result);
        sender.sendMessage(formattedResult);
    }
    

    ServerThread getClientByName(String name) {
        for (ServerThread client : clients) {
            if (client.getClientName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }

    public ServerThread getClientById(String clientId) {
        for (ServerThread client : clients) {
            if (String.valueOf(client.getClientId()).equals(clientId)) {
                return client;
            }
        }
        return null; // Return null if not found
    }
    
    
    public void handlePrivateMessage(ServerThread sender, String targetId, String message) {
        ServerThread receiver = getClientById(targetId);
        if (receiver == null) {
            sender.sendMessage("Error: User with ID " + targetId + " does not exist.");
            return;
        }
    
        // Check if the sender is muted by the receiver
        if (receiver.isMuted(sender.getClientName())) {
            sender.sendMessage("Your message to " + receiver.getClientName() + " was not delivered. You are muted by them.");
            return;
        }
    
        // Send formatted messages to both sender and receiver
        String senderMessage = formatMessage("(to " + receiver.getClientName() + "): " + message);
        String receiverMessage = formatMessage(sender.getClientName() + " (private): " + message);
    
        sender.sendMessage(senderMessage);
        receiver.sendMessage(receiverMessage);
    }

    public void handleMute(ServerThread sender, String targetName) {
        ServerThread target = getClientByName(targetName.trim().toLowerCase());
        if (target == null) {
            sender.sendMessage("Error: User " + targetName + " does not exist.");
            return;
        }
        sender.mute(targetName);
        sender.sendMessage("You have muted " + targetName + ".");
        sendMessage(target, "You have been muted by " + sender);
    }
    
    public void handleUnmute(ServerThread sender, String targetName) {
        ServerThread target = getClientByName(targetName.trim().toLowerCase());
        if (target == null) {
            sender.sendMessage("Error: User " + targetName + " does not exist.");
            return;
        }
        sender.unmute(targetName);
        sender.sendMessage("You have unmuted " + targetName + ".");
        sendMessage(target, "You have been unmuted by " + sender);
    }   

    public String formatMessage(String message) { // Rev/11-23-2024
        if (message == null || message.isEmpty()) {
            return message; // Return as-is for empty/null input
        }
    
        // Apply Bold, Italics, and Underline
        message = applyFormatting(message, "*", "<b>", "</b>");
        message = applyFormatting(message, "_", "<i>", "</i>");
        message = applyFormatting(message, "~", "<u>", "</u>");
    
        // Apply Color Formatting
        if (message.contains("#")) {
            message = applyColorFormatting(message);
        }
    
        return message;
    }
    
    private String applyFormatting(String text, String delimiter, String openTag, String closeTag) {
        if (!text.contains(delimiter)) return text; // Skip if no delimiter found
    
        StringBuilder result = new StringBuilder();
        boolean isOpen = false; // Track tag state
        for (char c : text.toCharArray()) {
            if (String.valueOf(c).equals(delimiter)) {
                result.append(isOpen ? closeTag : openTag);
                isOpen = !isOpen;
            } else {
                result.append(c);
            }
        }
    
        // Ensure closing tag is added for unbalanced delimiters
        if (isOpen) {
            result.append(closeTag);
        }
    
        return result.toString();
    }
    
    private String applyColorFormatting(String text) {
        StringBuilder result = new StringBuilder();
        String[] parts = text.split("#");
        String color = "black"; // Default color
    
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1) { // Odd indices are color codes
                String potentialColor = parts[i].split(" ")[0].trim(); // Extract potential color code
                if (isValidColorCode(potentialColor)) {
                    color = potentialColor; // Update color if valid
                }
                parts[i] = parts[i].substring(potentialColor.length()).trim(); // Remove color code
            }
            result.append("<font color=\"").append(color).append("\">").append(parts[i]).append("</font>");
            color = "black"; // Reset to default after each segment
        }
    
        return result.toString();
    }
    
    private boolean isValidColorCode(String color) {
        // Validate named color or hexadecimal color code
        return color.matches("^[a-zA-Z]+$") || color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }
    
    // end receive data from ServerThread
}