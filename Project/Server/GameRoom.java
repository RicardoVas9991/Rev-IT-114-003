public class GameRoom extends BaseGameRoom {
    private List<Question> questionPool;
    private Map<String, Integer> playerPoints = new ConcurrentHashMap<>();
    private List<String> correctPlayers = new ArrayList<>();
    private Question currentQuestion;
    private int roundNumber = 0;

    public GameRoom(List<Question> questions) {
        this.questionPool = new ArrayList<>(questions);
    }

    public void startRound() {
        if (questionPool.isEmpty()) {
            endSession();
            return;
        }
        
        currentQuestion = selectRandomQuestion();
        correctPlayers.clear();
        roundNumber++;

        ConnectionPayload questionPayload = new ConnectionPayload(currentQuestion.getText(), currentQuestion.getOptions());
        broadcastToClients(questionPayload);
        startTimer();
    }

    private Question selectRandomQuestion() {
        int randomIndex = new Random().nextInt(questionPool.size());
        return questionPool.remove(randomIndex);
    }

    public void submitAnswer(String clientId, int answerChoice) {
        if (answerChoice == currentQuestion.getCorrectOption() && !correctPlayers.contains(clientId)) {
            correctPlayers.add(clientId);
            updatePoints(clientId);
        }
    }

    private void updatePoints(String clientId) {
        int basePoints = 10;
        int pointsAwarded = basePoints - (correctPlayers.size() - 1) * 3;
        playerPoints.put(clientId, playerPoints.getOrDefault(clientId, 0) + Math.max(pointsAwarded, 1));
    }

    private void endRound() {
        RoomResultsPayload pointsPayload = new RoomResultsPayload(playerPoints);
        broadcastToClients(pointsPayload);
        startRound();
    }

    private void endSession() {
        System.out.println("Session ended after " + roundNumber + " rounds.");
        RoomResultsPayload finalScoreboard = new RoomResultsPayload(playerPoints);
        broadcastToClients(finalScoreboard);
    }

    private void startTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                endRound();
            }
        }, 20000); // 20 seconds for round timer
    }

    private void broadcastToClients(Object payload) {
        // Logic to send payload to all clients
    }
}