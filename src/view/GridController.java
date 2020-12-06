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
    transient Button submitButton;

    private Grid grid;

    public void start(Grid grid) {
        this.grid = grid;
        refreshGrid();
    }

    public void refreshGrid() {
        Cell[][] board = grid.getGrid();
        gridPane.getChildren().clear();
        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell currCell = board[row][col];
                currCell.getChildren().clear();
                currCell.setMinWidth(CELL_SIZE);
                currCell.setMinHeight(CELL_SIZE);
                Tooltip toolTip = new Tooltip(String.valueOf((char) (col + 65)) + "" + (row + 1));
                Tooltip.install(currCell, toolTip);
                Text text = null;
                if (currCell.getPiece() != null) {
                    if (currCell.getPiece() instanceof Wumpus) {
                        text = new Text("Wumpus");
                    } else if (currCell.getPiece() instanceof Hero) {
                        text = new Text("Hero");
                    } else if (currCell.getPiece() instanceof Mage) {
                        text = new Text("Mage");
                    }
                    Color color = currCell.getPiece().isPlayer() ? Color.BLUE : Color.RED;
                    text.setFill(color);
                    currCell.getChildren().addAll(new Rectangle(), text);
                } else if (!currCell.isPit()) {
                    text = new Text(String.valueOf((char) (col + 65)) + "" + (row + 1));
                    Color color = Color.BLACK;
                    text.setFill(color);
                    currCell.getChildren().addAll(new Rectangle(), text);
                }
                if (currCell.isPit()) {
                    text = new Text("Pit");
                    text.setStyle("-fx-font-weight: bold");
                    currCell.getChildren().addAll(new Rectangle(), text);
                }
                GridPane.setRowIndex(currCell, row);
                GridPane.setColumnIndex(board[row][col], col);
                currCell.getStyleClass().add("cell");
                gridPane.getChildren().addAll(currCell);
            }
        }
        gridPane.getStyleClass().add("grid");
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
                    if(grid.isTerminalState(grid.getGrid())) {
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
                    if(grid.isTerminalState(grid.getGrid())) {
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
}
