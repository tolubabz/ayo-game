import controller.GameController;
import model.Board;
import model.GameEngine;
import player.HumanPlayer;
import player.AIPlayer;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Game Mode:\n1. Human vs Human\n2. Human vs AI\n3. AI vs AI");
        int mode = sc.nextInt();

        Board board = new Board();
        GameEngine engine = new GameEngine(board);
        GameController controller = new GameController(engine);

        Thread player1, player2;
        AIPlayer ai1 = null, ai2 = null;

        switch (mode) {
            case 1:
                player1 = new Thread(new HumanPlayer(0, controller), "Player-1");
                player2 = new Thread(new HumanPlayer(1, controller), "Player-2");
                break;
            case 2:
                player1 = new Thread(new HumanPlayer(0, controller), "Human-1");
                ai2 = new AIPlayer(1, controller);
                player2 = new Thread(ai2, "AI-2");
                break;
            case 3:
                ai1 = new AIPlayer(0, controller);
                ai2 = new AIPlayer(1, controller);
                player1 = new Thread(ai1, "AI-1");
                player2 = new Thread(ai2, "AI-2");
                break;
            default:
                System.out.println("Invalid option"); return;
        }

        player1.start();
        player2.start();
        player1.join();
        player2.join();

        if (ai1 != null) ai1.shutdown();
        if (ai2 != null) ai2.shutdown();

        board.collectRemainingSeeds();
        board.printBoard();
        System.out.println("Game Over!");
    }
}
