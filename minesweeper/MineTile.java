package minesweeper;
/**
 * MineTile
 */
public class MineTile {

    public int x;
    public int y;

    public MineTile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return this.y ^ (this.x << 16);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof MineTile)) {
            return false;
        }
        MineTile other_tile = (MineTile) other;
        return other_tile.x == this.x && other_tile.y == this.y;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
    public String toCode() {
        return "new MineTile(" + this.x + ", " + this.y + ")";
    }
}