package Project.Server;

import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.TimedEvent;
import Project.Common.Payload;
import Project.Common.QAPayload;
import Project.Common.PointsPayload;
import Project.Common.Question;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameRoom extends BaseGameRoom {
    
    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;

    // List to hold questions
    private List<Question> questions;

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
        loadQuestions("questions.txt"); // Load questions from file
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart(){
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        startRoundTimer();
        sendQuestionToClients(); // Send question to clients
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
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd(){
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer(); // reset timer if round ended without the time expiring
        awardPoints(); // Award points to players
        LoggerUtil.INSTANCE.info("onRoundEnd() end");
        onSessionEnd();
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

    // Load questions from file
    private void loadQuestions(String filePath) {
        // rev / 11-04-2024
        // Load questions from file into memory
    }

    // Send question to clients
    private void sendQuestionToClients() {
        // rev / 11-04-2024
        if (questions != null && !questions.isEmpty()) {
            Random random = new Random();
            Question question = questions.remove(random.nextInt(questions.size()));
            QAPayload payload = new QAPayload(question.getText(), question.getAnswers());
            broadcast(payload);
        }
    }

    // Award points to players
    private void awardPoints() {
        // rev / 11-04-2024
        // Award points based on the order of correct answers
        PointsPayload payload = new PointsPayload(calculatePoints());
        broadcast(payload);
    }

    // Calculate points for players
    private Map<String, Integer> calculatePoints() {
        // rev / 11-04-2024
        // Calculate and return points for players
        return null;
    }

    // Broadcast payload to all clients
    private void broadcast(Payload payload) {
        // rev / 11-04-2024
        // Implement the method to send payload to all clients
    }
}
