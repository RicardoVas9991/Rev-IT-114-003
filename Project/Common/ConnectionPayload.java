package Project.Common;

public class ConnectionPayload extends Payload {
    private String clientName;
    private boolean isConnect;

    public ConnectionPayload() {
        setPayloadType(PayloadType.CLIENT_CONNECT);  // Call to super constructor - rev/11-14-2024
    }
    
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean isConnect) {
        this.isConnect = isConnect;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" Client Name [%s] Status [%s]", clientName, isConnect ? "connect" : "disconnect");
    }
}