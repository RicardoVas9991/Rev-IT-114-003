package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import Project.Client.CardView;
import Project.Client.Client;
import Project.Client.Interfaces.ICardControls;
import Project.Common.LoggerUtil;

/**
 * ChatPanel represents the main chat interface where messages can be sent and
 * received.
 */
public class ChatPanel extends JPanel {
    private JPanel chatArea = null;
    private UserListPanel userListPanel;
    private final float CHAT_SPLIT_PERCENT = 0.7f;
    private JTextArea chatHistory = new JTextArea(); // 

    /**
     * Constructor to create the ChatPanel UI.
     * 
     * @param controls The controls to manage card transitions.
     */
    public ChatPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));

        JPanel chatContent = new JPanel(new GridBagLayout());
        chatContent.setAlignmentY(Component.TOP_ALIGNMENT);

        // Wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(chatContent);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        chatArea = chatContent;

        userListPanel = new UserListPanel();

        // JSplitPane setup with chat on the left and user list on the right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, userListPanel);
        splitPane.setResizeWeight(CHAT_SPLIT_PERCENT); // Allocate % space to the chat panel initially

        // Enforce splitPane split
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(CHAT_SPLIT_PERCENT));
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(CHAT_SPLIT_PERCENT));
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        JPanel input = new JPanel();
        input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
        input.setBorder(new EmptyBorder(5, 5, 5, 5)); // Add padding

        JTextField textValue = new JTextField();
        input.add(textValue);

        JButton button = new JButton("Send");
        // Allows submission with the enter key instead of just the button click
        textValue.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    button.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        button.addActionListener(_ -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String text = textValue.getText().trim();
                    if (!text.isEmpty()) {
                        Client.INSTANCE.sendMessage(text);
                        textValue.setText(""); // Clear the original text
                    }
                } catch (NullPointerException | IOException e) {
                    LoggerUtil.INSTANCE.severe("Error sending message", e);
                }
            });
        });

        input.add(button);

        this.add(splitPane, BorderLayout.CENTER);
        this.add(input, BorderLayout.SOUTH);

        this.setName(CardView.CHAT.name());
        controls.addPanel(CardView.CHAT.name(), this);

        chatArea.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (chatArea.isVisible()) {
                        chatArea.revalidate();
                        chatArea.repaint();
                    }
                });
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (chatArea.isVisible()) {
                        chatArea.revalidate();
                        chatArea.repaint();
                    }
                });
            }
        });

        // Add vertical glue to push messages to the top
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // Column index 0
        gbc.gridy = GridBagConstraints.RELATIVE; // Automatically move to the next row
        gbc.weighty = 1.0; // Give extra space vertically to this component
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        chatArea.add(Box.createVerticalGlue(), gbc);
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chatroom");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChatPanel(null));
        frame.pack();
        frame.setVisible(true);
    }


    public void handleSpecialCommands(String message) {
        if (message.startsWith("/flip")) {
            // Handle flip command
            String result = Math.random() < 0.5 ? "heads" : "tails";
            chatHistory.append("You flipped a coin and got " + result + "\n");
        } if (message.startsWith("/roll")) {
            // Handle roll command
            String[] parts = message.split(" ");
            if (parts.length == 2) {
                String[] rollParts = parts[1].split("d");
                if (rollParts.length == 2) {
                    int rolls = Integer.parseInt(rollParts[0]);
                    int sides = Integer.parseInt(rollParts[1]);
                    StringBuilder result = new StringBuilder("You rolled " + rolls + "d" + sides + ": ");
                    for (int i = 0; i < rolls; i++) {
                        result.append((int) (Math.random() * sides) + 1).append(" ");
                    }
                    chatHistory.append(result.toString().trim() + "\n");
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

    // Rev/11-23-2024 -  Show the client-side code that processes the text per the requirement
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
    

    /**
     * Adds a message to the chat area.
     * 
     * @param text The text of the message.
     */
    public void addText(String message) {
        SwingUtilities.invokeLater(() -> {
            JEditorPane textContainer = new JEditorPane("text/html", message);
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
}