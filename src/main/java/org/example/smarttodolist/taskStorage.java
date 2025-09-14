package org.example.smarttodolist;

import javafx.scene.control.Alert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class taskStorage {
    private File temp=new File("tasks.temp");
    private File perm=new File("tasks.txt");
    private File backup=new File("tasks.bak");
    private File redo=new File("tasks.redo");
    taskManager taskManager;
    public taskStorage(taskManager t){
        this.taskManager=t;
    }
    public void save(){
        try{
        try (FileWriter writer=new FileWriter(temp)) {
            for (Task t : taskManager.original) {
                writer.write(t.toString() + System.lineSeparator());
            }
        }
            if (perm.exists()) Files.copy(perm.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (temp.exists()) Files.move(temp.toPath(), perm.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (FileNotFoundException e){
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Error");
            alert.setHeaderText("I couldn't save your tasks...");
            alert.setContentText("Could not create save file: "+e);
            alert.showAndWait();
        }
        catch (IOException e) {
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Error");
            alert.setHeaderText("Something went wrong...");
            alert.setContentText("Error: "+e);
            alert.showAndWait();
        }
    }
    public void load(){
        if (perm.exists()){
            try {
                try (BufferedReader reader = new BufferedReader(new FileReader("tasks.txt"))) {
                    taskManager.original.clear();
                    String line;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    while ((line = reader.readLine()) != null) {
                        String[] params = line.split("\\|");
                        if (params.length == 4) {
                            if (params[0].contains("%!%")) params[0] = params[0].replaceAll("%!%", "\\|");
                            taskManager.original.add(new Task(params[0], params[1], params[2], LocalDate.parse(params[3], formatter)));
                        }
                    }
                }
                if (backup.exists()) Files.copy(perm.toPath(),backup.toPath(),StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Load Error");
                alert.setHeaderText("Something went wrong...");
                alert.setContentText("Error "+e);
                alert.showAndWait();
            }
        }
        else if (backup.exists()){
            try {
                Alert alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Load Error");
                alert.setHeaderText("tasks.txt not found");
                alert.setContentText("I couldn't find your main save file, so I'm loading from your backup instead");
                alert.showAndWait();
                try (BufferedReader reader = new BufferedReader(new FileReader("tasks.bak"))) {
                    taskManager.original.clear();
                    String line;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    while ((line = reader.readLine()) != null) {
                        String[] params = line.split("\\|");
                        if (params.length == 4) {
                            if (params[0].contains("%!%")) params[0] = params[0].replaceAll("%!%", "\\|");
                            taskManager.original.add(new Task(params[0], params[1], params[2], LocalDate.parse(params[3], formatter)));
                        }
                    }
                }
                Files.copy(backup.toPath(),perm.toPath(),StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Load Error");
                alert.setHeaderText("Something went wrong...");
                alert.setContentText("Error "+e);
                alert.showAndWait();
            }

        }
        else{
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Load");
            alert.setHeaderText(null);
            alert.setContentText("No save file detected, proceeding with blank list");
            alert.showAndWait();

        }
    }
    public void redo(){
        try {
            if (redo.exists()) {
                Files.copy(perm.toPath(),backup.toPath(),StandardCopyOption.REPLACE_EXISTING);
                Files.move(redo.toPath(),perm.toPath(),StandardCopyOption.REPLACE_EXISTING);
                load();
            }
        } catch (IOException e) {
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Redo Error");
            alert.setHeaderText("Something went wrong...");
            alert.setContentText("Error "+e);
            alert.showAndWait();
        }
    }
    public void undo(){
        try {
            if (backup.exists()) {
                Files.copy(perm.toPath(),redo.toPath(),StandardCopyOption.REPLACE_EXISTING);
                Files.move(backup.toPath(),perm.toPath(),StandardCopyOption.REPLACE_EXISTING);
                load();
            }
        } catch (IOException e) {
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Undo Error");
            alert.setHeaderText("Something went wrong...");
            alert.setContentText("Error "+e);
            alert.showAndWait();
        }
    }
    public boolean hasBak(){
        return backup.exists();
    }
    public boolean hasRed(){
        return redo.exists();
    }
}
