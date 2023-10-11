package minesweeper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * MineBot
 * A bot that plays minesweeper
 * 
 * @author Charlie Gregg
 */
public class MineBot {

    public Sweeper board; // the board being played on
    private int state; // the current win state
    private Set<Minefield> fields; // all known fields on the board

    public MineBot(Sweeper board) {
        this.board = board;
        this.fields = new HashSet<>();
        this.fields.add(new Minefield(this.board.getAllSpaces(), this.board.mineCount));
        this.state = 0;
    }
    /**
     * Make the bot open a space
     * @param space the space to open
     */
    private void open(MineTile space) {
        Open response = this.board.tryOpen(space);
        if (response.tag() == Open.Tag.LOSE) {
            this.state = -1;
            return;
        }
        if (response.tag() == Open.Tag.WIN) {
            this.state = 1;
            return;
        }
        this.fields.add(new Minefield(this.board.getTouching(space), response.neighbours()));
    }
    /**
     * Make the bot open the best space available
     */
    public void openBest() {
        double best_chance = 1; // best chance of a mine so far
        Minefield best_field = this.fields.iterator().next(); // best field so far
        double chance; // the new chance
        MineTile space; // the space to open
        for (Minefield field : this.fields) {
            chance = field.getChance();
            if (chance < best_chance) {
                best_chance = chance;
                best_field = field;
            }
        }
        space = new ArrayList<>(best_field.getSpaces()).get(
            (int) Math.floor(Math.random()*best_field.getSpaces().size())
        );
        this.open(space);
    }
    /**
     * Run the bot until it wins or loses
     * @param interupter a function to run every iteration with the board
     * @return true if the bot won
     */
    public boolean run(Consumer<Sweeper> interupter) {
        while (this.state == 0) {
            interupter.accept(this.board);
            this.expand();
        }
        return this.state == 1;
    }
    /**
     * Expand the fields on the board and resolve
     * This is the main logic of the bot
     */
    private void expand() {
        Set<Minefield> newFields; // fields after filtering for 0's
        boolean changed; // if there has been an update in the fields after reduction
        boolean found; // if a safe tile to open has been found
        boolean solved; // if the board is solved
        if (this.state != 0) {
            return;
        }
        // remove any field spaces that are over open tiles
        newFields = new HashSet<>();
        for (Minefield field : this.fields) {
            field.filter(this.board::isOpen, true);
            if (field.exists()) {
                newFields.add(field);
            }
        }
        this.fields = newFields;
        // reduce the size and overlap of fields
        changed = true;
        reduce: while (changed) {
            changed = false;
            ArrayList<Minefield> orderedFields = new ArrayList<>(this.fields);
            for (int i = 0; i < orderedFields.size(); i++) {
                Minefield aField = orderedFields.get(i);
                for (int j = i+1; j < orderedFields.size(); j++) {
                    Minefield bField = orderedFields.get(j);
                    ArrayList<Minefield> result = Minefield.intersect(aField, bField);
                    if (result.size() > 0) {
                        result.removeIf(field -> (!field.exists()));
                        this.fields.remove(aField);
                        this.fields.remove(bField);
                        this.fields.addAll(result);
                        changed = true;
                        continue reduce; // reexamine the fields
                    }
                }
            }
        }
        // find possible opennings and open them
        found = false;
        for (Minefield field : new ArrayList<>(this.fields)) {
            if (field.empty()) {
                for (MineTile space : field.getSpaces()) {
                    this.open(space);
                    found = true;
                }
            }
            // add flags for show (not needed since bot doesn't look for them)
            if (field.filled()) {
                for (MineTile space : field.getSpaces()) {
                    this.board.setFlag(space, true);
                }
            }
        }
        // test if the board is solved
        solved = true;
        for (Minefield field : new ArrayList<>(this.fields)) {
            if (!field.filled()) {
                solved = false;
                break;
            }
        }
        if (solved) {
            this.state = 1;
        } else if (!found) {
            this.openBest(); // if we don't know, guess
        }
    }
    /**
     * Gets all spaces in at least min fields
     * @param min the minimum number of fields a space must be in
     * @return the spaces in at least min fields
     */
    public Set<MineTile> getHighlights(int min) {
        int[][] counts = new int[this.board.width][this.board.height]; // number of fields a space is in
        Set<MineTile> tiles = new HashSet<>(); // spaces in at least min fields
        for (Minefield field : this.fields) {
            for (MineTile space : field.getSpaces()) {
                counts[space.x()][space.y()]++;
            }
        }
        for (int x = 0; x < this.board.width; x++) {
            for (int y = 0; y < this.board.height; y++) {
                if (counts[x][y] >= min) {
                    tiles.add(new MineTile(x, y));
                }
            }
        }
        return tiles;
    }
}