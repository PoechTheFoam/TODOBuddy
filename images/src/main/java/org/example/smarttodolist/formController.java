package org.example.smarttodolist;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class formController {
    @FXML
    private GridPane myGridPane;
    @FXML
    private TextField formTextField;
    @FXML
    private ChoiceBox<String> formPri;
    @FXML
    private ChoiceBox<String> formEff;
    @FXML
    private DatePicker formDL;
    @FXML
    Button confirmButton;
    @FXML
    private Label formLabel;
    String[] choices = {"Unlisted","Low","Medium","High"};
    public void initialize() {
        final DateTimeFormatter formatter= DateTimeFormatter.ofPattern("dd/MM/yyyy");
        formPri.getItems().addAll(choices);
        formEff.getItems().addAll(choices);
        formDL.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate localDate) {
                return (localDate!=null) ? localDate.format(formatter):"";
            }
            @Override
            public LocalDate fromString(String s) {
                return (!s.isEmpty()) ? LocalDate.parse(s,formatter):null;
            }
        });
    }
    public Task confirm(){
        if (formTextField.getText().isEmpty()||formPri.getValue()==null||formEff.getValue()==null||formDL.getValue()==null) {
            formLabel.setText("Must fill out all fields");
            return null;
        }
        return new Task(formTextField.getText(),formPri.getValue(),formEff.getValue(),formDL.getValue());
    }
}
