package Project.Client;

public class ClientData {
    public static long DEFAULT_CLIENT_ID = -1L;
    private long clientId = ClientData.DEFAULT_CLIENT_ID;
    private String clientName;
    public long getClientId() {
        return clientId;
    }
    public void setClientId(long clientId2) {
        this.clientId = clientId2;
    }
    public String getClientName() {
        return clientName;
    }
    public void setClientName(String clientName2) {
        this.clientName = clientName2;
    }

    public void reset(){
        this.clientId = ClientData.DEFAULT_CLIENT_ID;
        this.clientName = "";
    }
}