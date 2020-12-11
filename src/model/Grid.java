package model;

import java.util.*;

import static model.Constants.GRID_SIZE;

public class Grid {

    enum Type {
        WUMPUS,
        HERO,
        MAGE
    }

    private Cell[][] grid;

    public Grid(int rows, int columns) {
        if (rows % 3 != 0 || columns % 3 != 0) {
            System.out.printf("Invalid grid dimensions row: %d and col: %d\n", rows, columns);
            System.exit(1);
        }
        grid = new Cell[rows][columns];
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new Cell(new Coords(i, j));
            }
        }

        // Create computer pieces and place in Cell
        for (int j = 0; j < GRID_SIZE; j += 3) {
            createPieces(grid, 0, j, false);
        }

        // Create player pieces and place in Cell
        for (int j = 0; j < GRID_SIZE; j += 3) {
            createPieces(grid, Constants.GRID_SIZE - 1, j, true);
            updatProbability(grid, Constants.GRID_SIZE - 1, j, true);
        }

        // Create (d/3 - 1) pits randomly for each row in Grid
        Random rand = new Random();
        for (int i = 1; i < Constants.GRID_SIZE - 1; i++) {
            int numOfPits = (Constants.GRID_SIZE / 3) - 1;
            while (numOfPits > 0) {
                int randCol = rand.nextInt(GRID_SIZE);
                if (!grid[i][randCol].isPit()) {
                    grid[i][randCol].setIsPit(true);
                    numOfPits--;
                }
            }
        }
    }

    private void updatProbability(Cell[][] grid, int row, int col, boolean isPlayer) {
        grid[row][col].updatePW(100.0);
        grid[row][col + 1].updatePH(100.0);
        grid[row][col + 2].updatePM(100.0);

    }

    public void createPieces(Cell[][] grid, int row, int col, boolean isPlayer) {
        grid[row][col].setPiece(new Wumpus(isPlayer));
        grid[row][col + 1].setPiece(new Hero(isPlayer));
        grid[row][col + 2].setPiece(new Mage(isPlayer));
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public void setGrid(Cell[][] grid) {
        this.grid = grid;
    }

    public boolean executeMove(Coords start, Coords target) {
        Cell currCell = grid[start.getRow()][start.getColumn()];
        Cell targetCell = grid[target.getRow()][target.getColumn()];
        if (currCell.getPiece() == null) {
            System.out.println("No piece found at the starting location.");
            return false;
        }
        if (!currCell.getPiece().isPlayer()) {
            System.out.println("Selected piece is not a player piece.");
            return false;
        }
        if (currCell.isValidMove(targetCell) && (targetCell.isPit() || currCell.getPiece().diesTo(targetCell.getPiece()))) {
            currCell.setPiece(null);
        } else if (currCell.isValidMove(targetCell) && currCell.getPiece().isSameType(targetCell.getPiece())) {
            currCell.setPiece(null);
            targetCell.setPiece(null);
        } else if (currCell.isValidMove(targetCell)) {
            targetCell.setPiece(currCell.getPiece());
            currCell.setPiece(null);
        } else {
            return false;
        }
        return true;
    }

    public String getGameStatus(Cell[][] state) {
        int playerPieces = 0, computerPieces = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j].getPiece() != null) {
                    if (state[i][j].getPiece().isPlayer()) {
                        playerPieces++;
                    } else {
                        computerPieces++;
                    }
                }
            }
        }
        if (computerPieces == 0 && playerPieces > computerPieces) {
            return "Player Wins!";
        } else if (playerPieces == 0 && computerPieces > playerPieces) {
            return "CPU Wins!";
        } else if (computerPieces == 0 && playerPieces == 0) {
            return "Draw!";
        } else {
            return "Game not finished";
        }
    }

    public String getGameStatus() {
        return getGameStatus(grid);
    }

    public boolean isTerminalState(Cell[][] state) {
        int playerPieces = 0, computerPieces = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j].getPiece() != null) {
                    if (state[i][j].getPiece().isPlayer()) {
                        playerPieces++;
                    } else {
                        computerPieces++;
                    }
                }
            }
        }
        return playerPieces == 0 || computerPieces == 0;
    }

    /**
     * Since only the computer player is using the minimax algorithm, this will
     * take the perspective of the max player. The evaluation metric will the
     * number of computer pieces subtracted by the number of user pieces on the
     * board. Max value would be d for a computer win and -d for a computer
     * loss. According to the professor, having a different evaluation metric
     * for terminal states compared to heuristic values can lead to better
     * results (Piazza).
     */
    public int getEvaluationMetric(Cell[][] state) {
        int playerPieces = 0, computerPieces = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j].getPiece() != null) {
                    if (state[i][j].getPiece().isPlayer()) {
                        playerPieces++;
                    } else {
                        computerPieces++;
                    }
                }
            }
        }
        return computerPieces - playerPieces;
    }

    public int getAdvancedHeuristicValue(Cell[][] state) {
        int myWumps = 0;
        int enemyWumps = 0;
        int myHeroes = 0;
        int enemyHeroes = 0;
        int myMages = 0;
        int enemyMages = 0;
        double myScore = 0;
        double enemyScore = 0;

        if (isTerminalState(state)) {
            String gameStatus = getGameStatus(state);
            if (gameStatus.equals("Player Wins!")) {
                return -1000;
            } else if (gameStatus.equals("CPU Wins!")) {
                return 1000;
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j].getPiece() != null) {
                    if (state[i][j].getPiece() instanceof Wumpus) {
                        if (state[i][j].getPiece().isPlayer()) {
                            myWumps++;
                        } else {
                            enemyWumps++;
                        }
                    } else if (state[i][j].getPiece() instanceof Hero) {
                        if (state[i][j].getPiece().isPlayer()) {
                            myHeroes++;
                        } else {
                            enemyHeroes++;
                        }
                    } else if (state[i][j].getPiece() instanceof Mage) {
                        if (state[i][j].getPiece().isPlayer()) {
                            myMages++;
                        } else {
                            enemyMages++;
                        }
                    }
                }
            }
        }

        double myWumpScore = (calcUnitValue(Type.WUMPUS, myWumps, myHeroes, myMages) + Constants.LIFE_POINTS) * myWumps;
        double myHeroScore = (calcUnitValue(Type.HERO, myWumps, myHeroes, myMages) + Constants.LIFE_POINTS) * myHeroes;
        double myMageScore = (calcUnitValue(Type.MAGE, myWumps, myHeroes, myMages) + Constants.LIFE_POINTS) * myMages;
        myScore = myScore + myMageScore + myWumpScore + myHeroScore;

        double enemyWumpScore = (calcUnitValue(Type.WUMPUS, enemyWumps, enemyHeroes, enemyMages) + Constants.LIFE_POINTS) * enemyWumps;
        double enemyHeroScore = (calcUnitValue(Type.HERO, enemyWumps, enemyHeroes, enemyMages) + Constants.LIFE_POINTS) * enemyHeroes;
        double enemyMageScore = (calcUnitValue(Type.MAGE, enemyWumps, enemyHeroes, enemyMages) + Constants.LIFE_POINTS) * enemyMages;
        enemyScore = enemyScore + enemyMageScore + enemyWumpScore + enemyHeroScore;

        return (int) (enemyScore - myScore);
    }

    // Calculates unit value by the unit potential that depends on the enemy unit types and quantity.
    // If a the enemy units counter the current unit, it will get a lower score.
    private double calcUnitValue(Type unitType, int wumps, int heroes, int mages) {
        int kp = Constants.KILL_POINTS;
        double divider = 4.0;

        if (mages == 0 || heroes == 0 || wumps == 0) {
            divider = 1.0;
            // System.out.println("Divider changed");
        }

        switch (unitType) {
            case WUMPUS:
                return (mages * kp - heroes * kp) / divider;
            case HERO:
                return (wumps * kp + -mages * kp) / divider;
            case MAGE:
                return (heroes * kp + -wumps * kp) / divider;
            default:
                return 0;
        }
    }

    public List<Cell[][]> getAllPossibleChildNodes(Cell[][] state, boolean isMaxPlayer) {
        List<Cell[][]> allPossibleStates = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j].getPiece() != null && !state[i][j].getPiece().isPlayer() == isMaxPlayer) {
                    allPossibleStates.addAll(getAllPossibleMoves(state, i, j));
                }
            }
        }
        return allPossibleStates;
    }

    public Cell[][] getCopyOfState(Cell[][] state) {
        Cell[][] copy = new Cell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                copy[i][j] = state[i][j].getCopy();
            }
        }
        return copy;
    }

    public Cell[][] processMove(Cell[][] state, int startRow, int startCol, int targetRow, int targetCol) {
        Cell currCell = state[startRow][startCol];
        Cell targetCell = state[targetRow][targetCol];
        if (currCell.isValidMove(targetCell) && (targetCell.isPit() || currCell.getPiece().diesTo(targetCell.getPiece()))) {
            currCell.setPiece(null);
        } else if (currCell.isValidMove(targetCell) && currCell.getPiece().isSameType(targetCell.getPiece())) {
            currCell.setPiece(null);
            targetCell.setPiece(null);
        } else if (currCell.isValidMove(targetCell)) {
            targetCell.setPiece(currCell.getPiece());
            currCell.setPiece(null);
        }
        return state;
    }

    public List<Cell[][]> getAllPossibleMoves(Cell[][] state, int row, int col) {
        List<Cell[][]> allPossibleMoves = new ArrayList<>();
        Cell currCell = state[row][col];
        if (row > 0 && col >= 0 && col < GRID_SIZE) {
            Cell targetCell = state[row - 1][col];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row - 1, col));
            }
        }
        if (row > 0 && col > 0) {
            Cell targetCell = state[row - 1][col - 1];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row - 1, col - 1));
            }
        }
        if (row > 0 && col < GRID_SIZE - 1) {
            Cell targetCell = state[row - 1][col + 1];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row - 1, col + 1));
            }
        }
        if (row < GRID_SIZE - 1 && col >= 0 && col < GRID_SIZE) {
            Cell targetCell = state[row + 1][col];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row + 1, col));
            }
        }
        if (row < GRID_SIZE - 1 && col > 0) {
            Cell targetCell = state[row + 1][col - 1];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row + 1, col - 1));
            }
        }
        if (row < GRID_SIZE - 1 && col < GRID_SIZE - 1) {
            Cell targetCell = state[row + 1][col + 1];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row + 1, col + 1));
            }
        }
        if (col > 0 && row >= 0 && row < GRID_SIZE) {
            Cell targetCell = state[row][col - 1];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row, col - 1));
            }
        }
        if (col < GRID_SIZE - 1 && row >= 0 && row < GRID_SIZE) {
            Cell targetCell = state[row][col + 1];
            if (currCell.isValidMove(targetCell)) {
                allPossibleMoves.add(processMove(getCopyOfState(state), row, col, row, col + 1));
            }
        }
        return allPossibleMoves;
    }

    public State minimax(Cell[][] node, int depth, int maxDepth, boolean isMaxPlayer) {
        if (depth == maxDepth || isTerminalState(node)) {
            return new State(getAdvancedHeuristicValue(node), node);
        }
        State bestState = null;
        if (isMaxPlayer) {
            bestState = new State(Integer.MIN_VALUE, null);
            // Gets all possible child nodes.
            List<Cell[][]> listOfChildNodes = getAllPossibleChildNodes(node, true);
            // Create a priority queue that prioritizes nodes with higher H value.
            PriorityQueue<Cell[][]> pq = new PriorityQueue<>(listOfChildNodes.size(), Collections.reverseOrder((c1, c2) -> {
                return Double.compare(getAdvancedHeuristicValue(c1), getAdvancedHeuristicValue(c2));
            }));
            // Adds all possible child nodes to the priority queue.
            listOfChildNodes.forEach(childState -> {
                pq.add(childState);
            });

            while (!pq.isEmpty()) {
                Cell[][] child = pq.poll();
                State state = minimax(child, depth + 1, maxDepth, false);
                if (bestState == null) {
                    bestState.state = child;
                } else if (state.heuristicValue > bestState.heuristicValue) {
                    bestState.heuristicValue = state.heuristicValue;
                    bestState.state = child;
                }
            }
        } else {
            bestState = new State(Integer.MAX_VALUE, null);
            // Gets all possible child nodes.
            List<Cell[][]> listOfChildNodes = getAllPossibleChildNodes(node, false);
            // Create a priority queue that prioritizes nodes with higher H value.
            PriorityQueue<Cell[][]> pl = new PriorityQueue<>(listOfChildNodes.size(), (c1, c2) -> {
                return Double.compare(getAdvancedHeuristicValue(c1), getAdvancedHeuristicValue(c2));
            });
            // Adds all possible child nodes to the priority queue.
            listOfChildNodes.forEach(childState -> {
                pl.add(childState);
            });

            while (!pl.isEmpty()) {
                Cell[][] child = pl.poll();
                State state = minimax(child, depth + 1, maxDepth, true);
                if (bestState == null) {
                    bestState.state = child;
                } else if (state.heuristicValue < bestState.heuristicValue) {
                    bestState.heuristicValue = state.heuristicValue;
                    bestState.state = child;
                }
            }
        }
        return bestState;
    }

    public State alphaBetaMiniMax(Cell[][] node, int depth, int alpha, int beta, int maxDepth, boolean isMaxPlayer) {
        if (depth == maxDepth || isTerminalState(node)) {
            return new State(getAdvancedHeuristicValue(node), node);
        }
        State bestState = null;
        if (isMaxPlayer) {
            bestState = new State(Integer.MIN_VALUE, null);
            // Gets all possible child nodes.
            List<Cell[][]> listOfChildNodes = getAllPossibleChildNodes(node, true);
            // Create a priority queue that prioritizes nodes with higher H value.
            PriorityQueue<Cell[][]> pq = new PriorityQueue<>(listOfChildNodes.size(), Collections.reverseOrder((c1, c2) -> {
                return Double.compare(getAdvancedHeuristicValue(c1), getAdvancedHeuristicValue(c2));
            }));
            // Adds all possible child nodes to the priority queue.
            listOfChildNodes.forEach(childState -> {
                pq.add(childState);
            });

            while (!pq.isEmpty()) {
                Cell[][] child = pq.poll();
                State state = alphaBetaMiniMax(child, depth + 1, alpha, beta, maxDepth, false);
                if (bestState == null) {
                    bestState.state = child;
                } else if (state.heuristicValue > bestState.heuristicValue) {
                    bestState.heuristicValue = state.heuristicValue;
                    bestState.state = child;
                }
                alpha = Math.max(alpha, state.heuristicValue);
                if (beta <= alpha) {
                    break;
                }
            }

        } else {
            bestState = new State(Integer.MAX_VALUE, null);
            // Gets all possible child nodes.
            List<Cell[][]> listOfChildNodes = getAllPossibleChildNodes(node, false);
            // Create a priority queue that prioritizes nodes with higher H value.
            PriorityQueue<Cell[][]> pl = new PriorityQueue<>(listOfChildNodes.size(), (c1, c2) -> {
                return Double.compare(getAdvancedHeuristicValue(c1), getAdvancedHeuristicValue(c2));
            });
            // Adds all possible child nodes to the priority queue.
            listOfChildNodes.forEach(childState -> {
                pl.add(childState);
            });

            while (!pl.isEmpty()) {
                Cell[][] child = pl.poll();
                State state = alphaBetaMiniMax(child, depth + 1, alpha, beta, maxDepth, true);
                if (bestState == null) {
                    bestState.state = child;
                } else if (state.heuristicValue < bestState.heuristicValue) {
                    bestState.heuristicValue = state.heuristicValue;
                    bestState.state = child;
                }
                beta = Math.min(beta, state.heuristicValue);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestState;
    }

    /**
     * Compare the previous board state with the next board state to find the
     * move that the computer made.
     *
     * @param currentState The current state of the board
     * @param nextState The state of the board after the computer makes their
     * move
     * @return A string value representing the computer's move
     */
    @Deprecated
    public String findMove(Cell[][] currentState, Cell[][] nextState) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (currentState[row][col].getPiece() != null && nextState[row][col].getPiece() == null) {
                    Piece piece = currentState[row][col].getPiece();
                    StringBuilder sb = new StringBuilder();
                    sb.append((char) (col + 97));
                    sb.append((char) (row + 49));
                    sb.append(" ");
                    for (int i = 0; i < GRID_SIZE; i++) {
                        for (int j = 0; j < GRID_SIZE; j++) {
                            Piece nextStatePiece = nextState[i][j].getPiece();
                            if (piece.isSameType(nextStatePiece) && piece.isPlayer() == nextStatePiece.isPlayer()
                                    && (currentState[i][j].getPiece() == null || nextStatePiece.canAttack(currentState[i][j].getPiece()))) {
                                sb.append((char) (j + 97));
                                sb.append((char) (i + 49));
                                return sb.toString();
                            }
                        }
                    }
                }
            }
        }
        return "No computer move was found.";
    }
}
