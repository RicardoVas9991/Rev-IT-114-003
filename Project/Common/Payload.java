package Project.Common;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Payload implements Serializable {
    private PayloadType payloadType;
    private long clientId;
    private String message;

    // rev / 11-04-2024
    // Additional fields for QAPayload and PointsPayload
    private String question;
    private List<String> answers;
    private Map<String, Integer> points;

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // rev / 11-04-2024
    // Getters and setters for new fields
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public Map<String, Integer> getPoints() {
        return points;
    }

    public void setPoints(Map<String, Integer> points) {
        this.points = points;
    }

    @Override
    public String toString(){
        return String.format("Payload[%s] Client Id [%s] Message: [%s]", getPayloadType(), getClientId(), getMessage());
    }
}
