package org.example.smarttodolist;

import javafx.animation.FadeTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class controller {
    @FXML
    private Pagination myPagination;
    @FXML
    private ChoiceBox<String> myChoiceBox;
    @FXML
    TableView<Task> myTable;
    @FXML
    TableColumn<Task,String> taskCol;
    @FXML
    TableColumn<Task,String> priCol;
    @FXML
    TableColumn<Task,String> effCol;
    @FXML
    TableColumn<Task,LocalDate> DLCol;
    @FXML
    TableColumn<Task,Void> delCol;
    @FXML
    TableColumn<Task,String> symbolCol;
    @FXML
    Label myLabel;
    @FXML
    MenuBar menuBar;
    @FXML
    MenuItem menuItemSave;
    @FXML
    CheckMenuItem checkMenuItemAutoSave;
    @FXML
    MenuItem menuItemUndo;
    @FXML
    MenuItem menuItemAdd;
    @FXML
    MenuItem menuItemClose;
    @FXML
    MenuItem menuItemHelp;
    @FXML
    CheckMenuItem priCheck;
    @FXML
    CheckMenuItem effCheck;
    @FXML
    MenuItem menuItemRedo;
    final taskManager taskManager=new taskManager();
    final taskStorage taskStorage=new taskStorage(taskManager);
    Stage stage;
    private Task task;
    private ArrayList<Task> current;
    private final int tasksPerPage=15;
    private final String[] views={"Unsorted","Simple Sort"};
    public void initialize(){
        menuItemHelp.setOnAction(e->{
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Legend");
            alert.setContentText("""
                    - Adding tasks: Use the Add Task button to enter a new task. (Users must fill out all fields)
                    
                    - Editing tasks: Double-click a cell to edit it. 
                    * For effort level & priority columns, they must be toggled for editing.
                    * For deadlines, you can either type out a date or choose it via the date picker.
                    
                    - Priority markers: 
                    + "!" = Low 
                    + "!!" = Medium 
                    + "!!!" = High 
                    + "?" = Unlisted
                    
                    - Effort highlights:
                    • Green = Low effort
                    • Yellow = Medium effort
                    • Red = High effort
                    • Blue = Unlisted
                    
                    - Undo/Redo: Restores the last saved version of your list. (current only one rollback)
                    
                    - Sorting: Use the Sort button to reorder tasks by your preferred order ("smart" by default).
                    
                    - Max tasks/page: 15
                    """);
            alert.setTitle("Quick Help");
            alert.show();
        });
        menuBar.setStyle("""
                -fx-font-family:".VnArial";
                -fx-font-weight:bold;
                """);
        taskStorage.load();
        menuItemSave.setOnAction(e->save());
        checkMenuItemAutoSave.setSelected(true);
        myChoiceBox.getItems().addAll(views);
        myChoiceBox.setValue(views[1]);
        myChoiceBox.setOnAction(event -> {
            updateLists();
            refreshUI();
        });
        final DateTimeFormatter formatter= DateTimeFormatter.ofPattern("dd/MM/yyyy");
        myChoiceBox.setStyle("""
                -fx-font-family:".VnArial";
                -fx-font-size:11;
                -fx-alignment:center;
                -fx-text-alignment:center;
                """);
        priCheck.setOnAction(e->{
            priCol.setVisible(priCheck.isSelected());
        });
        effCheck.setOnAction(e->{
            effCol.setVisible(effCheck.isSelected());
        });
        myTable.getStylesheets().add("rowStyling.css");
        myTable.setRowFactory(row->new TableRow<Task>(){
            @Override
            protected void updateItem(Task item,boolean empty){
                super.updateItem(item,empty);
                if (empty){
                    setGraphic(null);
                    setText(null);
                    getStyleClass().clear();
                }
                else{
                    getStyleClass().clear();
                    if (getItem().effort.equals("High")){
                        getStyleClass().add("high-effort");
                    }
                    else if (getItem().effort.equals("Medium")){
                        getStyleClass().add("medium-effort");
                    }
                    else if (getItem().effort.equals("Low")){
                        getStyleClass().add("low-effort");
                    }
                    else{
                        getStyleClass().add("unlisted");
                    }
                }
            }
        });
        symbolCol.setCellValueFactory(e->{
            if (e.getValue().priority.equals("High")){
                return new ReadOnlyStringWrapper("!!!");
            }
            else if (e.getValue().priority.equals("Medium")){
                return new ReadOnlyStringWrapper("!!");
            }
            else if (e.getValue().priority.equals("Low")){
                return new ReadOnlyStringWrapper("!");
            }
            else {
                return new ReadOnlyStringWrapper("?");
            }
        });
        symbolCol.setCellFactory(e->new TableCell<>(){
            {
                getStyleClass().add("redText");
            }
            @Override
            protected void updateItem(String item, boolean empty){
                if (empty) {
                    setGraphic(null);
                    setText(null);
                }
                else {
                    setText(item);
                }
            }
        });
        taskCol.setCellValueFactory(cellData->new ReadOnlyStringWrapper(cellData.getValue().task));
        taskCol.setCellFactory(col->new TableCell<Task,String>(){
            final TextField textField=new TextField();
            {
                textField.focusedProperty().addListener(e->{
                    if (!textField.isFocused()){
                        if (textField.getText().isEmpty()) {showNotif("Error updating task: \"Task\" not provided."); cancelEdit();}
                        else commitEdit(textField.getText());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item,empty);
                if (empty){
                    setGraphic(null);
                    setText(null);
                }
                else if (isEditing()){
                    setGraphic(textField);
                    textField.setText(item);
                    setText(null);
                }
                else {
                    setGraphic(null);
                    setText(item);
                }

            }
            @Override
            public void startEdit(){
                super.startEdit();
                updateItem(getItem(),isEmpty());
                textField.requestFocus();
            }
            @Override
            public void cancelEdit(){
                super.cancelEdit();
                updateItem(getItem(),isEmpty());
            }
        });
        taskCol.setOnEditCommit(event -> {
            if (!event.getNewValue().isEmpty()) {
                event.getRowValue().task=event.getNewValue();
                updateLists();
                refreshUI();
                if (checkMenuItemAutoSave.isSelected()) save();
            }
        });
        priCol.setCellValueFactory(cellData->new ReadOnlyStringWrapper(cellData.getValue().priority));
        priCol.setCellFactory(ChoiceBoxTableCell.forTableColumn("Unlisted","Low","Medium","High"));
        priCol.setOnEditCommit(event->{
            if (event.getNewValue()!=null&&!event.getNewValue().isEmpty()){
                event.getRowValue().priority=event.getNewValue();
                updateLists();
                refreshUI();
                if (checkMenuItemAutoSave.isSelected()) save();
            }
        });
        effCol.setCellValueFactory(cellData->new ReadOnlyStringWrapper(cellData.getValue().effort));
        effCol.setCellFactory(ChoiceBoxTableCell.forTableColumn("Unlisted","Low","Medium","High"));
        effCol.setOnEditCommit(event->{
            if (event.getNewValue()!=null&&!event.getNewValue().isEmpty()){
                event.getRowValue().effort=event.getNewValue();
                updateLists();
                refreshUI();
                if(checkMenuItemAutoSave.isSelected()) save();
            }
        });

        DLCol.setCellValueFactory(cellData->new ReadOnlyObjectWrapper<LocalDate>(cellData.getValue().dl));
        DLCol.setCellFactory(col-> new TableCell<Task,LocalDate>(){
            DatePicker datePicker = new DatePicker();
            {datePicker.setConverter(new StringConverter<LocalDate>(){
                @Override
                public String toString(LocalDate localDate) {
                    return (localDate != null) ? localDate.format(formatter) : "";
                }
                @Override
                public LocalDate fromString(String s) {
                    if (!s.isEmpty()){
                        try {
                            return LocalDate.parse(s,formatter);
                        }
                        catch (DateTimeParseException e){
                            return null;                        }
                    }
                    else return null;
                }
            });
                datePicker.focusedProperty().addListener(e->{
                    if (!datePicker.isFocused()){
                        if (datePicker.getValue()!=null) commitEdit(datePicker.getValue());
                        else {showNotif("Error updating task: \"Deadline\" left blank or provided in wrong format."); cancelEdit();}
                    }
                });
            }

            @Override
            protected void updateItem(LocalDate item, boolean empty){
                super.updateItem(item,empty);
                if (empty){
                    setGraphic(null);
                    setText(null);
                }
                else if (isEditing()) {
                    datePicker.setValue(item);
                    setGraphic(datePicker);
                    setText(null);

                }
                else {
                    setGraphic(null);
                    setText(item.format(formatter));
                }
            }
            @Override
            public void startEdit(){
                super.startEdit();
                updateItem(getItem(),isEmpty());
                datePicker.requestFocus();
            }
            @Override
            public void cancelEdit(){
                super.cancelEdit();
                updateItem(getItem(),isEmpty());
            }
        });
        DLCol.setOnEditCommit(taskLocalDateCellEditEvent -> {
            if (taskLocalDateCellEditEvent.getNewValue()!=null) {
                taskLocalDateCellEditEvent.getRowValue().dl=taskLocalDateCellEditEvent.getNewValue();
                updateLists();
                refreshUI();
                if (checkMenuItemAutoSave.isSelected()) save();
            }
        });
        delCol.setCellFactory(col->new TableCell<Task,Void>(){
            final Button delButton=new Button("Delete");
            {
            delButton.setOnAction(event -> {
                Task rowTask=getTableView().getItems().get(getIndex());
                delete(rowTask);
            });
            }
            @Override
            protected void updateItem(Void item,boolean empty){
                super.updateItem(item,empty);
                if (empty) setGraphic(null);
                else setGraphic(delButton);
            }
        });
        myTable.setEditable(true);
        delCol.setEditable(false);
        updateLists();
        refreshUI();
        myPagination.currentPageIndexProperty().addListener(event->refreshUI());
    }
    public void add(ActionEvent e) throws IOException {
        FXMLLoader loader=new FXMLLoader(getClass().getResource("form.fxml"));
        Parent formRoot = loader.load();
        Stage formStage=new Stage();
        Scene formScene = new Scene(formRoot);
        formStage.setTitle("Adding Task");
        Image icon=new Image("todobuddy.png");
        formStage.getIcons().add(icon);
        formStage.setScene(formScene);
        formStage.show();
        formController formController=loader.getController();
        formController.confirmButton.setOnAction(event -> {
            task=formController.confirm();
            if (task!=null) {
                taskManager.original.add(task);
                formStage.close();
                updateLists();
                refreshUI();
                if (checkMenuItemAutoSave.isSelected()) save();
            }
        });
    }
    public void updateLists(){
        current=currentView();
    }
    public void refreshUI(){
        myTable.getItems().clear();
        myPagination.setPageCount(Math.max(1,Math.ceilDiv(current.size(),tasksPerPage)));
        myTable.getItems().addAll(pageContent(myPagination.getCurrentPageIndex()));
    }
    public void delete(Task task){
        taskManager.original.remove(task);
        updateLists();
        refreshUI();
        if (checkMenuItemAutoSave.isSelected()) save();
    }
    public ArrayList<Task> currentView(){
        String view=myChoiceBox.getValue();
        if (view.equals(views[0])) return taskManager.original;
        else if (view.equals(views[1])) return taskManager.viewSortedSimple();
        else return null;
    }
    public List<Task> pageContent(int pageNum){
        int start=pageNum*tasksPerPage;
        int end= Math.min(start+tasksPerPage,current.size());
        return (!current.isEmpty()) ? current.subList(start,end):current;
    }
    public void save(){
        taskStorage.save();
        showNotif("Saved!");
        updateUndoAvail();
        updateRedoAvail();
    }
    public void showNotif(String message){
        myLabel.setVisible(true);
        myLabel.setText(message);
        FadeTransition transition=new FadeTransition();
        transition.setNode(myLabel);
        transition.setDuration(Duration.seconds(2));
        transition.setFromValue(1);
        transition.setToValue(0);
        transition.play();
        transition.setOnFinished(e->myLabel.setVisible(false));
    }
    public Alert alertSetup(){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().clear();
        ButtonType yes=new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no=new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType cancel=new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().addFirst(yes);
        alert.getButtonTypes().add(no);
        alert.getButtonTypes().add(cancel);
        alert.setTitle("Exit");
        alert.setHeaderText("You are about to close the app, see you in another session!");
        alert.setContentText("Save your work before closing?");
        return alert;
    }
    public void close(){
        Alert alert=alertSetup();
        ButtonType signal=alert.showAndWait().get();
        if (signal.getButtonData()== ButtonBar.ButtonData.YES) {
            save();
            stage.close();
        }
        else if (signal.getButtonData()== ButtonBar.ButtonData.NO) {
            stage.close();
        }
    }
    public void undo(){
        taskStorage.undo();
        updateUndoAvail();
        updateLists();
        refreshUI();
    }
    public void redo(){
        taskStorage.redo();
        updateRedoAvail();
        updateLists();
        refreshUI();
    }
    public void updateUndoAvail(){
        menuItemUndo.setDisable(!taskStorage.hasBak());
    }
    public void updateRedoAvail(){
        menuItemRedo.setDisable(!taskStorage.hasRed());
    }
}
