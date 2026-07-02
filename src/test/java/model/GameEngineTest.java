package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.BoardSupport.pits;
import static testutil.BoardSupport.seed;

/**
 * Tier 2 tests for {@link GameEngine} — verifies the {@link MoveResult} it reports and that
 * an invalid move is a no-op on the board.
 *
 * <p>Note on {@link MoveResult#getLastPitIndex()}: the engine populates that field with the
 * origin pit the caller chose (not the landing pit), so the tests assert against the chosen
 * pit accordingly.
 */
class GameEngineTest {

    private static final int P1 = 0;
    private static final int P2 = 1;

    @Test
    @DisplayName("a valid capturing move reports success, the captured count, and the chosen pit")
    void validCaptureIsReported() {
        Board board = new Board();
        seed(board, 1, 1, 4, 0, 0, 0, 4, 4, 4, 4, 4, 4); // move from pit 0 lands in pit 1 -> 2
        GameEngine engine = new GameEngine(board);

        MoveResult result = engine.playMove(P1, 0);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getCapturedSeeds());
        assertEquals(0, result.getLastPitIndex(), "engine reports the chosen origin pit");
        assertFalse(result.isGameOver(), "both sides still have seeds (pit 2 keeps P1 in play)");
    }

    @Test
    @DisplayName("the captured count reflects a 4-capture")
    void reportsFourCapture() {
        Board board = new Board();
        seed(board, 1, 3, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4); // pit 1 -> 4
        GameEngine engine = new GameEngine(board);

        assertEquals(4, engine.playMove(P1, 0).getCapturedSeeds());
    }

    @Test
    @DisplayName("a non-capturing move reports success with zero captured")
    void nonCapturingMoveReportsZero() {
        Board board = new Board(); // all pits = 4
        GameEngine engine = new GameEngine(board);

        MoveResult result = engine.playMove(P1, 0); // spreads to pits 1..4, no capture

        assertTrue(result.isSuccess());
        assertEquals(0, result.getCapturedSeeds());
    }

    @Test
    @DisplayName("an invalid move reports failure and leaves the board untouched")
    void invalidMoveIsANoOp() {
        Board board = new Board();
        int[] before = pits(board);
        GameEngine engine = new GameEngine(board);

        MoveResult result = engine.playMove(P1, 6); // pit 6 belongs to P2

        assertFalse(result.isSuccess());
        assertEquals(0, result.getCapturedSeeds());
        assertEquals(6, result.getLastPitIndex());
        assertArrayEquals(before, pits(board), "board is unchanged after an invalid move");
        assertArrayEquals(new int[]{0, 0}, board.getScores());
    }

    @Test
    @DisplayName("the game-over flag is set when a move empties a side")
    void gameOverFlagPropagates() {
        Board board = new Board();
        seed(board, 0, 0, 0, 0, 0, 1, 4, 4, 4, 4, 4, 4); // P1's only seed is in pit 5
        GameEngine engine = new GameEngine(board);

        MoveResult result = engine.playMove(P1, 5); // last seed lands in pit 6 -> P1 side empty

        assertTrue(result.isSuccess());
        assertEquals(0, result.getCapturedSeeds(), "landed on opponent's side, no capture");
        assertTrue(result.isGameOver(), "P1's side is now empty");
    }

    @Test
    @DisplayName("getBoard returns the same board instance it was constructed with")
    void exposesItsBoard() {
        Board board = new Board();
        assertSame(board, new GameEngine(board).getBoard());
    }
}
