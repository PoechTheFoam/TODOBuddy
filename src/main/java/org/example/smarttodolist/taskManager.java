package org.example.smarttodolist;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class taskManager{
    ArrayList<Task> original=new ArrayList<>();
    ArrayList<Task> simpleView;
    HashMap<String, Integer> choices = new HashMap<>();
    public taskManager(){
        choices.put("Unlisted",0);
        choices.put("Low",1);
        choices.put("Medium",2);
        choices.put("High",3);
    }
    public ArrayList<Task> viewSortedSimple(){
        simpleView=new ArrayList<>(original);
        simpleView.sort(this::compareTasksSimple);
        return simpleView;
    }
    public int compareTasksSimple(Task a,Task b){
        int result= Double.compare(totalScore(b), totalScore(a));
        if (result==0&&a.task.length()!=b.task.length()) return (a.task.length()>b.task.length()) ? -1:1;
        return result;
    }
    public double totalScore(Task t){
        return deadlineScore(t)+priScore(t)+effScore(t)+taskScore(t);
    }
    public double deadlineScore(Task task){
        long daysDiff = ChronoUnit.DAYS.between(LocalDate.now(),task.dl);
        return switch ((int) daysDiff) {
            case 0 -> 0.6;
            case -1 -> 0.5;
            case -2 -> 0.425;
            case 1 -> 0.5;
            case 2 -> 0.475;
            default -> Math.max(-1,(0.6 - Math.abs(daysDiff * 0.05)));
        };
    }
    public double priScore(Task task){
        return (choices.getOrDefault(task.priority,0)==3) ? 0.4: (choices.getOrDefault(task.priority,0)==2) ? 0.3: (choices.getOrDefault(task.priority,0)==1) ? 0.2:0;
    }
    public double effScore(Task task){
        return (choices.getOrDefault(task.effort,0)==1)?0.3:(choices.getOrDefault(task.effort,0)==2)?0.2: (choices.getOrDefault(task.effort,0)==3)?0.1:0;
    }
    public double taskScore(Task task){
        return (task.task.length()>=20) ? 0.2: (task.task.length()>=10)?0.1:0;
    }
}
