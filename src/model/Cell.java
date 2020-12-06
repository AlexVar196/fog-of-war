package model;

import javafx.scene.layout.StackPane;

public class Cell extends StackPane {

    private Coords coords;
    private Piece piece;
    private boolean isPit;

    public Cell(Coords coords) {
        this.coords = coords;
    }

    public void setPiece(Piece p) {
        this.piece = p;
    }

    public Piece getPiece() {
        return this.piece;
    }

    public boolean isPit() {
        return this.isPit;
    }

    public void setIsPit(boolean isPit) {
        this.isPit = isPit;
    }

    public Coords getCoords() {
        return this.coords;
    }

    public boolean isValidMove(Cell target) {
        int sourceRow = this.coords.getRow();
        int sourceCol = this.coords.getColumn();
        int targetRow = target.getCoords().getRow();
        int targetCol = target.getCoords().getColumn();
        if(piece == null) {
            return false;
        } else {
            boolean validColumn = targetCol == sourceCol || targetCol == sourceCol + 1 || targetCol == sourceCol - 1;
            boolean validRow = targetRow == sourceRow || targetRow == sourceRow + 1 || targetRow == sourceRow - 1;
            if(validRow && validColumn && !coords.equals(target.coords)) {
                Piece targetPiece = target.getPiece();
                if(targetPiece == null || piece.canAttack(targetPiece) || piece.diesTo(targetPiece)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public Cell getCopy() {
        Cell copy = new Cell(coords.getCopy());
        if(piece != null) {
            copy.setPiece(piece.getCopy());
        }
        copy.setIsPit(isPit);
        return copy;
    }
}
