package com.example.andrew.testtask.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Student {

    @SerializedName("id")
    private String id;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("courses")
    private List<Course> courses;

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
