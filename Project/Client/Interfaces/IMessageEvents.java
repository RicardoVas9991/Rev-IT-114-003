package Project.Client.Interfaces;

public interface IMessageEvents {
    void onMessageSend(String message); // Rev/11-23-2024

    void onMessageReceive(long clientId, String message); // Rev/11-23-2024
}
