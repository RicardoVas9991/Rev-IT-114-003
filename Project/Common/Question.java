// rev / 11-04-2024
package Project.Common;

import java.util.List;

public class Question {
    private String questionText;
    private String category;
    private List<String> options;
    private int correctOption;

    public Question(String questionText, String category, List<String> options, int correctOption) {
        this.questionText = questionText;
        this.category = category;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOption() {
        return correctOption;
    }
}