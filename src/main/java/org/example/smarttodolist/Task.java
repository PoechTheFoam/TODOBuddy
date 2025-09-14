package org.example.smarttodolist;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Task {
    DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String task;
    String priority;
    String effort;
    LocalDate dl;

    public Task(String task, String priority, String effort, LocalDate dl) {
        this.task = task;
        this.priority = priority;
        this.effort = effort;
        this.dl = dl;
    }

    public String toString() {
        if (task.contains("|")){
            String escapedTask=task.replaceAll("\\|","%!%");
            return escapedTask + "|" + priority + "|" + effort + "|" + dl.format(formatter);
        }
        else return task + "|" + priority + "|" + effort + "|" + dl.format(formatter);
    }
}
