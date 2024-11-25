package Project.Client.Views;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;

import javax.swing.border.EmptyBorder;

import Project.Client.CardView;
import Project.Client.Interfaces.ICardControls;

/**
 * ConnectionPanel is a JPanel that allows the user to input the host and port
 * for a connection. It uses a BorderLayout with a BoxLayout for the content
 * panel. It validates the port input and displays error messages if necessary.
 */
public class ConnectionPanel extends JPanel {   // Rev/11-23-2024 - Show the code related to the ConnectionPanel
    // private JTextField usernameField;
    // private JTextField hostField;
    // private JTextField portField;
    // private JButton connectButton;
    private String host;
    private int port;

    public ConnectionPanel(ICardControls controls) {
        
        super(new BorderLayout(10, 10)); // Set BorderLayout with gaps

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // Add host input field
        JLabel hostLabel = new JLabel("Host:");
        JTextField hostValue = new JTextField("127.0.0.1");
        hostValue.setToolTipText("Enter the host address"); // Add tooltip
        JLabel hostError = new JLabel();
        hostError.setVisible(false); // Initially hide the error label
        content.add(hostLabel);
        content.add(hostValue);
        content.add(hostError);

        // Add port input field
        JLabel portLabel = new JLabel("Port:");
        JTextField portValue = new JTextField("3000");
        portValue.setToolTipText("Enter the port number"); // Add tooltip
        JLabel portError = new JLabel();
        portError.setVisible(false); // Initially hide the error label
        content.add(portLabel);
        content.add(portValue);
        content.add(portError);

        // Add Next button
        JButton button = new JButton("Next");
        button.setAlignmentX(JButton.CENTER_ALIGNMENT); // Center the button
        button.addActionListener(_ -> {
            SwingUtilities.invokeLater(() -> {
                boolean isValid = true;
                try {
                    port = Integer.parseInt(portValue.getText());
                    portError.setVisible(false); // Hide error label if valid
                } catch (NumberFormatException e) {
                    portError.setText("Invalid port value, must be a number");
                    portError.setVisible(true); // Show error label if invalid
                    isValid = false;
                }
                if (isValid) {
                    host = hostValue.getText();
                    controls.next(); // Navigate to the next card
                }
            });
        });
        content.add(Box.createVerticalStrut(10)); // Add vertical spacing
        content.add(button);

        // Add the content panel to the center of the BorderLayout
        this.add(content, BorderLayout.CENTER);
        this.setName(CardView.CONNECT.name()); // Set the name of the panel
        controls.addPanel(CardView.CONNECT.name(), this); // Add panel to controls

    //     Rev/11-23-2024
    //     setLayout(new GridLayout(4, 2));

    //     add(new JLabel("Username:"));
    //     usernameField = new JTextField();
    //     add(usernameField);

    //     add(new JLabel("Host:"));
    //     hostField = new JTextField();
    //     add(hostField);

    //     add(new JLabel("Port:"));
    //     portField = new JTextField();
    //     add(portField);

    //     connectButton = new JButton("Connect");
    //     add(connectButton);

    //     connectButton.addActionListener(new ActionListener() {
    //         @Override
    //         public void actionPerformed(ActionEvent e) {
    //         }
    //     });
    
}


     /**
     * Gets the host value entered by the user.
     * 
     * @return the host value.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port value entered by the user.
     * 
     * @return the port value.
     */
    public int getPort() {
        return port;
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("Connect to Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ConnectionPanel(null));
        frame.pack();
        frame.setVisible(true);
    }

}
