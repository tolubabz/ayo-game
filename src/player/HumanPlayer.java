package player;

import controller.GameController;

import java.util.Scanner;

public class HumanPlayer extends Player {

    private static final Object inputLock = new Object();
    private static final Scanner scanner = new Scanner(System.in);

    public HumanPlayer(int playerId, GameController controller) { super(playerId, controller); }

    @Override
    protected int chooseMove() {
        int move = -1;
        boolean valid = false;

        while (!valid) {
            synchronized (inputLock) {
                try {
                    System.out.print("Player " + (playerId + 1) + " select pit: ");
                    move = Integer.parseInt(scanner.nextLine());
                    valid = true;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, enter number 0-5 or 6-11");
                }
            }
        }
        return move;
    }
}
