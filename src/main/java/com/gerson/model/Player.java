package com.gerson.model;

import java.io.PrintStream;

public class Player {
    private final int id;
    private String name;
    private int progress;
    private int reportedClicks;
    private final PrintStream output;

    public Player(int id, PrintStream output) {
        this.id = id;
        this.output = output;
        this.progress = 0;
        this.reportedClicks = 0;
        this.name = "Jugador " + id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProgress() {
        return progress;
    }

    public void advance(int amount) {
        this.progress = Math.min(100, this.progress + amount);
    }

    public int getReportedClicks() {
        return reportedClicks;
    }

    public void setReportedClicks(int reportedClicks) {
        this.reportedClicks = reportedClicks;
    }

    public PrintStream getOutput() {
        return output;
    }
}