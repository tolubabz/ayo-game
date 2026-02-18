package model;

public class MoveResult {

    private final boolean success;
    private final int capturedSeeds;
    private final int lastPitIndex;
    private final boolean gameOver;

    public MoveResult(boolean success, int capturedSeeds, int lastPitIndex, boolean gameOver) {
        this.success = success;
        this.capturedSeeds = capturedSeeds;
        this.lastPitIndex = lastPitIndex;
        this.gameOver = gameOver;
    }

    public boolean isSuccess() { return success; }
    public int getCapturedSeeds() { return capturedSeeds; }
    public int getLastPitIndex() { return lastPitIndex; }
    public boolean isGameOver() { return gameOver; }
}
