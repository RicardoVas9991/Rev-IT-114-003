// rev / 11-04-2024
package Project.Common;

import java.util.List;

public class Question {
    private String text;
    private String category;
    private List<String> answers;
    private String correctAnswer;

    public Question(String text, String category, List<String> answers, String correctAnswer) {
        this.text = text;
        this.category = category;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
    }

    public String getText() {
        return text;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
