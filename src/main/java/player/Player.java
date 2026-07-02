package player;

import controller.GameController;

public abstract class Player implements Runnable {

    protected final int playerId;
    protected final GameController controller;

    public Player(int playerId, GameController controller) {
        this.playerId = playerId;
        this.controller = controller;
    }

    protected abstract int chooseMove();

    @Override
    public void run() {
        while (!controller.isGameOver()) {
            controller.waitForTurn(playerId);
            if (controller.isGameOver()) break;

            int move = chooseMove();
            controller.playMove(playerId, move);
            controller.endTurn();
        }
    }
}
