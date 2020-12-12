package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.*;
import model.Cell;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.scene.paint.Paint;

import static model.Constants.CELL_SIZE;
import static model.Constants.GRID_SIZE;

public class GridController implements Serializable {

    @FXML
    transient Label gameStatusLabel;

    @FXML
    transient TextArea outputTextArea;

    @FXML
    transient GridPane gridPane;

    @FXML
    transient TextField inputTextField;

    @FXML
    transient Label fogOfWarToggleText;

    @FXML
    transient Button submitButton;

    private Grid grid;
    private Boolean fogOfWar = true;

    public void start(Grid grid) {
        this.grid = grid;
        refreshGrid();
    }

    public void refreshGrid() {
        if (fogOfWar) {
            fogOfWarToggleText.setText("ON");
            Color color = Color.GREEN;
            fogOfWarToggleText.setTextFill(color);
        } else {
            fogOfWarToggleText.setText("OFF");
            Color color = Color.RED;
            fogOfWarToggleText.setTextFill(color);
        }
        Cell[][] board = grid.getGrid();
        gridPane.getChildren().clear();

        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell currCell = board[row][col];
                currCell.getChildren().clear();
                currCell.setMinWidth(CELL_SIZE);
                currCell.setMinHeight(CELL_SIZE);
                Text text = null;

                if (currCell.getPiece() != null) {
                    if (!currCell.getPiece().isPlayer()) {
                        if (currCell.getPiece() instanceof Wumpus) {
                            if (!fogOfWar) {
                                text = new Text("Wumpus");
                            } else {
                                text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                            }
                            addPlayerObservations(board, currCell, 'w');
                            currCell.updatePW(100.0);
                        } else if (currCell.getPiece() instanceof Hero) {
                            if (!fogOfWar) {
                                text = new Text("Hero");
                            } else {
                                text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                            }
                            addPlayerObservations(board, currCell, 'h');
                            currCell.updatePH(100.0);
                        } else if (currCell.getPiece() instanceof Mage) {
                            if (!fogOfWar) {
                                text = new Text("Mage");
                            } else {
                                text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                            }
                            addPlayerObservations(board, currCell, 'm');
                            currCell.updatePM(100.0);
                        }
                    } else {
                        if (currCell.getPiece() instanceof Wumpus) {
                            text = new Text("Wumpus");
                            addComputerObservations(board, currCell, 'w');
                        } else if (currCell.getPiece() instanceof Hero) {
                            text = new Text("Hero");
                            addComputerObservations(board, currCell, 'h');
                        } else if (currCell.getPiece() instanceof Mage) {
                            text = new Text("Mage");
                            addComputerObservations(board, currCell, 'm');
                        }
                    }

                    if (!fogOfWar) {
                        Color color = currCell.getPiece().isPlayer() ? Color.BLUE : Color.RED;
                        text.setFill(color);
                    } else {
                        Color color = currCell.getPiece().isPlayer() ? Color.BLUE : Color.BLACK;
                        text.setFill(color);
                    }
                    currCell.getChildren().addAll(new Rectangle(), text);
                } else if (currCell.isPit()) {
                    if (!fogOfWar) {
                        text = new Text("Pit");
                        text.setStyle("-fx-font-weight: bold");
                    } else {
                        text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                    }
                    addComputerObservations(board, currCell, 'p');
                    addPlayerObservations(board, currCell, 'p');

                    currCell.getChildren().addAll(new Rectangle(), text);

                } else if (!currCell.isPit()) {
                    text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                    Color color = Color.BLACK;
                    text.setFill(color);
                    currCell.getChildren().addAll(new Rectangle(), text);
                } else {
                    currCell.resetPlayerObservations();
                    currCell.resetComputerObservations();
                }

                GridPane.setRowIndex(currCell, row);
                GridPane.setColumnIndex(board[row][col], col);
                currCell.getStyleClass().add("cell");
                gridPane.getChildren().addAll(currCell);
            }
        }

        calculateProbabilities(board);
        updateToolTip(board);
        gridPane.getStyleClass().add("grid");
    }

    private void calculateProbabilities(Cell[][] board) {
        updatePitProbability(board);

    }

    private void updatePitProbability(Cell[][] board) {
        List<Cell> nonPlayerCells = new ArrayList<>();
        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell currCell = board[row][col];
                if (currCell.getPiece() == null) {
                    nonPlayerCells.add(currCell);
                }
            }
        }
        double numOfPits = ((Constants.GRID_SIZE / 3) - 1) * (GRID_SIZE - 2);
        double numberOfEmptyCells = nonPlayerCells.size();
        double p = (numOfPits / numberOfEmptyCells) * 100;

        BigDecimal bd = new BigDecimal(p).setScale(3, RoundingMode.HALF_UP);
        double roundedP = bd.doubleValue();

        for (Cell cell : nonPlayerCells) {
            cell.updatePP(roundedP);
        }
    }

    private String getCellObservations(Cell currCell) {
        int col = currCell.getCoords().getColumn();
        int row = currCell.getCoords().getRow();
        String stench = "";
        String noise = "";
        String heat = "";
        String breeze = "";
        Boolean changed = false;
        String finalString = "";

        List<String> observations = new ArrayList<>();

        if (currCell.isStench()) {
            stench = "Stench";
            observations.add(stench);
            changed = true;
        }
        if (currCell.isNoise()) {
            noise = "Noise";
            observations.add(noise);
            changed = true;
        }
        if (currCell.isHeat()) {
            heat = "Heat";
            observations.add(heat);
            changed = true;
        }
        if (currCell.isBreeze()) {
            breeze = "Breeze";
            observations.add(breeze);
            changed = true;
        }

        if (changed) {
            String observationsText = "";
            for (int i = 0; i < observations.size(); i++) {
                if (i != observations.size()-1) {
                    observationsText += (observations.get(i) + ", ");
                } else {
                    observationsText += (observations.get(i) + ". ");
                }
            }
            
            System.err.println(observationsText);
            finalString = "Cell: " + String.valueOf((char) (col + 65)) + "-" + (row + 1) + "\nObservations: " + observationsText;

        } else {
            if (currCell.getPiece() != null && !currCell.getPiece().isPlayer()) {
                finalString = "Cell: " + String.valueOf((char) (col + 65)) + "-" + (row + 1) + "\nObservations: None\nEmpty Cell: 100%";
            } else {
                finalString = "Cell: " + String.valueOf((char) (col + 65)) + "-" + (row + 1) + "\nObservations: None.";
            }
        }
        return finalString;
    }

    private String getProbabilityText(Cell currCell) {
        String probabilityText = "\nPW: " + currCell.getPW() + "%";
        probabilityText += "\nPH: " + currCell.getPH() + "%";
        probabilityText += "\nPM: " + currCell.getPM() + "%";
        probabilityText += "\nPP: " + currCell.getPP() + "%";

        return probabilityText;
    }

    private void updateToolTip(Cell[][] board) {
        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell currCell = board[row][col];

                if (currCell.getPiece() != null && currCell.getPiece().isPlayer()) {
                    String observationText = getCellObservations(currCell);
                    Tooltip toolTip = new Tooltip(observationText);
                    Tooltip.install(currCell, toolTip);

//                    var surroundingCells = getSurroundingCells(board, currCell);
//                    for (Cell cell : surroundingCells) {
//                        String surroundingObservationText = getCellObservations(cell);
//                        String surroundingProbabilityText = getProbabilityText(cell);
//                        String finalTooltipText = surroundingObservationText + surroundingProbabilityText;
//
//                        System.out.println(finalTooltipText);
//
//                        Tooltip surroundingToolTip = new Tooltip(finalTooltipText);
//                        Tooltip.install(cell, surroundingToolTip);
//                    }
                } else {
                    String cellText = "Cell: " + String.valueOf((char) (col + 65)) + "-" + (row + 1);
                    String probabilityText = getProbabilityText(currCell);
                    String finalTooltipText = cellText + probabilityText;

                    Tooltip toolTip = new Tooltip(finalTooltipText);
                    Tooltip.install(currCell, toolTip);
                }
            }
        }
    }

    public void toggleFogOfWar() {
        if (fogOfWar) {
            fogOfWar = false;
        } else {
            fogOfWar = true;
        }
        refreshGrid();
    }

    private void diplayBoardLogs() {
        Cell[][] board = grid.getGrid();
        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell currCell = board[row][col];
                System.out.println("Cell " + currCell.getCoords().toString() + "\nstench: " + currCell.isStench() + "\nnoise: " + currCell.isNoise() + "\nheat: " + currCell.isHeat() + "\nbreeze: " + currCell.isBreeze() + "\n\n");
            }
        }
    }

    @FXML
    public void submitMove() {
        String[] input = inputTextField.getText().toLowerCase().trim().split(" ");
        inputTextField.setText("");
        if (input.length != 2) {
            System.out.printf("Invalid number of arguments: %d\n", input.length);
        } else if (input[0].length() != 2 || input[1].length() != 2) {
            System.out.printf("Invalid argument lengths: '%s' and '%s'\n", input[0], input[1]);
        } else {
            // Convert ASCII values to integers for columns and rows
            int startCol = input[0].charAt(0) - 97;
            int startRow = input[0].charAt(1) - 49;
            int endCol = input[1].charAt(0) - 97;
            int endRow = input[1].charAt(1) - 49;
            if (startCol < 0 || startCol >= GRID_SIZE
                    || startRow < 0 || startRow >= GRID_SIZE
                    || endCol < 0 || endCol >= GRID_SIZE
                    || endRow < 0 || endRow >= GRID_SIZE) {
                String output = String.format("Invalid arguments: '%s' and '%s'\n", input[0], input[1]);
                output += String.format("Start col, start row, end Col, end Row: %d, %d, %d, %d\n", startCol, startRow, endCol, endRow);
                outputTextArea.setText(output);
            } else {
                if (grid.executeMove(new Coords(startRow, startCol), new Coords(endRow, endCol))) {
                    String output = String.format("Human moved from [%s] to [%s]\n", input[0], input[1]);
                    refreshGrid();
                    if (grid.isTerminalState(grid.getGrid())) {
                        gameStatusLabel.setText(grid.getGameStatus());
                        submitButton.setDisable(true);
                        return;
                    }
                    int alpha = Integer.MIN_VALUE;
                    int beta = Integer.MAX_VALUE;

                    Cell[][] oldState = grid.getGrid();
                    long startTime2 = System.nanoTime();
                    //State bestMoveMiniMax = grid.minimax(grid.getGrid(), 0, Constants.MAX_DEPTH, true);
                    State bestMoveAlphaBeta = grid.alphaBetaMiniMax(grid.getGrid(), 0, alpha, beta, Constants.MAX_DEPTH, true);
                    long stopTime2 = System.nanoTime();
                    long durationAlphaBeta = stopTime2 - startTime2;
                    System.out.println("Execution time with depth " + Constants.MAX_DEPTH + ": " + TimeUnit.MILLISECONDS.convert(durationAlphaBeta, TimeUnit.NANOSECONDS) + " miliseconds");
                    //Cell[][] newState = bestMoveMiniMax.state;
                    Cell[][] newState = bestMoveAlphaBeta.state;
                    grid.setGrid(newState);
                    String move = findComputerMove(oldState, newState);
                    output += move;
                    outputTextArea.setText(output);
                    refreshGrid();
                    if (grid.isTerminalState(grid.getGrid())) {
                        gameStatusLabel.setText(grid.getGameStatus());
                        submitButton.setDisable(true);
                    }
                } else {
                    String output = String.format("Error in making move: %s %s\n", input[0], input[1]);
                    outputTextArea.setText(output);
                }
            }
        }
    }
    // Finds computer move by comparing old state and new state and finding the difference.

    private String findComputerMove(Cell[][] oldState, Cell[][] newState) {
        List<String> before = getComputerPosition(oldState);
        List<String> after = getComputerPosition(newState);
        List<String> temp = new ArrayList<>();
        temp.addAll(before);
        before.removeAll(after);
        after.removeAll(temp);

        return "CPU moved from " + before + " to " + after;
    }

    private List<String> getComputerPosition(Cell[][] state) {
        List<String> arr = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j].getPiece() != null && state[i][j].getPiece().isPlayer() == false) {
                    arr.add(String.valueOf((char) (i + 65)) + "" + (j + 1));

                }
            }
        }
        return arr;
    }

    private void addPlayerObservations(Cell[][] board, Cell currCell, char pieceType) {
        var surroundingCells = getSurroundingCells(board, currCell);
        switch (pieceType) {
            case 'w':
                for (Cell cell : surroundingCells) {
                    System.out.println("cell " + cell.getCoords().toString() + " Trying to add stentch");
                    cell.setIsStench(true);
                    System.out.println("Result at " + cell.getCoords().toString() + ": " + cell.isStench());

                }
                break;
            case 'h':
                for (Cell cell : surroundingCells) {
                    cell.setIsNoise(true);
                    System.out.println("cell " + cell.getCoords().toString() + " now has Noise");
                }
                break;
            case 'm':
                for (Cell cell : surroundingCells) {
                    cell.setIsHeat(true);
                    System.out.println("cell " + cell.getCoords().toString() + " now has Heat");
                }
                break;
            case 'p':
                for (Cell cell : surroundingCells) {
                    System.out.println("cell " + cell.getCoords().toString() + " Trying to add breeze");
                    cell.setIsBreeze(true);
                    System.out.println("Result at " + cell.getCoords().toString() + ": " + cell.isBreeze());
                }
                break;
        }
    }

    private void addComputerObservations(Cell[][] board, Cell currCell, char pieceType) {
        var surroundingCells = getSurroundingCells(board, currCell);
        switch (pieceType) {
            case 'w':
                for (Cell cell : surroundingCells) {
                    System.out.println("cell " + cell.getCoords().toString() + " Trying to add stentch");
                    cell.setIsCPUStench(true);
                    System.out.println("Result at " + cell.getCoords().toString() + ": " + cell.isStench());
                }
                break;
            case 'h':
                for (Cell cell : surroundingCells) {
                    cell.setIsCPUNoise(true);
                    System.out.println("cell " + cell.getCoords().toString() + " now has Noise");
                }
                break;
            case 'm':
                for (Cell cell : surroundingCells) {
                    cell.setIsCPUHeat(true);
                    System.out.println("cell " + cell.getCoords().toString() + " now has Heat");
                }
                break;
            case 'p':
                for (Cell cell : surroundingCells) {
                    System.out.println("cell " + cell.getCoords().toString() + " Trying to add breeze");
                    cell.setIsCPUBreeze(true);
                    System.out.println("Result at " + cell.getCoords().toString() + ": " + cell.isCPUBreeze());
                }
                break;
        }
    }

    private List<Cell> getSurroundingCells(Cell[][] board, Cell currCell) {

        List<Cell> allSurroundingCells = new ArrayList<>();
        int row = currCell.getCoords().getRow();
        int col = currCell.getCoords().getColumn();
        if (row > 0 && col >= 0 && col < GRID_SIZE) {
            Cell surroundingCell = board[row - 1][col];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        if (row > 0 && col > 0) {
            Cell surroundingCell = board[row - 1][col - 1];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        if (row > 0 && col < GRID_SIZE - 1) {
            Cell surroundingCell = board[row - 1][col + 1];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        if (row < GRID_SIZE - 1 && col >= 0 && col < GRID_SIZE) {
            Cell surroundingCell = board[row + 1][col];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        if (row < GRID_SIZE - 1 && col > 0) {
            Cell surroundingCell = board[row + 1][col - 1];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        if (row < GRID_SIZE - 1 && col < GRID_SIZE - 1) {
            Cell surroundingCell = board[row + 1][col + 1];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        if (col > 0 && row >= 0 && row < GRID_SIZE) {
            Cell surroundingCell = board[row][col - 1];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        if (col < GRID_SIZE - 1 && row >= 0 && row < GRID_SIZE) {
            Cell surroundingCell = board[row][col + 1];
            if (surroundingCell != null) {
                allSurroundingCells.add(surroundingCell);
            }
        }
        return allSurroundingCells;
    }
}
