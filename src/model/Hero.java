package model;

public class Hero extends Piece {

    public Hero(boolean isHumanPlayer) {
        super(isHumanPlayer);
    }

    @Override
    public boolean canAttack(Piece p) {
        return (p instanceof Wumpus || p instanceof Hero) && (isPlayer() != p.isPlayer());
    }
    @Override
    public boolean diesTo(Piece p){
        return p instanceof Mage && (isPlayer() != p.isPlayer());
    }
    @Override
    public Piece getCopy() {
        return new Hero(isPlayer());
    }

    @Override
    public String getName() {
        return "Hero";
    }
}
