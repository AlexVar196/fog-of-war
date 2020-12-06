package model;

public class Coords {

    private int column;
    private int row;

    public Coords(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getColumn() {
        return this.column;
    }

    public int getRow() {
        return this.row;
    }

    public boolean equals(Coords c) {
        return this.row == c.getRow() && this.column == c.getColumn();
    }

    public Coords getCopy() {
        return new Coords(this.row, this.column);
    }
}
