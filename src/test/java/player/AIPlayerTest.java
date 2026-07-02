package player;

import controller.GameController;
import model.Board;
import model.GameEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutil.BoardSupport.seed;

/**
 * Tier 3 tests for {@link AIPlayer} — verifies the greedy one-move heuristic, i.e. that
 * {@code chooseMove} selects the valid move maximizing immediate capture. This exercises the
 * decision logic deterministically; it does not assert anything about thread-pool timing.
 */
class AIPlayerTest {

    private static final int P1 = 0;

    private AIPlayer ai;

    @AfterEach
    void tearDown() {
        if (ai != null) ai.shutdown();
    }

    /** Builds an AI player driving a controller over a board seeded with the given pits. */
    private AIPlayer aiOnBoard(int playerId, int... pits) {
        Board board = new Board();
        seed(board, pits);
        ai = new AIPlayer(playerId, new GameController(new GameEngine(board)));
        return ai;
    }

    @Test
    @DisplayName("chooses the move that captures the most seeds")
    void picksHighestCapture() {
        // pit 0 -> pit 1 (=2) captures 2; pit 1 and pit 2 capture nothing
        AIPlayer ai = aiOnBoard(P1, 1, 1, 4, 0, 0, 0, 4, 4, 4, 4, 4, 4);
        assertEquals(0, ai.chooseMove());
    }

    @Test
    @DisplayName("prefers a capturing move even when it is not the first valid one")
    void prefersCaptureOverEarlierMove() {
        // valid moves are 0, 2, 3; only move 2 captures (pit 3 -> 2), so it must win over pit 0
        AIPlayer ai = aiOnBoard(P1, 1, 0, 1, 1, 0, 0, 4, 4, 4, 4, 4, 4);
        assertEquals(2, ai.chooseMove());
    }

    @Test
    @DisplayName("returns -1 when there are no valid moves")
    void noValidMovesReturnsMinusOne() {
        AIPlayer ai = aiOnBoard(P1, 0, 0, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4);
        assertEquals(-1, ai.chooseMove());
    }
}