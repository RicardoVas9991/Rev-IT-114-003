package Project.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.PointsPayload;
import Project.Common.QAPayload;
import Project.Common.TimedEvent;

public class GameRoom extends BaseGameRoom {
    
    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;

    private List<Question> questions = new ArrayList<>(); // ucid/date (rev/11-02-2024): List to store questions
    private Map<ServerPlayer, Integer> playerScores = new HashMap<>(); // ucid/date (rev/11-02-2024): Map to track scores
    private Question currentQuestion;
    
    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerPlayer sp){
        // sync GameRoom state to new client
        syncCurrentPhase(sp);
        syncReadyStatus(sp);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerPlayer sp){
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + playersInRoom.size());
        if(playersInRoom.isEmpty()){
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        }
    }

    // timer handlers
    private void startRoundTimer(){
        roundTimer = new TimedEvent(30, ()-> onRoundEnd());
        roundTimer.setTickCallback((time)->System.out.println("Round Time: " + time));
    }
    private void resetRoundTimer(){
        if(roundTimer != null){
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private void startTurnTimer(){
        turnTimer = new TimedEvent(30, ()-> onTurnEnd());
        turnTimer.setTickCallback((time)->System.out.println("Turn Time: " + time));
    }
    private void resetTurnTimer(){
        if(turnTimer != null){
            turnTimer.cancel();
            turnTimer = null;
        }
    }
    // end timer handlers
    
    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart(){
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        loadQuestionsFromFile(); // (rev/11-02-2024): Load questions at session start
        startNewRound();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart(){
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        startRoundTimer();
        LoggerUtil.INSTANCE.info("onRoundStart() end");
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart(){
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }
    // Note: logic between Turn Start and Turn End is typically handled via timers and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd(){
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring

        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }
    // Note: logic between Round Start and Round End is typically handled via timers and user interaction
    private void loadQuestionsFromFile() {
        // ucid/date (rev/11-02-2024): Sample questions added. Replace with file reading logic.
        questions.add(new Question("What is 2 + 2?", Arrays.asList("A. 3", "B. 4", "C. 5", "D. 6"), "B"));
        questions.add(new Question("Capital of France?", Arrays.asList("A. Berlin", "B. Madrid", "C. Paris", "D. Rome"), "C"));
        LoggerUtil.INSTANCE.info("Questions loaded.");
    }

    private void startNewRound() {
        if (questions.isEmpty()) {
            onSessionEnd(); // End the session if no more questions
            return;
        }

        currentQuestion = questions.remove(new Random().nextInt(questions.size()));
        broadcastQuestion(currentQuestion);
        startRoundTimer(); // ucid/date (rev/11-02-2024): Timer for the round start
    }

    private void broadcastQuestion(Question question) {
        QAPayload payload = new QAPayload(question.getText(), question.getOptions()); // Use the new QAPayload
        playersInRoom.values().forEach(player -> player.getServerThread().send(payload));
    }

    public void handleAnswer(ServerThread st, String answer) {
        LoggerUtil.INSTANCE.info("Answer received from " + st.getClientName() + ": " + answer);
        ServerPlayer player = playersInRoom.get(st.getClientId());

        if (player != null && answer.equals(currentQuestion.getCorrectAnswer())) {
            playerScores.put(player, playerScores.getOrDefault(player, 0) + 10); // ucid/date (rev/11-02-2024): Score logic
            st.sendMessage("Correct answer! 10 points awarded.");
            checkRoundEndCondition();
        } else {
            st.sendMessage("Incorrect answer.");
        }
    }

    private void checkRoundEndCondition() {
        // ucid/date (rev/11-02-2024): Check if round should end
        if (allPlayersAnswered()) {
            onRoundEnd();
        }
    }

    private boolean allPlayersAnswered() {
        return playerScores.size() == playersInRoom.size();
    }


    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer();

    // Check condition to end the session; otherwise, move to the next round
    if (/* condition to check if the session should end */) {
        onSessionEnd();
    } else {
        PointsPayload pointsPayload = new PointsPayload(getPlayerScores());
        playersInRoom.values().forEach(player -> player.getServerThread().send(pointsPayload));
        startNewRound(); // Start the next round if the session continues
    }

    LoggerUtil.INSTANCE.info("onRoundEnd() end");
}

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd(){
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        resetReadyStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    private Map<String, Integer> getPlayerScores() {
        Map<String, Integer> scores = new HashMap<>();
        playerScores.forEach((player, points) -> scores.put(player.getClientId(), points));
        return scores; // re
    }

    // send/sync data to ServerPlayer(s)

    
    // end send data to ServerPlayer(s)

    // receive data from ServerThread (GameRoom specific)
    
 
   // end receive data from ServerThread (GameRoom specific)
}