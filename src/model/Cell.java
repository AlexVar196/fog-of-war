package model;

import javafx.scene.layout.StackPane;

public class Cell extends StackPane {

    private Coords coords;
    private Piece piece;
    private boolean isPit;
    private boolean isPlayesObservedStench;
    private boolean isPlayerObservedNoise;
    private boolean isPlayerObservedHeat;
    private boolean isPlayerObservedBreeze;
    private boolean isComputerObservedStench;
    private boolean isComputerObservedNoise;
    private boolean isComputerObservedHeat;
    private boolean isComputerObservedBreeze;

    private double pW = 0.0;
    private double pH = 0.0;
    private double pM = 0.0;
    private double pP = 0.0;

    public Cell(Coords coords) {
        this.coords = coords;
    }

    public void setPiece(Piece p) {
        this.piece = p;
    }

    public Piece getPiece() {
        return this.piece;
    }

    public void updatePW(double p) {
        this.pW = p;
    }

    public void updatePH(double p) {
        this.pH = p;
    }

    public void updatePM(double p) {
        this.pM = p;
    }

    public void updatePP(double p) {
        this.pP = p;
    }

    public double getPW() {
        return this.pW;
    }

    public double getPH() {
        return this.pH;
    }

    public double getPM() {
        return this.pM;
    }

    public double getPP() {
        return this.pP;
    }

    public boolean isPit() {
        return this.isPit;
    }

    public void setIsPit(boolean isPit) {
        this.isPit = isPit;
    }

    public boolean isHeat() {
        return this.isPlayerObservedHeat;
    }

    public void setIsHeat(boolean isHeat) {
        this.isPlayerObservedHeat = isHeat;
    }

    public boolean isCPUHeat() {
        return this.isComputerObservedHeat;
    }

    public void setIsCPUHeat(boolean isHeat) {
        this.isComputerObservedHeat = isHeat;
    }

    public boolean isCPUStench() {
        return this.isComputerObservedStench;
    }

    public void setIsCPUStench(boolean isStench) {
        this.isComputerObservedStench = isStench;
    }

    public boolean isCPUNoise() {
        return this.isComputerObservedNoise;
    }

    public void setIsCPUNoise(boolean isNoise) {
        this.isComputerObservedNoise = isNoise;
    }

    public boolean isStench() {
        return this.isPlayesObservedStench;
    }

    public void setIsStench(boolean isStench) {
        this.isPlayesObservedStench = isStench;
    }

    public boolean isNoise() {
        return this.isPlayerObservedNoise;
    }

    public void setIsNoise(boolean isNoise) {
        this.isPlayerObservedNoise = isNoise;
    }

    public boolean isCPUBreeze() {
        return this.isComputerObservedBreeze;
    }

    public void setIsCPUBreeze(boolean isBreeze) {
        this.isComputerObservedBreeze = isBreeze;
    }

    public boolean isBreeze() {
        return this.isPlayerObservedBreeze;
    }

    public void setIsBreeze(boolean isBreeze) {
        this.isPlayerObservedBreeze = isBreeze;
    }

    public Coords getCoords() {
        return this.coords;
    }

    public boolean isValidMove(Cell target) {
        int sourceRow = this.coords.getRow();
        int sourceCol = this.coords.getColumn();
        int targetRow = target.getCoords().getRow();
        int targetCol = target.getCoords().getColumn();
        if (piece == null) {
            return false;
        } else {
            boolean validColumn = targetCol == sourceCol || targetCol == sourceCol + 1 || targetCol == sourceCol - 1;
            boolean validRow = targetRow == sourceRow || targetRow == sourceRow + 1 || targetRow == sourceRow - 1;
            if (validRow && validColumn && !coords.equals(target.coords)) {
                Piece targetPiece = target.getPiece();
                if (targetPiece == null || piece.canAttack(targetPiece) || piece.diesTo(targetPiece)) {
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
        if (piece != null) {
            copy.setPiece(piece.getCopy());
        }
        copy.setIsPit(isPit);
        return copy;
    }

    public void resetPlayerObservations() {
        this.isPlayerObservedHeat = false;
        this.isPlayerObservedNoise = false;
        this.isPlayesObservedStench = false;
    }

    public void resetComputerObservations() {
        this.isComputerObservedHeat = false;
        this.isComputerObservedNoise = false;
        this.isComputerObservedStench = false;
    }

    public void resetProbabilities() {
        this.pH = 0.0;
        this.pW = 0.0;
        this.pM = 0.0;
        this.pW = 0.0;
    }

}
