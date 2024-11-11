package Project.Common;

import java.util.List;

public class ConnectionPayload extends Payload {
    private String questionText;
    private List<String> answerOptions;

    public ConnectionPayload(String questionText, List<String> answerOptions) {
        super(PayloadType.QUESTION);
        this.questionText = questionText;
        this.answerOptions = answerOptions;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    public void setClientName(String clientName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setClientName'");
    }
}