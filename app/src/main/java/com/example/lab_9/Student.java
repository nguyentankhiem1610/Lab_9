package com.example.lab_9;

public class Student {
    private final String id;
    private final String name;
    private final String email;
    private final String tel;

    public Student(String id, String name, String email, String tel) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.tel = tel;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTel() { return tel; }
}
