package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.BoardSupport.pits;
import static testutil.BoardSupport.seed;

/**
 * Tier 1 tests for {@link Board} — the deterministic core of the game.
 *
 * <p>Board keeps its state private and exposes no seed-level accessors, so these tests use
 * the reflection helpers in {@link testutil.BoardSupport} ({@code seed}/{@code pits}) to set
 * up arbitrary positions and read pit contents. This keeps the production API untouched while
 * still allowing precise assertions on sowing and capture.
 *
 * <p>The assertions encode the rules <em>as implemented</em> (see README): capture happens
 * when the last seed lands on the mover's <em>own</em> side in a pit holding 2 or 4, and a
 * large sow wraps around and re-seeds its origin pit. If those rules are ever changed to
 * match a different Ayò variant, these tests are the place that pins the intended behavior.
 */
class BoardTest {

    private static final int P1 = 0; // owns pits 0..5
    private static final int P2 = 1; // owns pits 6..11

    // --- move validation ----------------------------------------------------

    @Nested
    @DisplayName("isValidMove")
    class Validation {

        @Test
        @DisplayName("accepts each player's own non-empty pits on a fresh board")
        void acceptsOwnPits() {
            Board board = new Board();
            for (int pit = 0; pit <= 5; pit++) assertTrue(board.isValidMove(P1, pit));
            for (int pit = 6; pit <= 11; pit++) assertTrue(board.isValidMove(P2, pit));
        }

        @Test
        @DisplayName("rejects pits owned by the other player")
        void rejectsWrongOwner() {
            Board board = new Board();
            assertFalse(board.isValidMove(P1, 6), "P1 may not sow from P2's side");
            assertFalse(board.isValidMove(P2, 5), "P2 may not sow from P1's side");
        }

        @Test
        @DisplayName("rejects out-of-range pit indices")
        void rejectsOutOfBounds() {
            Board board = new Board();
            assertFalse(board.isValidMove(P1, -1));
            assertFalse(board.isValidMove(P1, 12));
        }

        @Test
        @DisplayName("rejects an empty pit")
        void rejectsEmptyPit() {
            Board board = new Board();
            board.applyMove(P1, 0); // empties pit 0
            assertFalse(board.isValidMove(P1, 0));
        }
    }

    // --- sowing -------------------------------------------------------------

    @Nested
    @DisplayName("applyMove — sowing")
    class Sowing {

        @Test
        @DisplayName("distributes seeds counter-clockwise and empties the origin")
        void distributesFromOrigin() {
            Board board = new Board(); // all pits = 4
            board.applyMove(P1, 0);
            // 4 seeds from pit 0 land in pits 1..4; last pit (4) holds 5 -> no capture
            assertArrayEquals(new int[]{0, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4}, pits(board));
            assertArrayEquals(new int[]{0, 0}, board.getScores());
        }

        @Test
        @DisplayName("wraps past the board end and re-seeds the origin pit (as implemented)")
        void wrapsAndReSeedsOrigin() {
            Board board = new Board();
            seed(board, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            board.applyMove(P1, 0);
            // 12 seeds: one into each of pits 1..11, then the 12th wraps back into pit 0
            int[] expected = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            assertArrayEquals(expected, pits(board));
            assertArrayEquals(new int[]{0, 0}, board.getScores(), "landing pit holds 1, so no capture");
        }
    }

    // --- capture ------------------------------------------------------------

    @Nested
    @DisplayName("applyMove — capture")
    class Capture {

        @Test
        @DisplayName("captures when the last seed makes an own-side pit hold 2")
        void capturesTwo() {
            Board board = new Board();
            seed(board, 1, 1, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4);
            board.applyMove(P1, 0); // last seed lands in pit 1 -> 2 seeds
            assertEquals(2, board.getScores()[P1]);
            assertEquals(0, pits(board)[1], "captured pit is emptied");
        }

        @Test
        @DisplayName("captures when the last seed makes an own-side pit hold 4")
        void capturesFour() {
            Board board = new Board();
            seed(board, 1, 3, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4);
            board.applyMove(P1, 0); // last seed lands in pit 1 -> 4 seeds
            assertEquals(4, board.getScores()[P1]);
            assertEquals(0, pits(board)[1]);
        }

        @Test
        @DisplayName("does not capture when the pit holds 3")
        void noCaptureOnThree() {
            Board board = new Board();
            seed(board, 1, 2, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4);
            board.applyMove(P1, 0); // last seed lands in pit 1 -> 3 seeds
            assertEquals(0, board.getScores()[P1]);
            assertEquals(3, pits(board)[1]);
        }

        @Test
        @DisplayName("does not capture when the last seed lands on the opponent's side")
        void noCaptureOnOpponentSide() {
            Board board = new Board();
            seed(board, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0);
            board.applyMove(P1, 5); // last seed lands in pit 6 (P2's side) -> 2 seeds
            assertEquals(0, board.getScores()[P1], "capture only applies on the mover's own side");
            assertEquals(2, pits(board)[6], "opponent's pit is left untouched");
        }
    }

    // --- end of round -------------------------------------------------------

    @Nested
    @DisplayName("isGameOver")
    class GameOver {

        @Test
        @DisplayName("is false on a fresh board")
        void notOverInitially() {
            assertFalse(new Board().isGameOver());
        }

        @Test
        @DisplayName("is true when player 1's side is empty")
        void overWhenP1Empty() {
            Board board = new Board();
            seed(board, 0, 0, 0, 0, 0, 0, 4, 4, 4, 4, 4, 4);
            assertTrue(board.isGameOver());
        }

        @Test
        @DisplayName("is true when player 2's side is empty")
        void overWhenP2Empty() {
            Board board = new Board();
            seed(board, 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0);
            assertTrue(board.isGameOver());
        }
    }

    // --- sweeping remaining seeds ------------------------------------------

    @Nested
    @DisplayName("collectRemainingSeeds")
    class Collect {

        @Test
        @DisplayName("sweeps each side's remaining seeds to its owner and clears the board")
        void sweepsToOwners() {
            Board board = new Board();
            seed(board, 1, 2, 3, 0, 0, 0, 1, 1, 1, 1, 0, 0);
            board.collectRemainingSeeds();
            assertArrayEquals(new int[]{6, 4}, board.getScores());
            assertArrayEquals(new int[12], pits(board), "all pits are emptied");
        }
    }

    // --- isolation ----------------------------------------------------------

    @Nested
    @DisplayName("clone")
    class Clone {

        @Test
        @DisplayName("returns an independent copy that cannot mutate the original")
        void copyIsIndependent() {
            Board original = new Board();
            Board copy = original.clone();

            copy.applyMove(P1, 0); // mutate only the copy

            assertEquals(4, pits(original)[0], "original pit is unchanged");
            assertEquals(0, pits(copy)[0], "copy reflects the move");
            assertArrayEquals(new int[]{0, 0}, original.getScores());
        }
    }
}