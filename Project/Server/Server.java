package Project.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import Project.Common.LoggerUtil;

public enum Server {
    INSTANCE;

    {
        // Initialize LoggerUtil
        LoggerUtil.LoggerConfig config = new LoggerUtil.LoggerConfig();
        config.setFileSizeLimit(2048 * 1024); // 2MB
        config.setFileCount(1);
        config.setLogLocation("server.log");
        LoggerUtil.INSTANCE.setConfig(config);
    }

        private int port = 3000;
        private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
        private boolean isRunning = true;
        private long nextClientId = 1;
        @SuppressWarnings("unused")
        private long serverStartTime; // Tracks server uptime
        @SuppressWarnings("unused")
        private ServerSocket serverSocket; // Moved to instance variable for accessibility

    private Server() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerUtil.INSTANCE.info("JVM is shutting down. Perform cleanup tasks.");
            shutdown();
        }));
    }

    private void start(int port) {
        this.port = port;
        serverStartTime = System.currentTimeMillis(); // Initialize start time
        LoggerUtil.INSTANCE.info("Listening on port " + this.port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket; // Assign to instance variable
            createRoom(Room.LOBBY); // Create the lobby room
            while (isRunning) {
                LoggerUtil.INSTANCE.info("Waiting for next client");
                Socket incomingClient = serverSocket.accept();
                LoggerUtil.INSTANCE.info("Client connected");

                ServerThread sClient = new ServerThread(incomingClient, this::onClientInitialized);
                sClient.start();
            }
        } catch (IOException e) {
            LoggerUtil.INSTANCE.severe("Error accepting connection", e);
        } finally {
            shutdown();
            LoggerUtil.INSTANCE.info("Closing server socket");
        }
    }

    void shutdown() {
        try {
            rooms.values().removeIf(room -> {
                room.disconnectAll();
                return true;
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error cleaning up rooms", e);
        }
    }

    private void onClientInitialized(ServerThread sClient) {
        sClient.sendClientId(nextClientId++);
        if (nextClientId < 0) {
            nextClientId = 1;
        }

        LoggerUtil.INSTANCE.info(String.format("Server: *%s[%s] initialized*",
                sClient.getClientName(), sClient.getClientId()));
        joinRoom(Room.LOBBY, sClient);
    }

    protected boolean createRoom(String name) {
        final String nameCheck = name.toLowerCase();
        if (rooms.containsKey(nameCheck)) {
            return false;
        }
        Room room = new Room(name);
        rooms.put(nameCheck, room);
        LoggerUtil.INSTANCE.info(String.format("Created new Room %s", name));
        return true;
    }

    protected boolean joinRoom(String name, ServerThread client) {
        final String nameCheck = name.toLowerCase();
        if (!rooms.containsKey(nameCheck)) {
            return false;
        }
        Room current = client.getCurrentRoom();
        if (current != null) {
            current.removedClient(client);
        }
        Room next = rooms.get(nameCheck);
        next.addClient(client);
        return true;
    }

    protected List<String> listRooms(String roomQuery) {
        final String nameCheck = roomQuery.toLowerCase();
        return rooms.values().stream()
                .filter(room -> room.getName().toLowerCase().contains(nameCheck))
                .map(Room::getName)
                .collect(Collectors.toList());
    }

    protected void removeRoom(Room room) {
        rooms.remove(room.getName().toLowerCase());
        LoggerUtil.INSTANCE.info(String.format("Server removed room %s", room.getName()));
    }

    //Extra Credit Features - Milestone4 - rev/12/4/2024
    public static void main(String[] args) {
        boolean headless = Arrays.asList(args).contains("--headless");
        Server server = Server.INSTANCE;

        if (!headless) {
            SwingUtilities.invokeLater(() -> new ServerUI().setVisible(true)); // Delegate UI to ServerUI
        }

        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.warning("Invalid port argument. Defaulting to 3000.");
        }

        server.start(port); // Start server
        LoggerUtil.INSTANCE.info("Server Stopped");
    }
}
