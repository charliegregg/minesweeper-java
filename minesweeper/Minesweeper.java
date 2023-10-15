package minesweeper;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Minesweeper
 * A minesweeper game
 * 
 * @author Charlie Gregg
 */
public class Minesweeper {
    public static final String RED_ANSI = "\u001B[31m"; // ANSI escape codes for colours
    public static final String WHITE_BG_ANSI = "\u001B[47m";
    public static final String GREEN_BG_ANSI = "\u001B[42m";
    public static final String YELLOW_BG_ANSI = "\u001B[43m";
    public static final String BLUE_BG_ANSI = "\u001B[44m";
    public static final String RESET_ANSI = "\u001B[0m";

    public static int winState = 0; // 0 none, 1 win, -1 lose

    public static int width; // the width of the board to play on
    public static int height; // the height of the board to play on
    public static double mineChance; // the chance of a mine being on each tile

    public enum Difficulty {
        EASY(1,     15, 15, 0.07),
        MEDIUM(2,   15, 15, 0.10),
        HARD(3,     15, 25, 0.14),
        EXPERT(4,   20, 30, 0.19),
        INSANE(5,   30, 50, 0.22);

        public final int value; // difficulty number
        public final int width; // width of board
        public final int height; // height of board
        public final double mineChance; // chance of each tile being a mine

        Difficulty(int value, int width, int height, double mineChance) {
            this.value = value;
            this.width = width;
            this.height = height;
            this.mineChance = mineChance;
        }
    }

    /**
     * Open a space on the board
     * @param board the board to open on
     * @param space the space to open
     */
    public static void open(Sweeper board, MineTile space) {
        Set<MineTile> queue = new HashSet<>(); // queue of spaces to open
        MineTile tile; // the next tile to open
        Open response; // the response from opening the tile
        
        queue.add(space);
        while (queue.size() > 0) {
            tile = queue.iterator().next();
            queue.remove(tile);
            response = board.tryOpen(tile);
            if (response.tag() == Open.Tag.LOSE) {
                winState = -1;
                break;
            }
            if (response.tag() == Open.Tag.WIN) {
                winState = 1;
                break;
            }
            if (response.tag() == Open.Tag.FLAG) {
                System.out.println("That cell is flagged");
            }
            if (response.neighbours() == 0) {
                queue.addAll(board.getTouching(tile));
            }
        }
    }
    /**
     * Toggle a flag on a space
     * @param board the board to toggle on
     * @param space the space to toggle
     */
    public static void toggleFlag(Sweeper board, MineTile space) {
        board.setFlag(space, !board.isFlagged(space));
    }
    /**
     * Display the board
     * @param board the board to display
     * @param highlightedSpaces the spaces to highlight
     * @param colour the colour to highlight with
     */
    public static void display(Sweeper board, Set<MineTile> highlightedSpaces, String colour) {
        String gridDisplay = "X "; // the string to display
        MineTile space; // the current space being displayed
        String tile; // the string to display for the current space
        
        for (int x = 0; x < board.width; x++) {
            gridDisplay += Integer.toHexString(x) + (x < 16 ? " " : "");
        }
        for (int y = 0; y < board.height; y++) {
            gridDisplay += "\n";
            gridDisplay += Integer.toHexString(y) + (y < 16 ? " " : "") + RED_ANSI + WHITE_BG_ANSI;
            for (int x = 0; x < board.width; x++) {
                space = new MineTile(x, y);
                tile = board.getDisplay(space) + " ";
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
    /**
     * Display the board
     * @param board the board to display
     */
    public static void display(Sweeper board) {
        display(board, new HashSet<>(), Minesweeper.WHITE_BG_ANSI);
    }
    /**
     * Let the user play the game
     * @param in the scanner to read input from
     */
    public static void player(Scanner in) {
        Sweeper ms = new Sweeper(mineChance, width, height); // board
        String move; // the move to make
        MineTile space; // the space to move to
        boolean flag; // whether to flag the space
        String[] coords; // the coordinates of the space
        
        display(ms);
        // display a help message at the beginning
        System.out.println("Make moves using decimal coordinates, follow with an 'f' to flag");
        while (winState == 0) {
            do {
                System.out.print("Move (x,yf?): ");
                move = in.nextLine().toLowerCase();
            } while (!move.matches(" *\\d+ *, *\\d+ *f?")); // regex for a valid move
            flag = move.endsWith("f");
            if (flag) {
                move = move.replace("f", ""); // safe due to regex, any 'f' will always be the last char
            }
            coords = move.split(","); // may need to be stripped due to whitespace
            space = new MineTile(Integer.parseInt(coords[0].strip()), Integer.parseInt(coords[1].strip()));
            if (flag) {
                toggleFlag(ms, space);
            } else {
                open(ms, space);
            }
            display(ms);
        }
        System.out.println(winState == 1 ? "You won!" : "You lost!");
    }
    /**
     * Let the AI play the game
     */
    public static void ai() {
        Sweeper ms = new Sweeper(mineChance, width, height); // board
        MineBot bot = new MineBot(ms); // the bot
        boolean won = bot.run(Minesweeper::display); // run the bot
        
        display(ms);
        System.out.println(won ? "The AI won!" : "The AI lost!");
    }
    /**
     * Set the difficulty of the game
     * @param difficulty the difficulty to set to (1-5)
     */
    public static void setDifficulty(int difficulty) {
        width = Difficulty.values()[difficulty - 1].width;
        height = Difficulty.values()[difficulty - 1].height;
        mineChance = Difficulty.values()[difficulty - 1].mineChance;
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String mode; // "p" or "a"
        int difficulty; // 1-5

        do {
            System.out.print("Play or AI (p/a): ");
            mode = in.nextLine().toLowerCase();
        } while (!mode.equals("p") && !mode.equals("a"));
        do {
            try {
                System.out.print("Difficulty (1-5): ");
                difficulty = Integer.parseInt(in.nextLine());
            } catch (NumberFormatException e) {
                difficulty = 0;
                System.out.println("Invalid difficulty");
            }
        } while (difficulty < 1 || difficulty > 5);
        setDifficulty(difficulty);
        if (mode.equals("p")) {
            player(in);
        } else {
            ai();
        }
    }
}
