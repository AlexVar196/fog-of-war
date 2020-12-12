package model;

public class Constants {

    // The value of d. GRID_SIZE must be a multiple of 3
    public static final int GRID_SIZE = 3;

    // Each Cell is a StackPane represented in the Grid (GridPane).
    // The Cell size determines the width/height of the StackPane in pixels.
    public static final int CELL_SIZE = 75;

    // Max depth of the tree to calculate. The max depth is equaled to "d" as required in the assignment,
    // but the constant will be kept since its name is clearer.
    //public static int MAX_DEPTH = GRID_SIZE;
    public static int MAX_DEPTH = 2;

    // The point value granted to a unit for being alive.
    public static int LIFE_POINTS = 5;

    // The point value granted for a unit for killing another unit.
    public static int KILL_POINTS = 15;
}
