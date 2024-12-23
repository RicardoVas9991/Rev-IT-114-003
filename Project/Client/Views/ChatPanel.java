package Project.Client.Views;

import java.awt.BorderLayout;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
    private Set<String> mutedUsers = new HashSet<>();
    private Map<Long, String> connectedUsers = new HashMap<>();
    private String lastSender = null; // rev/12/4/2024 - initialization
    


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
        // Chat History Export (Client-Side) - Milestone4 - rev/12/4/2024
        // Add this button to the chat UI
        JButton exportButton = new JButton("Export Chat");
        exportButton.addActionListener((_) -> exportChatHistory());
        // Add exportButton to the UI (e.g., a panel)
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

        button.addActionListener((_) -> {
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
        input.add(exportButton);

        this.add(splitPane, BorderLayout.CENTER);
        this.add(input, BorderLayout.SOUTH);

        this.setName(CardView.CHAT.name());
        controls.addPanel(CardView.CHAT.name(), this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(button);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Add spacing
        buttonPanel.add(exportButton);

        input.add(buttonPanel);

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

    /**
     * Adds a message to the chat area.
     * 
     * @param text The text of the message.
     */
    public void addText(String text) {
        SwingUtilities.invokeLater(() -> {
            JEditorPane textContainer = new JEditorPane("text/plain", text);
            textContainer.setEditable(false);
            textContainer.setOpaque(false);
            textContainer.setBorder(BorderFactory.createEmptyBorder());
    
            JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatArea);
            int availableWidth = chatArea.getWidth() - (parentScrollPane != null
                    ? parentScrollPane.getVerticalScrollBar().getPreferredSize().width : 0) - 10;
    
            textContainer.setSize(new Dimension(availableWidth, Integer.MAX_VALUE));
            textContainer.setPreferredSize(new Dimension(availableWidth, textContainer.getPreferredSize().height));
    
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 5, 0);
    
            chatArea.add(textContainer, gbc);
            chatArea.revalidate();
            chatArea.repaint();
    
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatArea);
            if (scrollPane != null) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMaximum());
            }
        });
    }

    // Method to export chat history
    private void exportChatHistory() {
        // rev/12/4/2024
        try {
            StringBuilder chatHistory = new StringBuilder();
            for (Component comp : chatArea.getComponents()) {
                if (comp instanceof JEditorPane) {
                    JEditorPane pane = (JEditorPane) comp;
                    chatHistory.append(pane.getText()).append("\n");
                }
            }
            String filename = "chat_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
            Files.write(Paths.get(filename), chatHistory.toString().getBytes());
            JOptionPane.showMessageDialog(this, "Chat exported to " + filename, "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export chat: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }    

    private void updateUserListUI() {
        SwingUtilities.invokeLater(() -> {
            Set<String> userNames = new HashSet<>(connectedUsers.values());
            userListPanel.updateUserList(userNames, lastSender, mutedUsers);
        });
    }
    
    /**
     * Handles an incoming message from a server or another client.
     * This method is invoked externally by the message handling system.
     * 
     * @param sender  The sender's name.
     * @param message The message content.
     */
    public void handleIncomingMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            lastSender = sender;
            addText(sender + ": " + message); // Add message to chat
            updateUserListUI(); // Refresh the user list
        });
    } 


}