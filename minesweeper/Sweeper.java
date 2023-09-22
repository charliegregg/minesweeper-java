package minesweeper;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Sweeper
 * A minesweeper board simulator
 * 
 * @author Charlie Gregg
 */
public class Sweeper {
    private boolean[][] mines; // true if there is a mine at this location
    private int[][] neighbours; // the number of neighbouring mines
    private boolean[][] open; // true if the tile has been opened
    private boolean[][] flags; // true if the tile has been flagged
    private boolean filled; // true if the board has been populated
    MineTile hitLocation; // the location of the hit mine
    int width; // the width of the board
    int height; // the height of the board
    int mineCount; // the number of mines on the board
    double mineChance; // the chance of a mine being on each tile
    long seed; // the seed used to generate the board
    int remainingSpaces; // the number of unopened tiles

    private final static String SYMBOLS = " 12345678#FX"; // the symbols used to display the board

    public Sweeper(double mineChance, int width, int height, long seed) {
        this.mineChance = Math.min(Math.max(mineChance, 0), 0.5);
        this.width = width;
        this.height = height;
        this.mineCount = (int) Math.floor((this.width*this.height - 1) * this.mineChance);
        this.filled = false;
        this.seed = seed;
        this.neighbours = new int[this.width][this.height];
        this.mines = new boolean[this.width][this.height];
        this.flags = new boolean[this.width][this.height];
        this.open = new boolean[this.width][this.height];
        this.remainingSpaces = this.width * this.height;
        this.hitLocation = new MineTile(-1, -1);
    }
    public Sweeper(double mineChance, int width, int height) {
        this(mineChance, width, height, new Random().nextLong());
    }
    public Sweeper(String board) {
        this.mineCount = board.length() - board.replace("*", "").length();
        this.mineChance = (this.mineCount/board.length());
        this.filled = true;
        this.seed = 0;
        this.hitLocation = new MineTile(-1, -1);
        this.init(board);
    }
    /**
     * Initialise the board
     * @param board the board string initialise from
     */
    private void init(String board) {
        String[] rows = board.split("\n"); // rows of the board
        this.height = rows.length;
        this.width = rows[0].length();
        this.neighbours = new int[this.width][this.height];
        this.mines = new boolean[this.width][this.height];
        this.flags = new boolean[this.width][this.height];
        this.open = new boolean[this.width][this.height];
        this.remainingSpaces = this.width * this.height;
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                if (rows[y].charAt(x) == '*') {
                    this.setMine(new MineTile(x, y), true);
                }
            }
        }
    }
    /**
     * Place mines onto the grid, ignoring the safe tile
     * @param safe the tile to ignore
     */
    private void plantMines(MineTile safe) {
        int placed = 0;
        Random rand = new Random(this.seed);
        while (placed < this.mineCount) {
            MineTile space = new MineTile(rand.nextInt(this.width), rand.nextInt(this.height));
            if (this.isMine(space)) {continue;}
            if (space.equals(safe)) {continue;}
            this.setMine(space, true);
            placed++;
        }
        this.filled = true;
    }
    /**
     * Check if a space is a mine
     * @param space the space to check
     * @return true if the space is a mine
     */
    private boolean isMine(MineTile space) {
        return this.mines[space.x()][space.y()];
    }
    /**
     * Set a space to be a mine or not
     * @param space the space to set
     * @param mine whether the space is a mine
     */
    private void setMine(MineTile space, boolean mine) {
        if (this.mines[space.x()][space.y()] == mine) {
            return;
        }
        int neighbourChange = (!this.mines[space.x()][space.y()] && mine) ? 1 : -1;

        this.mines[space.x()][space.y()] = mine;
        Set<MineTile> surrounding = this.getSurrounding(space);
        for (MineTile tile : surrounding) {
            this.neighbours[tile.x()][tile.y()] += neighbourChange;
        }
    }
    /**
     * Get the number of neighbouring mines
     * @param space the space to check
     * @return the number of neighbouring mines
     */
    private int getNeighbours(MineTile space) {
        return this.neighbours[space.x()][space.y()];
    }
    /**
     * Get all tiles touching a space which are unopened
     * @param space the space to check
     * @return the unopened tiles touching the space
     */
    public Set<MineTile> getTouching(MineTile space) {
        return this.getSurrounding(space, true);
    }
    /**
     * Add a space to a set given a condition
     * @param spaces the set of spaces
     * @param space the space to add
     * @param onlyClosed whether to only add closed spaces
     */
    private void addIf(Set<MineTile> spaces, MineTile space, boolean onlyClosed) {
        if (!onlyClosed || !this.isOpen(space)) {
            spaces.add(space);
        }
    }
    /**
     * Get the spaces surrounding a space
     * @param space the space to check
     * @param onlyClosed whether to only get closed spaces
     * @return the spaces surrounding the space
     */
    public Set<MineTile> getSurrounding(MineTile space, boolean onlyClosed) {
        Set<MineTile> spaces = new HashSet<>();
        if (space.y() > 0) {
            this.addIf(spaces, new MineTile(space.x(), space.y() - 1), onlyClosed);
        }
        if (space.y() < this.height - 1) {
            this.addIf(spaces, new MineTile(space.x(), space.y() + 1), onlyClosed);
        }
        if (space.x() > 0) {
            this.addIf(spaces, new MineTile(space.x() - 1, space.y()), onlyClosed);
            if (space.y() > 0) {
                this.addIf(spaces, new MineTile(space.x() - 1, space.y() - 1), onlyClosed);
            }
            if (space.y() < this.height - 1) {
                this.addIf(spaces, new MineTile(space.x() - 1, space.y() + 1), onlyClosed);
            }
        }
        if (space.x() < this.width - 1) {
            this.addIf(spaces, new MineTile(space.x() + 1, space.y()), onlyClosed);
            if (space.y() > 0) {
                this.addIf(spaces, new MineTile(space.x() + 1, space.y() - 1), onlyClosed);
            }
            if (space.y() < this.height - 1) {
                this.addIf(spaces, new MineTile(space.x() + 1, space.y() + 1), onlyClosed);
            }
        }
        return spaces;
    }
    /**
     * Get the spaces surrounding a space
     * @param space the space to check
     * @return the spaces surrounding the space
     */
    public Set<MineTile> getSurrounding(MineTile space) {
        return this.getSurrounding(space, false);
    }
    /**
     * Check if a tile is open
     * @param space the space to check
     * @return true if the space is open
     */
    public boolean isOpen(MineTile space) {
        return this.open[space.x()][space.y()];
    }
    /**
     * Set a tile to be open
     * @param space the space to set
     */
    private void setOpen(MineTile space) {
        if (!this.isOpen(space)) {
            this.remainingSpaces -= 1;
            this.open[space.x()][space.y()] = true;
        }
    }
    /**
     * Check if a tile is flagged
     * @param space the space to check
     * @return true if the space is flagged
     */
    public boolean isFlagged(MineTile space) {
        return this.flags[space.x()][space.y()];
    }
    /**
     * Set a tile to be flagged
     * @param space the space to set
     * @param flag whether to flag the space
     */
    public void setFlag(MineTile space, boolean flag) {
        this.flags[space.x()][space.y()] = flag;
    }
    /**
     * Attempt to open a tile
     * @param space the space to open
     * @return the result of opening the tile
     */
    public Open tryOpen(MineTile space) {
        if (!this.filled) {
            this.plantMines(space);
        }
        if (this.isFlagged(space)) {
            return new Open(0, Open.Tag.FLAG);
        }
        if (this.isMine(space)) {
            this.hitLocation = space;
            return new Open(0, Open.Tag.LOSE);
        }
        this.setOpen(space);
        Open.Tag tag = this.remainingSpaces == this.mineCount ? Open.Tag.WIN : Open.Tag.NONE;
        return new Open(this.getNeighbours(space), tag);
    }
    /**
     * Get the display character for a space
     * @param space the space to display
     * @return the character to display
     */
    public char getDisplay(MineTile space) {
        if (this.isOpen(space)) {
            return Sweeper.SYMBOLS.charAt(this.getNeighbours(space));
        } else if (this.hitLocation.equals(space)) {
            return Sweeper.SYMBOLS.charAt(11);
        } else if (this.isFlagged(space)) {
            return Sweeper.SYMBOLS.charAt(10);
        } else{
            return Sweeper.SYMBOLS.charAt(9);
        }
    }
    /**
     * Get all tiles as a set
     * @return all tiles as a set
     */
    public Set<MineTile> getAllSpaces() {
        Set<MineTile> spaces = new HashSet<>(this.height * this.width);
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                spaces.add(new MineTile(x, y));
            }
        }
        return spaces;
    }
}
/**
 * Open
 * The result of opening a tile
 * 
 * @param neighbours the number of neighbouring mines
 * @param tag the result of opening the tile
 */
record Open(int neighbours, Tag tag) {
    enum Tag {
        WIN, // the game has been won
        LOSE, // the game has been lost
        FLAG, // the tile is flagged
        NONE // normal opening
    }
    public int neighbours() {
        return this.neighbours;
    }
    public Tag tag() {
        return this.tag;
    }
}