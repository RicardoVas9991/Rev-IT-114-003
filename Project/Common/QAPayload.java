// rev/11-02-2024

package Project.Common;

import java.util.List;

public class QAPayload extends Payload {
    private String question;
    private List<String> answerOptions;

    public QAPayload(String question, List<String> answerOptions) {
        setPayloadType(PayloadType.QUESTION);
        this.question = question;
        this.answerOptions = answerOptions;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }
}
