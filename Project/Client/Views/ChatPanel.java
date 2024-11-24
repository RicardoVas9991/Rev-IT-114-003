package Project.Client.Views;

import javax.swing.*;

import Project.Client.Interfaces.IMessageEvents;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private IMessageEvents messageCallback;

    public ChatPanel(IMessageEvents messageCallback) {
        this.messageCallback = messageCallback;
        setLayout(new BorderLayout(10, 10));

        // Chat History
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        // User List
        userList = new JList<>();
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(150, 0));

        // Message Input
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendMessage();
            }
        });

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendMessage();
            }
        });

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Add Components
        add(chatScroll, BorderLayout.CENTER);
        add(userScroll, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            messageCallback.onMessageSend(message);
            messageField.setText(""); // Clear the input field
        }
    }

    public void appendMessage(String message, boolean isPrivate) {
        if (isPrivate) {
            chatArea.append("[PRIVATE] " + message + "\n");
        } else {
            chatArea.append(message + "\n");
        }
    }


    public void updateUsers(String[] users) {
        userList.setListData(users);
    }
}
