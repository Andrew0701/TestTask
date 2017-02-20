package com.example.andrew.testtask.interfaces;

import com.example.andrew.testtask.models.Student;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("students")
    Call<List<Student>> getStudents();
}
