package app;

import java.awt.Dimension;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Constants;
import model.Grid;
import view.GridController;

import static model.Constants.GRID_SIZE;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        Stage mainStage = new Stage();
        loader.setLocation(getClass().getResource("../view/Grid.fxml"));
        AnchorPane root = (AnchorPane) loader.load();
        GridController gridController = null;
        gridController = loader.getController();

        Grid grid = new Grid(Constants.GRID_SIZE, GRID_SIZE);
        gridController.start(grid);
        loader.setController(gridController);

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth() / 2;
        double height = screenSize.getHeight() * 0.90;
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add("/view/stylesheets/Grid.css");
        mainStage.setScene(scene);
        mainStage.setResizable(true);
        mainStage.setTitle("AdversarialSearch");
        mainStage.show();
    }

    /**
     * The main method that launches the AdversarialSearch application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
