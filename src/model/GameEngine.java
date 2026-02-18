package model;

import java.util.concurrent.locks.ReentrantLock;

public class GameEngine {

    private final Board board;
    private final ReentrantLock lock = new ReentrantLock(true);

    public GameEngine(Board board) { this.board = board; }

    public MoveResult playMove(int playerId, int pitIndex) {
        lock.lock();
        try {
            if (!board.isValidMove(playerId, pitIndex)) {
                return new MoveResult(false, 0, pitIndex, board.isGameOver());
            }

            int before = board.getScores()[playerId];
            board.applyMove(playerId, pitIndex);
            int after = board.getScores()[playerId];

            int captured = after - before;
            boolean gameOver = board.isGameOver();

            return new MoveResult(true, captured, pitIndex, gameOver);
        } finally {
            lock.unlock();
        }
    }

    public Board getBoard() { return board; }
}
