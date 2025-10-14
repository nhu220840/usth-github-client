package com.usth.githubclient.domain.model;

import java.util.Calendar;

public class ContributionDataEntry {
    private final Calendar date;
    private int count;

    public ContributionDataEntry(Calendar date, int count) {
        this.date = date;
        this.count = count;
    }

    public Calendar getDate() {
        return date;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        this.count++;
    }
}