package Project.Client;

import Project.Client.Interfaces.IClientConnectionEvents;
import Project.Client.Interfaces.IMessageEvents;
import Project.Client.Views.ConnectionPanel;
import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Client.Views.ChatPanel;

import javax.swing.*;
import java.awt.*;

public class ClientUI extends JFrame implements IClientConnectionEvents, IMessageEvents {
    private CardLayout layout;
    private JPanel mainPanel;
    private ChatPanel chatPanel;

    public ClientUI() {
        setTitle("Chat Application");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        layout = new CardLayout();
        mainPanel = new JPanel(layout);

        // Add Connection Panel - Rev/11-23-2024
        ConnectionPanel connectionPanel = new ConnectionPanel(this);
        mainPanel.add(connectionPanel, "connection");

        // Add Chat Panel - Rev/11-23-2024
        chatPanel = new ChatPanel(this);
        mainPanel.add(chatPanel, "chat");

        add(mainPanel);
        layout.show(mainPanel, "connection");
    }

    @Override
    public void onConnectAttempt(String username, String host, int port) {
        // TODO: Add networking logic for connection
        // On success:
        layout.show(mainPanel, "chat");
    }

    @Override // - Rev/11-23-2024
    public void onMessageSend(String message) {
        if (message.startsWith("/mute ") || message.startsWith("/unmute ")) {
            processMuteCommand(message);
        } else if (message.startsWith("@")) {
            // Extract target username and message
            int spaceIndex = message.indexOf(" ");
            if (spaceIndex > 1) {
                String target = message.substring(1, spaceIndex);
                String privateMessage = message.substring(spaceIndex + 1).trim();

                if (!privateMessage.isEmpty()) {
                    // Send private message to the server
                    sendPrivateMessage(target, privateMessage);
                } else {
                    chatPanel.appendMessage("[Error] Private message cannot be empty.");
                }
            } else {
                chatPanel.appendMessage("[Error] Invalid private message format. Use @username message.");
            }
        } else {
            // Regular message
            sendPublicMessage(message);
        }
    }

    private void sendPrivateMessage(String target, String message) {
        Payload payload = new Payload(PayloadType.PRIVATE_MESSAGE);
        payload.setTarget(target);
        payload.setMessage(message);
        sendToServer(payload); // Replace this with actual networking logic - Rev/11-23-2024
        chatPanel.appendMessage("[Private to " + target + "]: " + message);
    }

    private void sendPublicMessage(String message) {
        Payload payload = new Payload(PayloadType.MESSAGE);
        payload.setMessage(message);
        sendToServer(payload); // Replace this with actual networking logic - Rev/11-23-2024
        chatPanel.appendMessage("You: " + message);
    }

    private void processMuteCommand(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length == 2) {
            String action = parts[0];
            String target = parts[1];

            if (!target.trim().isEmpty()) {
                PayloadType type = action.equalsIgnoreCase("/mute") ? PayloadType.MUTE : PayloadType.UNMUTE;
                sendMuteCommand(target, type);
            } else {
                chatPanel.appendMessage("[Error] Username cannot be empty for mute/unmute.");
            }
        } else {
            chatPanel.appendMessage("[Error] Invalid command. Use /mute username or /unmute username.");
        }
    }

    private void sendMuteCommand(String target, PayloadType type) {
        Payload payload = new Payload(type);
        payload.setTarget(target);
        sendToServer(payload); // Replace this with actual networking logic
        String action = type == PayloadType.MUTE ? "muted" : "unmuted";
        chatPanel.appendMessage("You " + action + " " + target + ".");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
    }
}
