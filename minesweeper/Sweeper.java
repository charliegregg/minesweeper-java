package minesweeper;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Sweeper
 */
class OpenResponse {
    int neighbours;
    String tag;

    public OpenResponse(int neighbours, String tag) {
        this.neighbours = neighbours;
        this.tag = tag;
    }
}
public class Sweeper {

    private boolean[][] mines;
    private int[][] neighbours;
    private boolean[][] open;
    private boolean[][] flags;
    private boolean filled;
    MineTile hitLocation;
    int width;
    int height;
    int mineCount;
    double mineChance;
    long seed;
    int remainingSpaces;

    private final static String SYMBOLS = " 12345678#FX";

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
    private void init(String board) {
        String[] rows = board.split("\n");
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
    /*
     * place mines onto the grid, ignoring the safe tile
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
    private boolean isMine(MineTile space) {
        return this.mines[space.x][space.y];
    }
    private void setMine(MineTile space, boolean mine) {
        if (this.mines[space.x][space.y] == mine) {
            return;
        }
        int neighbourChange = (!this.mines[space.x][space.y] && mine) ? 1 : -1;

        this.mines[space.x][space.y] = mine;
        Set<MineTile> surrounding = this.getSurrounding(space);
        for (MineTile tile : surrounding) {
            this.neighbours[tile.x][tile.y] += neighbourChange;
        }
    }
    private int getNeighbours(MineTile space) {
        return this.neighbours[space.x][space.y];
    }
    public Set<MineTile> getTouching(MineTile space) {
        return this.getSurrounding(space, true);
    }
    private void addIf(Set<MineTile> spaces, MineTile space, boolean onlyClosed) {
        if (!onlyClosed || !this.isOpen(space)) {
            spaces.add(space);
        }
    }
    public Set<MineTile> getSurrounding(MineTile space, boolean onlyClosed) {
        Set<MineTile> spaces = new HashSet<>();
        if (space.y > 0) {
            this.addIf(spaces, new MineTile(space.x, space.y - 1), onlyClosed);
        }
        if (space.y < this.height - 1) {
            this.addIf(spaces, new MineTile(space.x, space.y + 1), onlyClosed);
        }
        if (space.x > 0) {
            this.addIf(spaces, new MineTile(space.x - 1, space.y), onlyClosed);
            if (space.y > 0) {
                this.addIf(spaces, new MineTile(space.x - 1, space.y - 1), onlyClosed);
            }
            if (space.y < this.height - 1) {
                this.addIf(spaces, new MineTile(space.x - 1, space.y + 1), onlyClosed);
            }
        }
        if (space.x < this.width - 1) {
            this.addIf(spaces, new MineTile(space.x + 1, space.y), onlyClosed);
            if (space.y > 0) {
                this.addIf(spaces, new MineTile(space.x + 1, space.y - 1), onlyClosed);
            }
            if (space.y < this.height - 1) {
                this.addIf(spaces, new MineTile(space.x + 1, space.y + 1), onlyClosed);
            }
        }
        return spaces;
    }
    public Set<MineTile> getSurrounding(MineTile space) {
        return this.getSurrounding(space, false);
    }
    public boolean isOpen(MineTile space) {
        return this.open[space.x][space.y];
    }
    private void setOpen(MineTile space) {
        if (!this.isOpen(space)) {
            this.remainingSpaces -= 1;
            this.open[space.x][space.y] = true;
        }
    }
    public void setFlag(MineTile space, boolean flag) {
        this.flags[space.x][space.y] = flag;
    }
    public boolean isFlagged(MineTile space) {
        return this.flags[space.x][space.y];
    }
    public OpenResponse tryOpen(MineTile space) {
        if (!this.filled) {
            this.plantMines(space);
        }
        if (this.isFlagged(space)) {
            return new OpenResponse(0, "flag");
        }
        if (this.isMine(space)) {
            this.hitLocation = space;
            return new OpenResponse(0, "lose");
        }
        this.setOpen(space);
        return new OpenResponse(this.getNeighbours(space), this.remainingSpaces == this.mineCount ? "win" : "");
    }
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