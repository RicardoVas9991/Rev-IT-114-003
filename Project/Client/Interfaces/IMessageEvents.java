package Project.Client.Interfaces;

public interface IMessageEvents {
    void onMessageSend(String message);

    void onMessageReceive(long clientId, String message);
}
