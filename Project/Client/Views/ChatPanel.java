package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatPanel extends JPanel { // Rev/11-23-2024 - Show the code related to the ChatPanel
    private JList<String> userList;
    private JTextArea chatHistory;
    private JTextField messageField;
    private JButton sendButton;
    private JPanel chatArea = null;
    private UserListPanel userListPanel;
    /**
     * Constructor to create the ChatPanel UI.
     * 
     * @param controls The controls to manage card transitions.
     */

    public ChatPanel() {
        setLayout(new BorderLayout());

        userList = new JList<>();
        add(new JScrollPane(userList), BorderLayout.WEST);

        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        add(new JScrollPane(chatHistory), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        inputPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                if (!message.isEmpty()) {
                    // Add logic to send the message
                    messageField.setText("");
                }
            }
        });

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
            }
        });
    }

    /**
     * Adds a user to the user list.
     * 
     * @param clientId   The ID of the client.
     * @param clientName The name of the client.
     */
    public void addUserListItem(long clientId, String clientName) {
        SwingUtilities.invokeLater(() -> userListPanel.addUserListItem(clientId, clientName));
    }

    /**
     * Removes a user from the user list.
     * 
     * @param clientId The ID of the client to be removed.
     */
    public void removeUserListItem(long clientId) {
        SwingUtilities.invokeLater(() -> userListPanel.removeUserListItem(clientId));
    }

    /**
     * Clears the user list.
     */
    public void clearUserList() {
        SwingUtilities.invokeLater(() -> userListPanel.clearUserList());
    }

    /**
     * Adds a message to the chat area.
     * 
     * @param text The text of the message.
     */
    public void addText(String text) {
        SwingUtilities.invokeLater(() -> {
            JEditorPane textContainer = new JEditorPane("text/plain", text);
            textContainer.setEditable(false);
            textContainer.setBorder(BorderFactory.createEmptyBorder());

            // Account for the width of the vertical scrollbar
            JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatArea);
            int scrollBarWidth = parentScrollPane.getVerticalScrollBar().getPreferredSize().width;

            // Adjust the width of the text container
            int availableWidth = chatArea.getWidth() - scrollBarWidth - 10; // Subtract an additional padding
            textContainer.setSize(new Dimension(availableWidth, Integer.MAX_VALUE));
            Dimension d = textContainer.getPreferredSize();
            textContainer.setPreferredSize(new Dimension(availableWidth, d.height));
            // Remove background and border
            textContainer.setOpaque(false);
            textContainer.setBorder(BorderFactory.createEmptyBorder());
            textContainer.setBackground(new Color(0, 0, 0, 0));

            // GridBagConstraints settings for each message
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; // Column index 0
            gbc.gridy = GridBagConstraints.RELATIVE; // Automatically move to the next row
            gbc.weightx = 1; // Let the component grow horizontally to fill the space
            gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
            gbc.insets = new Insets(0, 0, 5, 0); // Add spacing between messages

            chatArea.add(textContainer, gbc);
            chatArea.revalidate();
            chatArea.repaint();

            // Scroll down on new message
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = parentScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        });
    }

    // Rev/11-23-2024
    public static void main(String[] args) {
        JFrame frame = new JFrame("Chatroom");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChatPanel());
        frame.pack();
        frame.setVisible(true);
    }


    public void handleSpecialCommands(String message) {
        if (message.startsWith("/flip")) {
            // Handle flip command
            String result = Math.random() < 0.5 ? "heads" : "tails";
            chatHistory.append("You flipped a coin and got " + result + "\n");
        } else if (message.startsWith("/roll")) {
            // Handle roll command
            String[] parts = message.split(" ");
            if (parts.length == 2) {
                String[] rollParts = parts[1].split("d");
                if (rollParts.length == 2) {
                    int rolls = Integer.parseInt(rollParts[0]);
                    int sides = Integer.parseInt(rollParts[1]);
                    int result = (int) (Math.random() * sides) + 1;
                    chatHistory.append("You rolled " + rolls + "d" + sides + " and got " + result + "\n");
                }
            }
        } else {
            // Handle regular message
            chatHistory.append(message + "\n");
        }
    }
    // Rev/11-23-2024 - Text Formatting appears correctly on the UI
    public String processTextFormatting(String message) {
        message = message.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        message = message.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        message = message.replaceAll("_(.*?)_", "<u>$1</u>");
        message = message.replaceAll("#r(.*?)r#", "<red>$1</red>");
        message = message.replaceAll("#b(.*?)b#", "<blue>$1</blue>");
        message = message.replaceAll("#g(.*?)g#", "<green>$1</green>");
        return message;
    }
    
    public void sendMessage(String message) {
        if (message.startsWith("@")) {
            String[] parts = message.split(" ", 2);
            if (parts.length == 2) {
            }
        } else {
            // Send regular message
        }
    }

    public void handleMuteUnmute(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 2) {
            if (command.startsWith("/mute")) {
                // Add targetUsername to mute list
            } else if (command.startsWith("/unmute")) {
                // Remove targetUsername from mute list
            }
        }
    }
    

}
