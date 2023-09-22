package minesweeper;
/**
 * MineTile
 */
public record MineTile(int x, int y) {
    public int x() {
        return this.x;
    }
    public int y() {
        return this.y;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
    public String toCode() {
        return "new MineTile(" + this.x + ", " + this.y + ")";
    }
}