package player;

import controller.GameController;
import model.Board;
import util.GameLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AIPlayer extends Player {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    public AIPlayer(int playerId, GameController controller) { super(playerId, controller); }

    @Override
    protected int chooseMove() {
        Board snapshot = controller.getBoardSnapshot();
        List<Integer> validMoves = new ArrayList<>();
        for (int i = 0; i < 12; i++) if (snapshot.isValidMove(playerId, i)) validMoves.add(i);
        if (validMoves.isEmpty()) return -1;

        List<Future<Integer>> futures = new ArrayList<>();
        for (int move : validMoves) {
            futures.add(pool.submit(() -> simulateMove(snapshot, move)));
        }

        int bestMove = validMoves.get(0);
        int maxScore = -1;
        try {
            for (int i = 0; i < futures.size(); i++) {
                int score = futures.get(i).get();
                if (score > maxScore) { maxScore = score; bestMove = validMoves.get(i); }
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }

        GameLogger.log("AI Player " + (playerId + 1) + " chooses pit " + bestMove);
        return bestMove;
    }

    private int simulateMove(Board snapshot, int move) {
        Board copy = snapshot.clone();
        copy.applyMove(playerId, move);
        return copy.getScores()[playerId];
    }

    public void shutdown() { pool.shutdown(); }
}
