module org.example.smarttodolist {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.smarttodolist to javafx.fxml;
    exports org.example.smarttodolist;
}