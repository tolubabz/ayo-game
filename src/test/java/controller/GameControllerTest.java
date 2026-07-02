package controller;

import model.Board;
import model.GameEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.BoardSupport.pits;
import static testutil.BoardSupport.seed;

/**
 * Tier 4 tests for {@link GameController} — the turn-arbitration layer.
 *
 * <p>These are small integration tests over real threads. Every blocking assertion is wrapped
 * in a timeout so a coordination bug (e.g. a missed signal or a deadlock) fails fast instead
 * of hanging the suite.
 */
class GameControllerTest {

    private static GameController controllerOver(int... pits) {
        Board board = new Board();
        if (pits.length == 12) seed(board, pits);
        return new GameController(new GameEngine(board));
    }

    @Test
    @DisplayName("a waiting player only proceeds once endTurn hands over the turn")
    void endTurnHandsOverTheTurn() throws InterruptedException {
        GameController controller = controllerOver(); // fresh board, current player = 0
        AtomicBoolean player1Proceeded = new AtomicBoolean(false);

        Thread player1 = new Thread(() -> {
            controller.waitForTurn(1);
            player1Proceeded.set(true);
        }, "player-1");
        player1.start();
        try {
            // While it is player 0's turn, player 1 must stay blocked.
            Thread.sleep(100);
            assertFalse(player1Proceeded.get(), "player 1 must wait while it is player 0's turn");

            controller.endTurn(); // flip to player 1

            player1.join(1000);
            assertTrue(player1Proceeded.get(), "player 1 proceeds once it is its turn");
        } finally {
            player1.interrupt(); // unblock the thread if the assertions failed
            player1.join(1000);
        }
    }

    @Test
    @DisplayName("two player threads take strictly alternating turns")
    void turnsStrictlyAlternate() {
        GameController controller = controllerOver(); // fresh board, never game-over
        int rounds = 25;
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Thread p0 = new Thread(playerLoop(controller, 0, rounds, order), "player-0");
            Thread p1 = new Thread(playerLoop(controller, 1, rounds, order), "player-1");
            p0.start();
            p1.start();
            p0.join();
            p1.join();
        });

        assertEquals(2 * rounds, order.size(), "every turn was taken exactly once");
        for (int i = 0; i < order.size(); i++) {
            assertEquals(i % 2, order.get(i).intValue(), "turn " + i + " went to the wrong player");
        }
    }

    @Test
    @DisplayName("waitForTurn returns once the game is over, even out of turn")
    void gameOverReleasesWaiters() {
        // Player 1's side is empty -> game over. Current player is 0, yet player 1 must not block.
        GameController controller = controllerOver(4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0);
        assertTrue(controller.isGameOver());
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> controller.waitForTurn(1));
    }

    @Test
    @DisplayName("getBoardSnapshot returns an independent copy, not the live board")
    void snapshotIsADefensiveCopy() {
        Board board = new Board();
        GameController controller = new GameController(new GameEngine(board));

        Board snapshot = controller.getBoardSnapshot();

        assertNotSame(board, snapshot, "snapshot must be a copy");
        assertArrayEquals(pits(board), pits(snapshot), "copy starts equal to the live board");

        snapshot.applyMove(0, 0); // mutating the snapshot must not touch the live board
        assertEquals(4, pits(board)[0], "live board is unaffected by snapshot mutation");
    }

    /** A player thread that takes {@code rounds} turns, recording the order in which it moves. */
    private static Runnable playerLoop(GameController controller, int id, int rounds, List<Integer> order) {
        return () -> {
            for (int i = 0; i < rounds; i++) {
                controller.waitForTurn(id);
                order.add(id);
                controller.endTurn();
            }
        };
    }
}