package model;

import java.util.concurrent.locks.ReentrantLock;

public class Board implements Cloneable {

    private final int[] pits = new int[12];
    private final int[] scores = new int[2];
    private final ReentrantLock lock = new ReentrantLock();

    public Board() {
        for (int i = 0; i < pits.length; i++) pits[i] = 4;
    }

    public boolean isValidMove(int playerId, int pitIndex) {
        lock.lock();
        try {
            if (pitIndex < 0 || pitIndex >= pits.length) return false;
            if (playerId == 0 && pitIndex > 5) return false;
            if (playerId == 1 && pitIndex < 6) return false;
            return pits[pitIndex] > 0;
        } finally {
            lock.unlock();
        }
    }

    public void applyMove(int playerId, int pitIndex) {
        lock.lock();
        try {
            int seeds = pits[pitIndex];
            pits[pitIndex] = 0;
            int currentPit = pitIndex;

            for (int i = 0; i < seeds; i++) {
                currentPit = (currentPit + 1) % 12;
                pits[currentPit]++;
            }

            // Capture
            if (playerId == 0 && currentPit <= 5 || playerId == 1 && currentPit >= 6) {
                if (pits[currentPit] == 2 || pits[currentPit] == 4) {
                    scores[playerId] += pits[currentPit];
                    pits[currentPit] = 0;
                }
            }

        } finally {
            lock.unlock();
        }
    }

    public boolean isGameOver() {
        lock.lock();
        try {
            boolean player1Empty = true, player2Empty = true;
            for (int i = 0; i < 6; i++) if (pits[i] > 0) player1Empty = false;
            for (int i = 6; i < 12; i++) if (pits[i] > 0) player2Empty = false;
            return player1Empty || player2Empty;
        } finally {
            lock.unlock();
        }
    }

    public int[] getScores() {
        lock.lock();
        try {
            return scores.clone();
        } finally {
            lock.unlock();
        }
    }

    public void collectRemainingSeeds() {
        lock.lock();
        try {
            for (int i = 0; i < 6; i++) {
                scores[0] += pits[i]; pits[i] = 0;
            }
            for (int i = 6; i < 12; i++) {
                scores[1] += pits[i]; pits[i] = 0;
            }
        } finally {
            lock.unlock();
        }
    }

    public void printBoard() {
        lock.lock();
        try {
            System.out.println("Pits: ");
            for (int i = 0; i < pits.length; i++) System.out.print(pits[i] + " ");
            System.out.println("\nScores: Player1=" + scores[0] + " Player2=" + scores[1]);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Board clone() {
        Board copy = new Board();
        lock.lock();
        try {
            System.arraycopy(this.pits, 0, copy.pits, 0, pits.length);
            System.arraycopy(this.scores, 0, copy.scores, 0, scores.length);
        } finally {
            lock.unlock();
        }
        return copy;
    }
}
