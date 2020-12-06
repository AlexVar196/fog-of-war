package model;

public abstract class Piece {

    private final boolean isHumanPlayer;

    public Piece(boolean isHumanPlayer) {
        this.isHumanPlayer = isHumanPlayer;
    }

    public abstract boolean canAttack(Piece p);
    public abstract boolean diesTo(Piece p);
    public abstract Piece getCopy();
    public abstract String getName();

    public boolean isPlayer() {
        return this.isHumanPlayer;
    }

    public boolean isSameType(Piece p) {
        return p != null && getClass().equals(p.getClass());
    }
}
