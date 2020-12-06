package model;

public class Mage extends Piece {

    public Mage(boolean isHumanPlayer) {
        super(isHumanPlayer);
    }

    @Override
    public boolean canAttack(Piece p) {
        return (p instanceof Hero || p instanceof Mage) && (isPlayer() != p.isPlayer());
    }
    @Override
    public boolean diesTo(Piece p){
        return p instanceof Wumpus && (isPlayer() != p.isPlayer());
    }
    @Override
    public Piece getCopy() {
        return new Mage(isPlayer());
    }

    @Override
    public String getName() {
        return "Mage";
    }
}
