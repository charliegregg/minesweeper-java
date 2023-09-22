package minesweeper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * MineBot
 */
public class MineBot {

    public Sweeper board;
    private int state;
    private Set<Minefield> fields;

    public MineBot(Sweeper board) {
        this.board = board;
        this.fields = new HashSet<>();
        this.fields.add(new Minefield(this.board.getAllSpaces(), this.board.mineCount));
        this.state = 0;
    }

    private void open(MineTile space) {
        OpenResponse response = this.board.tryOpen(space);
        if (response.tag == "lose") {
            this.state = -1;
            return;
        }
        if (response.tag == "win") {
            this.state = 1;
            return;
        }
        this.fields.add(new Minefield(this.board.getTouching(space), response.neighbours));
    }
    public void openBest() {
        double best_chance = 1;
        Minefield best_field = this.fields.iterator().next();
        for (Minefield field : this.fields) {
            double chance = field.getChance();
            if (chance < best_chance) {
                best_chance = chance;
                best_field = field;
            }
        }
        MineTile space = new ArrayList<>(best_field.getSpaces()).get((int) Math.floor(Math.random()*best_field.getSpaces().size()));
        this.open(space);
    }
    public boolean run(Consumer<Sweeper> interupter) {
        while (this.state == 0) {
            interupter.accept(this.board);
            this.expand();
        }
        return this.state == 1;
    }

    private void expand() {
        if (this.state != 0) {
            return;
        }
        // remove any field spaces that are over open tiles
        Set<Minefield> newFields = new HashSet<>();
        for (Minefield field : this.fields) {
            field.filter(this.board::isOpen, true);
            if (field.exists()) {
                newFields.add(field);
            }
        }
        this.fields = newFields;
        // reduce the size and overlap of fields
        Set<Minefield[]> queue = new HashSet<>();
        ArrayList<Minefield> orderedFields = new ArrayList<>(this.fields);
        for (int i = 0; i < orderedFields.size(); i++) {
            for (int j = i+1; j < orderedFields.size(); j++) {
                if (orderedFields.get(i).touches(orderedFields.get(j))) {
                    queue.add(new Minefield[] {orderedFields.get(i), orderedFields.get(j)});
                }
            }
        }
        while (queue.size() > 0) {
            Minefield[] pair = queue.iterator().next();
            queue.remove(pair);
            Minefield aField = pair[0];
            Minefield bField = pair[1];
            ArrayList<Minefield> result = Minefield.intersect(aField, bField);
            if (result.size() > 0) {
                result.removeIf(field -> (field.getSpaces().size() == 0));
                this.fields.remove(aField);
                this.fields.remove(bField);
                queue.removeIf(testPair -> 
                       testPair[0].equals(aField) 
                    || testPair[0].equals(bField) 
                    || testPair[1].equals(aField) 
                    || testPair[1].equals(bField)
                );
                for (Minefield newField : result) {
                    for (Minefield field : this.fields) {
                        if (newField.touches(field)) {
                            queue.add(new Minefield[] {newField, field});
                        }
                    }
                }

                this.fields.addAll(result);
            }
        }
        // find possible opennings and open them
        boolean found = false;
        for (Minefield field : new ArrayList<>(this.fields)) {
            if (field.empty()) {
                for (MineTile space : field.getSpaces()) {
                    this.open(space);
                    found = true;
                }
            }
            // add flags for show
            if (field.filled()) {
                for (MineTile space : field.getSpaces()) {
                    this.board.setFlag(space, true);
                }
            }
        }
        // test if the board is solved
        boolean solved = true;
        for (Minefield field : new ArrayList<>(this.fields)) {
            if (!field.filled()) {
                solved = false;
                break;
            }
        }
        if (solved) {
            this.state = 1;
        } else if (!found) {
            this.openBest();
        }
    }

    public Set<MineTile> get_highlights(int min) {
        int[][] counts = new int[this.board.width][this.board.height];
        for (Minefield field : this.fields) {
            for (MineTile space : field.getSpaces()) {
                counts[space.x()][space.y()]++;
            }
        }
        Set<MineTile> tiles = new HashSet<>();
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