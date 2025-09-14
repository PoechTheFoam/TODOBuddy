package org.example.smarttodolist;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class smartTodoList extends Application {
    public static void main(String[] args){
        launch();
    }
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader=new FXMLLoader(getClass().getResource("smartTodoList.fxml"));
        Parent root= loader.load();
        Scene scene=new Scene(root);
        stage.setTitle("TODO Buddy");
        Image icon=new Image("todobuddy.png");
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();
        controller controller=loader.getController();
        controller.stage=stage;
        stage.setOnCloseRequest(e->{
            e.consume();
            controller.close();});
    }
}
