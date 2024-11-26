package Project.Server;

import java.util.ArrayList;
import java.util.Arrays;
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

        // Note: any desired changes to the message must be done before this section
        long senderId = sender == null ? ServerThread.DEFAULT_CLIENT_ID : sender.getClientId();

        // loop over clients and send out the message; remove client if message failed
        // to be sent
        // Note: this uses a lambda expression for each item in the values() collection,
        // it's one way we can safely remove items during iteration
        info(String.format("sending message to %s recipients: %s", clientsInRoom.size(), message));
        clientsInRoom.values().removeIf(client -> {
            boolean failedToSend = !client.sendMessage(senderId, message);
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
        String formattedResult = String.format("**%s rolled %d dice with %d sides each and got a total of: %d**",
                                               sender.getClientName(), dice, sides, total);
        broadcastMessage(null, formattedResult);
    }
    
    // Rev/11-25-2024 - Show the code on the Room side that changes this format
    public void handleFlip(ServerThread sender) {
        String result = Math.random() < 0.5 ? "Heads" : "Tails";
        String formattedResult = String.format("**%s flipped a coin and got: %s**", sender.getClientName(), result);
        broadcastMessage(null, formattedResult);
    }
    

    private ServerThread getClientByName(String name) {
        for (ServerThread client : clients) {
            if (client.getClientName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }
    
    public void handlePrivateMessage(ServerThread sender, String targetName, String message) {
        ServerThread receiver = getClientByName(targetName.trim().toLowerCase());
        if (receiver == null) {
            sender.sendMessage("Error: User " + targetName + " does not exist.");
            return;
        }
        // Check if sender is muted by the receiver
        if (receiver.isMuted(sender.getClientName())) {
            sender.sendMessage("Your message to " + targetName + " was not delivered. You are muted by them.");
            return;
        }
        // Send message to both sender and receiver
        receiver.sendMessage(sender.getClientName() + " (private): " + message);
        sender.sendMessage("(to " + targetName + "): " + message);
    }

    public void handleMute(ServerThread sender, String targetName) {
        ServerThread target = getClientByName(targetName.trim().toLowerCase());
        if (target == null) {
            sender.sendMessage("Error: User " + targetName + " does not exist.");
            return;
        }
        sender.mute(targetName);
        sender.sendMessage("You have muted " + targetName + ".");
    }
    
    public void handleUnmute(ServerThread sender, String targetName) {
        ServerThread target = getClientByName(targetName.trim().toLowerCase());
        if (target == null) {
            sender.sendMessage("Error: User " + targetName + " does not exist.");
            return;
        }
        sender.unmute(targetName);
        sender.sendMessage("You have unmuted " + targetName + ".");
    }   

    public String formatMessage(String message) {   // - Rev/11-16-2024
        boolean wasCommand = false;
        String command = message; // - Rev/11-23-2024
			//BOLD
			//makes sure there is at least one pair of "bold" symbols i.e *bold*
			if (command.matches("(.*)\\*(.+)\\*(.*)")) {
				int count = 0;
				String changeText = "";
			ArrayList<String> tags = new ArrayList<String>();
			for (int i = 0; i < command.length(); i++) {
				if (Character.toString(command.charAt(i)).equals("*")) {
					count++;
					if (count %2 != 0) {
						tags.add("<b>");
					}
					else {
						tags.add("</b>");
					}
					
				}
			}
			String [] bold = command.split("\\*");
			
			//accounts for "***" as a potential text option
			if (bold.length == 0) {
				changeText += "<b>*</b>";
						if (command.length() > 3)
					changeText += command.substring(3);
				
			}
			for (int i = 0; i < bold.length; i++) {
				
				// accounts for odd number of "*"
				//  and also two "*" in a row
				if (tags.size() == 1 && tags.get(tags.size()-1).contains("<b>") || (bold[i].equals("") && (bold[i+1].equals("")))) {
					changeText += bold[i] + "*";
					tags.remove(0);
				}
				// convet "**" pairs to "<b></b>"
				else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</b>")) ){
					changeText += bold[i] + tags.get(0);
					tags.remove(0);
				}
				else changeText += bold[i];
				}
	    	wasCommand = true;
	    	
	    	// makes it so that conditions stack; 
	    	// can have more than one type of function applied on the same line
	    	if (changeText != "") {
				command = changeText;
			}
	    }
			// ITALICS
			// same logic as bold
			if (command.matches("(.*)_(.+)_(.*)")) {
				int count = 0;
				String changeText = "";
				ArrayList<String> tags = new ArrayList<String>();
				for (int i = 0; i < command.length(); i++) {
					if (Character.toString(command.charAt(i)).equals("_")) {
						count++;
						if (count %2 != 0) {
							tags.add("<i>");
						}
						else {
							tags.add("</i>");
						}
					}
				}
				String [] italics = command.split("_");
				if (italics.length == 0) {
					changeText += "<i>_</i>";
							if (command.length() > 3)
						changeText += command.substring(3);
					
				}
				for (int i = 0; i < italics.length; i++) {
					if (tags.size() == 1 && tags.get(tags.size()-1).contains("<i>") || (italics[i].equals("") && (italics[i+1].equals("")))) {
						changeText += italics[i] + "_";
						tags.remove(0);
					}
					else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</i>")) ){ //if (i % 2 == 0) {
						changeText += italics[i] + tags.get(0);
						tags.remove(0);
					}
					else changeText += italics[i];
					}
		    	wasCommand = true;
		    	if (changeText != "") {
					command = changeText;
				}
	    }
			// UNDERLINE
			// same logic as bold
			if (command.matches("(.*)~(.+)~(.*)")) {
				int count = 0;
				String changeText = "";
				ArrayList<String> tags = new ArrayList<String>();
				for (int i = 0; i < command.length(); i++) {
					if (Character.toString(command.charAt(i)).equals("~")) {
						count++;
						if (count %2 != 0) {
							tags.add("<u>");
						}
						else {
							tags.add("</u>");
						}
						
					}
				}
				String [] underline = command.split("~");
				if (underline.length == 0) {
					changeText += "<b>~</b>";
							if (command.length() > 3)
						changeText += command.substring(3);
					
				}			
				for (int i = 0; i < underline.length; i++) {
					if (tags.size() == 1 && tags.get(tags.size()-1).contains("<u>") || (underline[i].equals("") && (underline[i+1].equals("")))) {
						changeText += underline[i] + "~";
						tags.remove(0);
					}
					else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</i>")) ){ //if (i % 2 == 0) {
						changeText += underline[i] + tags.get(0);
						tags.remove(0);
					}
					else changeText += underline[i];
					}
		    	wasCommand = true;
		    	if (changeText != "") {
					command = changeText;
				}
	    }

        // COLOR
        // Rev/11-23-2024
		 // change color by declaring a color between two pound signs
			// e.x #red# this text would be red
			// to change back to black/default type '##'
			// color will stay black if declared color does not exist 
				//e.x #null# this will stay black
			if (command.matches("(.*)#(.+)#(.*)")) {
				String changeText = "";
				String colorString = "black";;
				String [] color = command.split("#", -1);
				System.out.println(Arrays.toString(color));
				if (color.length == 0) {
					changeText += command;	
				}	
					for (int i = 0; i < color.length; i++) {
						
						if (i % 2 != 0 && (!color[i].contains(" "))){
							// accounts for odd number of #
							if (i == color.length-1) {
								changeText += "#" + color[i];
							}
							else { 
								//if text is between two pound signs declare that as color variable
								colorString = color[i];
								if (colorString.equals("")) { colorString = "black"; }
							}
						}
						// will not work if whitespace between pound signs i.e. # #
						else if (i % 2 != 0 && (color[i].contains(" "))){
							changeText += "#" + color[i] + "#";
						}
						// append message
						else { 
							changeText += "<font color="+colorString+">" + color[i] + "</font>";
							colorString="black";
						}
					}
				wasCommand = true;
				if (changeText != "") {
					command = changeText;
				}
				
			}
	   
	    	if (wasCommand == true) {
		    	formatMessage(command);
		}
        return command;
    }

    public void broadcastMessage(ServerThread sender, String message) {
            String formattedMessage = formatMessage(message);
            for (ServerThread client : clients) {
                client.sendMessage(formattedMessage);
            }
        }
    // end receive data from ServerThread
}