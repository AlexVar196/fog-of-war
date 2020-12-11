package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.*;
import model.Cell;

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
    private Boolean fogOfWar = false;

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
                        } else if (currCell.getPiece() instanceof Hero) {
                            if (!fogOfWar) {
                                text = new Text("Hero");
                            } else {
                                text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                            }
                            addPlayerObservations(board, currCell, 'h');
                        } else if (currCell.getPiece() instanceof Mage) {
                            if (!fogOfWar) {
                                text = new Text("Mage");
                            } else {
                                text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                            }
                            addPlayerObservations(board, currCell, 'm');
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
                } else if (!currCell.isPit()) {
                    text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                    Color color = Color.BLACK;
                    text.setFill(color);
                    currCell.getChildren().addAll(new Rectangle(), text);
                } else {
                    currCell.resetPlayerObservations();
                    currCell.resetComputerObservations();
                }
                if (currCell.isPit()) {
                    if (!fogOfWar) {
                        text = new Text("Pit");
                        text.setStyle("-fx-font-weight: bold");
                    } else {
                        text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                    }

                    currCell.getChildren().addAll(new Rectangle(), text);
                    addComputerObservations(board, currCell, 'p');
                    addPlayerObservations(board, currCell, 'p');
                }
                GridPane.setRowIndex(currCell, row);
                GridPane.setColumnIndex(board[row][col], col);
                currCell.getStyleClass().add("cell");
                gridPane.getChildren().addAll(currCell);
            }
        }

        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell currCell = board[row][col];
                String stench = "";
                String noise = "";
                String heat = "";

                if (currCell.isStench()) {
                    stench = "\nStench";
                }
                if (currCell.isNoise()) {
                    noise = "\nNoise";
                }
                if (currCell.isHeat()) {
                    heat = "\nHeat";
                }
                if (currCell.isPit()) {
                    heat = "\nBreeze";
                }

                Tooltip toolTip = new Tooltip("Move: " + String.valueOf((char) (col + 65)) + "-" + (row + 1) + stench + noise + heat);
                Tooltip.install(currCell, toolTip);
            }
        }
        gridPane.getStyleClass().add("grid");
        diplayBoardLogs();

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
