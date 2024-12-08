package Project.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ServerUI extends JFrame {
    private JLabel uptimeLabel;
    private JLabel memoryLabel;
    private JButton stopServerButton;

    private long serverStartTime;

    public ServerUI() {
        // Set up the JFrame
        setTitle("Server UI");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize server start time
        serverStartTime = System.currentTimeMillis();

        // Create a panel for server stats
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(3, 1));

        uptimeLabel = new JLabel("Uptime: Calculating...");
        memoryLabel = new JLabel("Memory Usage: Calculating...");
        statsPanel.add(uptimeLabel);
        statsPanel.add(memoryLabel);

        // Create a stop server button
        stopServerButton = new JButton("Stop Server");
        stopServerButton.addActionListener(this::stopServer);

        statsPanel.add(stopServerButton);

        // Add stats panel to the JFrame
        add(statsPanel, BorderLayout.CENTER);

        // Start the stats update timer
        startStatsUpdater();
    }

    /**
     * Updates the server stats periodically.
     */
    private void startStatsUpdater() {
        Timer timer = new Timer(1000, _ -> updateStats());
        timer.start();
    }

    /**
     * Updates the uptime and memory usage labels.
     */
    private void updateStats() {
        long uptime = System.currentTimeMillis() - serverStartTime;
        uptimeLabel.setText("Uptime: " + (uptime / 1000) + " seconds");

        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        memoryLabel.setText("Memory Usage: " + (memoryUsage / (1024 * 1024)) + " MB");
    }

    /**
     * Stops the server and exits the application.
     *
     * @param e The action event triggered by the stop button.
     */
    private void stopServer(ActionEvent e) {
        // Perform server shutdown tasks
        Server.INSTANCE.shutdown();
        System.exit(0);
    }

    /**
     * Sets the visibility of the server UI.
     *
     * @param visible true to show the UI, false to hide it.
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
}
