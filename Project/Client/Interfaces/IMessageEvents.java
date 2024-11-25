package Project.Client.Interfaces;

/**
 * Interface for handling message events.
 */
public interface IMessageEvents {
    void onMessageSend(String message); // Rev/11-23-2024

    /**
     * Triggered when a message is received.
     *
     * @param id      The client ID.
     * @param message The message.
     */
    void onMessageReceive(long clientId, String message); // Rev/11-23-2024
}
