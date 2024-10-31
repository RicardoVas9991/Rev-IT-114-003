// rev/11-02-2024

package Project.Server;

import Project.Common.Grid;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.Player;
import Project.Common.TimedEvent;
import java.util.HashMap;
import java.util.Map;

public class GameRoom extends BaseGameRoom {
    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;

    private Grid grid = null;
    
    private Map<ServerPlayer, Integer> playerPoints = new HashMap<>();
    private String currentQuestion;
    private String correctAnswer;
    
    public GameRoom(String name) {
        super(name);
    }
    
    private void addPoints(ServerPlayer player, int points) {
        playerPoints.put(player, playerPoints.getOrDefault(player, 0) + points);
    }

    private int getPoints(ServerPlayer player) {
        return playerPoints.getOrDefault(player, 0);
    }


    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerPlayer sp) {
        // sync GameRoom state to new client
        syncCurrentPhase(sp);
        syncReadyStatus(sp);
        if (currentPhase != Phase.READY) {
            syncGridDimensions(sp);
        }
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
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> System.out.println("Round Time: " + time));
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private void startTurnTimer(){
        turnTimer = new TimedEvent(30, ()-> onTurnEnd());
        turnTimer.setTickCallback((time)->System.out.println("Turn Time: " + time));
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        grid = new Grid(2, 2);
        sendGridDimensions();
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        startRoundTimer();
        selectQuestion();  // Select and broadcast a new question at the start of each round
        LoggerUtil.INSTANCE.info("onRoundStart() end");
    }

    private void selectQuestion() {
        currentQuestion = "What is 2 + 2?";
        correctAnswer = "4";  // Example; replace with dynamic question selection
        sendMessage(null, "New Question: " + currentQuestion);
    }


    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring

        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer(); // reset timer if round ended without the time expiring

        LoggerUtil.INSTANCE.info("onRoundEnd() end");
        // example of some end session condition 2
        sendMessage(null, "Too slow populating the grid, you all lose");
        onSessionEnd();
    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        StringBuilder results = new StringBuilder("Game Over! Final Scores:\n");
        playerPoints.forEach((player, points) -> results.append(player.getClientId()).append(": ").append(points).append(" points\n"));
        sendMessage(null, results.toString());
        
        grid.reset();
        resetRoundTimer(); // just in case it's still active if we forgot to end it sooner
        sendGridDimensions();
        sendResetTurnStatus();
        resetReadyStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // misc logic
    private void checkIfAllTookTurns() {
        long ready = playersInRoom.values().stream().filter(p -> p.isReady()).count();
        long tookTurn = playersInRoom.values().stream().filter(p -> p.isReady() && p.didTakeTurn()).count();
        if (ready == tookTurn) {
            // example of some end session condition 2
            if (grid.areAllCellsOccupied()) {
                sendMessage(null, "Congrats, you filled the grid");
                onSessionEnd();
            } else {
                sendResetTurnStatus();
                onRoundStart();
                sendMessage(null, "Move again");
            }

        }
    }
    // end misc logic

    // send/sync data to ServerPlayer(s)

    /**
     * Sends a movement coordinate of one Player to all Players (including
     * themselves)
     * 
     * @param sp
     * @param x
     * @param y
     */
    private void sendMove(ServerPlayer sp, int x, int y) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendMove(sp.getClientId(), x, y);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    /**
     * A shorthand way of telling all clients to reset their local list's turn
     * status
     */
    private void sendResetTurnStatus() {
        playersInRoom.values().removeIf(spInRoom -> {
            spInRoom.setTakeTurn(false); // reset server data
            // using DEFAULT_CLIENT_ID as a trigger, prevents needing a nested loop to
            // update the status of each player to each player
            boolean failedToSend = !spInRoom.sendTurnStatus(Player.DEFAULT_CLIENT_ID, false);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    /**
     * Sends the turn status of one Player to all Players (including themselves)
     * 
     * @param sp
     */
    private void sendTurnStatus(ServerPlayer sp) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendTurnStatus(sp.getClientId(), sp.didTakeTurn());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    private void syncGridDimensions(ServerPlayer sp) {
        sp.sendGridDimensions(grid.getRows(), grid.getCols());
    }

    private void sendGridDimensions() {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendGridDimensions(grid.getRows(), grid.getCols());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    // end send data to ServerPlayer(s)

    // receive data from ServerThread (GameRoom specific)
    protected void handleMove(ServerThread st, int x, int y) {
        try {
            checkPlayerInRoom(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            if(!sp.isReady()){
                st.sendMessage("You weren't ready in time");
                return;
            }
            if (sp.didTakeTurn()) {
                st.sendMessage("You already took your turn");
                return;
            }
            if (grid.getCell(x, y).isOccupied()) {
                st.sendMessage("This cell is already occupied");
                return;
            }
            grid.setCell(x, y, true);
            sendMove(sp, x, y);
            sp.setTakeTurn(true);
            sendTurnStatus(sp);
            checkIfAllTookTurns();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // end receive data from ServerThread (GameRoom specific)

    // Method to handle answer processing
    public void handleAnswer(ServerThread st, String answer) {
        LoggerUtil.INSTANCE.info("Answer received from " + st.getClientName() + ": " + answer);
        
        if (isCorrectAnswer(answer)) { // ucid/date (rev/11-02-2024): Implemented answer checking
            st.sendMessage("Correct answer!");
            updatePlayerScore(st.getClientId(), 10); // ucid/date (rev/11-02-2024): Awarded points for correct answer
        } else {
            st.sendMessage("Incorrect answer. Try again.");
        }
    }

    // Helper method to update player score
    private void updatePlayerScore(long clientId, int points) { 
        ServerPlayer sp = playersInRoom.get(clientId);
        if (sp != null) {
            int newScore = sp.getScore() + points;
            sp.setScore(newScore); // ucid/date (rev/11-02-2024): Updated score
            broadcastScoreUpdate(sp); // Notify players of the score change
        }
    }

    // Notify all players about the updated score
    private void broadcastScoreUpdate(ServerPlayer sp) {
        playersInRoom.values().forEach(player -> player.sendMessage(
                sp.getClientId(), "Player " + sp.getClientId() + " has a new score: " + sp.getScore()));
    }

    // Method stub for answer checking (to be implemented)
    private boolean isCorrectAnswer(String answer) {
        // Implement logic here for validating the answer
        return true; // Placeholder
    }
    
}