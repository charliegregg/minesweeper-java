package minesweeper;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Minesweeper
 */
public class Minesweeper {
    public static final String RED_ANSI = "\u001B[31m";
    public static final String WHITE_BG_ANSI = "\u001B[47m";
    public static final String GREEN_BG_ANSI = "\u001B[42m";
    public static final String YELLOW_BG_ANSI = "\u001B[43m";
    public static final String BLUE_BG_ANSI = "\u001B[44m";
    public static final String RESET_ANSI = "\u001B[0m";

    public static int winState = 0; // 0 none, 1 win, -1 lose

    public static void open(Sweeper board, MineTile space) {
        Set<MineTile> queue = new HashSet<>();
        queue.add(space);
        while (queue.size() > 0) {
            MineTile tile = queue.iterator().next();
            queue.remove(tile);
            OpenResponse response = board.tryOpen(tile);
            if (response.tag == "lose") {
                System.out.println("YOU HIT A MINE!");
                winState = -1;
                break;
            }
            if (response.tag == "win") {
                System.out.println("YOU WIN!");
                winState = 1;
                break;
            }
            if (response.tag == "flag") {
                System.out.println("That cell is flagged");
            }
            if (response.neighbours == 0) {
                queue.addAll(board.getTouching(tile));
            }
        }
    }
    public static void toggleFlag(Sweeper board, MineTile space) {
        board.setFlag(space, !board.isFlagged(space));
    }
    public static void display(Sweeper board, Set<MineTile> highlightedSpaces, String colour) {
        String gridDisplay = "";
        gridDisplay += "X ";
        for (int x = 0; x < board.width; x++) {
            gridDisplay += Integer.toHexString(x) + " ";
        }
        for (int y = 0; y < board.height; y++) {
            gridDisplay += "\n";
            gridDisplay += Integer.toHexString(y) + " " + RED_ANSI + WHITE_BG_ANSI;
            for (int x = 0; x < board.width; x++) {
                MineTile space = new MineTile(x, y);
                String tile = board.getDisplay(space) + " ";
                if (highlightedSpaces.contains(space)) {
                    tile = colour + tile + Minesweeper.WHITE_BG_ANSI;
                }
                gridDisplay += tile;
            }
            gridDisplay += RESET_ANSI;
        }
        gridDisplay += "\n";
        System.out.println(gridDisplay);
    }
    public static void display(Sweeper board) {
        display(board, new HashSet<>(), Minesweeper.WHITE_BG_ANSI);
    }

    public static void player() {
        Sweeper ms = new Sweeper(0.1, 15, 15);
        Scanner in = new Scanner(System.in);
        display(ms);
        
        while (winState == 0) {
            System.out.print("Move (x,yf?): ");
            String move = in.nextLine().toLowerCase();
            boolean flag = move.endsWith("f");
            if (flag) {
                move = move.replace("f", "");
            }
            String[] coords = move.split(",");
            MineTile space = new MineTile(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
            if (flag) {
                toggleFlag(ms, space);
            } else {
                open(ms, space);
            }
            display(ms);
        }
        in.close();
    }
    public static void ai() {
        Sweeper ms = new Sweeper(99/480f, 30, 50);
        MineBot bot = new MineBot(ms);
        boolean won = bot.run(Minesweeper::display);
        display(ms);
        System.out.println(won ? "The AI won!" : "The AI lost!");
    }

    public static void main(String args[]) {
        ai();
    }
}
