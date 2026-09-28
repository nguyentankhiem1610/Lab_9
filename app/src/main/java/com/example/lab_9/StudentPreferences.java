package com.example.lab_9;

import android.content.Context;
import android.content.SharedPreferences;

public class StudentPreferences {
    private final SharedPreferences preferences;

    public StudentPreferences(Context context) {
        preferences = context.getSharedPreferences(
                "com.example.studentstorage.student", Context.MODE_PRIVATE);
    }
    public boolean save(Student student) {
        return preferences.edit()
                .putString("id", student.getId())
                .putString("name", student.getName())
                .putString("email", student.getEmail())
                .putString("tel", student.getTel())
                .commit();
    }

    public Student load() {
        if (!preferences.contains("id")) return null;
        return new Student(
                preferences.getString("id", ""),
                preferences.getString("name", ""),
                preferences.getString("email", ""),
                preferences.getString("tel", ""));
    }
}
