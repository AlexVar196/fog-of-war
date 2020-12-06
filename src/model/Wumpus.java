package model;

public class Wumpus extends Piece {

    public Wumpus(boolean isHumanPlayer) {
        super(isHumanPlayer);
    }

    @Override
    public boolean canAttack(Piece p) {
        return (p instanceof Mage || p instanceof Wumpus) && (isPlayer() != p.isPlayer());
    }
    @Override
    public boolean diesTo(Piece p){
        return p instanceof Hero && (isPlayer() != p.isPlayer());
    }
    @Override
    public Piece getCopy() {
        return new Wumpus(isPlayer());
    }

    @Override
    public String getName() {
        return "Wumpus";
    }
}
