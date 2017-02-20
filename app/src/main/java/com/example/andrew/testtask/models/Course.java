package com.example.andrew.testtask.models;

import com.google.gson.annotations.SerializedName;

public class Course {

    @SerializedName("name")
    private String name;

    @SerializedName("mark")
    private int mark;

    public void setName(String name) {
        this.name = name;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public String getName() {
        return name;
    }

    public int getMark() {
        return mark;
    }
}
