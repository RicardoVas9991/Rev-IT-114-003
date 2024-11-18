// rev / 11-04-2024
package Project.Common;

import java.util.List;

public class QAPayload extends Payload {
    private String questionText;
    private List<String> answerOptions;

    public QAPayload(String clientId, String questionText, List<String> answerOptions) {
        super(clientId, questionText, "QAPayload");
        this.questionText = questionText;
        this.answerOptions = answerOptions;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }
}