package com.example.lab_9;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StudentDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "students.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "students";

    public StudentDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE students ("
                + "id TEXT PRIMARY KEY NOT NULL, "
                + "name TEXT NOT NULL, "
                + "email TEXT NOT NULL, "
                + "tel TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Chưa có migration cho phiên bản mới");
    }

    public void save(Student student) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", student.getId());
        values.put("name", student.getName());
        values.put("email", student.getEmail());
        values.put("tel", student.getTel());

        db.beginTransaction();
        try {
            int updated = db.update(TABLE_NAME, values,
                    "id = ?", new String[]{student.getId()});
            if (updated == 0) db.insertOrThrow(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Student load(String id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{"id", "name", "email", "tel"},
                "id = ?", new String[]{id}, null, null, null)) {
            if (!cursor.moveToFirst()) return null;
            return new Student(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("tel")));
        }
    }
}
