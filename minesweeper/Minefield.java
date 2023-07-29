package minesweeper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Minefield
 */
public class Minefield {
    private Set<MineTile> spaces;
    private int mines;

    public Minefield(Set<MineTile> spaces, int mines) {
        this.spaces = spaces;
        this.mines = mines;
    }
    public boolean filled() {
        return this.spaces.size() == this.mines;
    }
    public boolean empty() {
        return this.mines == 0;
    }
    public boolean exists() {
        return this.spaces.size() > 0;
    }
    public Set<MineTile> getSpaces() {
        return this.spaces;
    }
    public double getChance() {
        return (double) this.mines/this.spaces.size();
    }
    public static ArrayList<Minefield> intersect(Minefield aField, Minefield bField) {
        Set<MineTile> cSpaces = new HashSet<>(aField.spaces);
        cSpaces.retainAll(bField.spaces);
        int cSize = cSpaces.size();
        int aSize = aField.spaces.size();
        int bSize = bField.spaces.size();

        int cMinesMin = cSize - Math.min(aSize - aField.mines, bSize - bField.mines);
        int cMinesMax = Math.min(Math.min(aField.mines, bField.mines), cSize);
        if (cMinesMin == cMinesMax && cSize > 0) {
            Set<MineTile> aSpaces = new HashSet<>(aField.spaces);
            aSpaces.removeAll(cSpaces);
            Set<MineTile> bSpaces = new HashSet<>(bField.spaces);
            bSpaces.removeAll(cSpaces);
            ArrayList<Minefield> fields = new ArrayList<>();
            fields.add(new Minefield(aSpaces, aField.mines - cMinesMin));
            fields.add(new Minefield(cSpaces, cMinesMin));
            fields.add(new Minefield(bSpaces, bField.mines - cMinesMin));
            return fields;
        }
        return new ArrayList<>();
    }
    public void filter(Function<MineTile, Boolean> decider, boolean invert) {
        Set<MineTile> newSpaces = new HashSet<>();
        for (MineTile space : this.spaces) {
            if (decider.apply(space) ^ invert) {
                newSpaces.add(space);
            }
        }
        this.spaces = newSpaces;
    }
    public void filter(Function<MineTile, Boolean> decider) {
        this.filter(decider, false);
    }
    public boolean touches(Minefield o) {
        return new HashSet<>(this.spaces).removeAll(o.spaces);
    }
    @Override
    public String toString() {
        return "Minefield(" + this.spaces + "-> "+this.spaces.size()+", " + this.mines + " mines, " + this.getChance() + " chance)";
    }
    public String toCode() {
        String spacesString = "";
        for (MineTile space : this.spaces) {
            spacesString += ",";
            spacesString += space.toCode();
        }
        spacesString = spacesString.length() == 0 ? "  " : spacesString;
        return "new Minefield(new HashSet<MineTile>(Arrays.asList(" + spacesString.substring(1) + ")), " + this.mines + ")";
    }
    @Override
    public int hashCode() {
        int tileHash = 0;
        for (MineTile space : this.spaces) {
            tileHash ^= space.hashCode();
            tileHash = Integer.rotateLeft(tileHash, 7);
        }
        return tileHash ^ mines;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Minefield)) {
            return false;
        }
        Minefield other_field = (Minefield) other;
        return this.spaces.equals(other_field.spaces) && other_field.mines == this.mines;
    }
}