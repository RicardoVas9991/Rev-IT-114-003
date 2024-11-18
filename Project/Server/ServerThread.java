package Project.Server;

import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import Project.Common.PayloadType;
import Project.Common.Phase;
import Project.Common.ReadyPayload;
import Project.Common.RoomResultsPayload;
import Project.Common.Payload;

import Project.Common.ConnectionPayload;
import Project.Common.LoggerUtil;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameRoom gameRoom;

    public ServerThread(Socket socket, GameRoom gameRoom) {
        this.socket = socket;
        this.gameRoom = gameRoom;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String input;

            while ((input = in.readLine()) != null) {
                processCommand(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCommand(String input) {
        if (input.startsWith("/answer")) {
            String[] parts = input.split(" ");
            int answerChoice = Integer.parseInt(parts[1]);
            gameRoom.submitAnswer(getClientId(), answerChoice);
        }
        // Handle other commands here
    }

    private String getClientId() {
        // Logic to retrieve the client's unique ID
        return socket.getRemoteSocketAddress().toString();
    }
}