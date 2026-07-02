package controller;

import model.Board;
import model.GameEngine;
import model.MoveResult;
import util.GameLogger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GameController {

    private final GameEngine engine;
    private final ReentrantLock turnLock = new ReentrantLock();
    private final Condition playerTurn = turnLock.newCondition();
    private int currentPlayer = 0;

    public GameController(GameEngine engine) { this.engine = engine; }

    public void waitForTurn(int playerId) {
        turnLock.lock();
        try {
            while (currentPlayer != playerId && !engine.getBoard().isGameOver()) {
                playerTurn.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            turnLock.unlock();
        }
    }

    public void playMove(int playerId, int pitIndex) {
        MoveResult result = engine.playMove(playerId, pitIndex);

        if (!result.isSuccess()) {

            GameLogger.log("Player " + (playerId + 1) + " attempted invalid move at pit " + pitIndex);
        } else {
            GameLogger.log("Player " + (playerId + 1) + " captured " + result.getCapturedSeeds() + " seeds at pit " + pitIndex);
        }

        engine.getBoard().printBoard();

        if (result.isGameOver()) GameLogger.log("Game over detected!");
    }

    public void endTurn() {
        turnLock.lock();
        try {
            currentPlayer = 1 - currentPlayer;
            GameLogger.log("Switching turn. Next player: " + (currentPlayer + 1));
            playerTurn.signalAll();
        } finally {
            turnLock.unlock();
        }
    }

    public boolean isGameOver() { return engine.getBoard().isGameOver(); }

    public Board getBoardSnapshot() { return engine.getBoard().clone(); }
}
