package com.example.lab_9;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private EditText edtId, edtName, edtEmail, edtTel;
    private Button btnSavePref, btnLoadPref;
    private Button btnSaveSqlite, btnLoadSqlite;
    private StudentPreferences preferences;
    private StudentDatabaseHelper database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtId = findViewById(R.id.edtId);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtTel = findViewById(R.id.edtTel);

        btnSavePref = findViewById(R.id.btnSavePref);
        btnLoadPref = findViewById(R.id.btnLoadPref);
        btnSaveSqlite = findViewById(R.id.btnSaveSqlite);
        btnLoadSqlite = findViewById(R.id.btnLoadSqlite);

        preferences = new StudentPreferences(getApplicationContext());
        database = new StudentDatabaseHelper(getApplicationContext());

        btnSavePref.setOnClickListener(view -> saveStudentPref());
        btnLoadPref.setOnClickListener(view -> loadStudentPref());
        btnSaveSqlite.setOnClickListener(view -> saveStudentSqlite());
        btnLoadSqlite.setOnClickListener(view -> loadStudentSqlite());
    }

    private String text(EditText input) {
        return input.getText().toString().trim();
    }

    private boolean required(EditText input) {
        if (!text(input).isEmpty()) return true;
        input.setError("Vui lòng nhập thông tin");
        input.requestFocus();
        return false;
    }

    private Student readForm() {
        if (!required(edtId) || !required(edtName)
                || !required(edtEmail) || !required(edtTel)) return null;
        if (!Patterns.EMAIL_ADDRESS.matcher(text(edtEmail)).matches()) {
            edtEmail.setError("Email chưa đúng định dạng");
            edtEmail.requestFocus();
            return null;
        }
        return new Student(text(edtId), text(edtName), text(edtEmail), text(edtTel));
    }

    private void displayStudent(Student student) {
        edtId.setText(student.getId());
        edtName.setText(student.getName());
        edtEmail.setText(student.getEmail());
        edtTel.setText(student.getTel());
    }

    private void saveStudentPref() {
        Student student = readForm();
        if (student == null) return;
        setBusy(true);
        executor.execute(() -> {
            String message;
            try {
                message = preferences.save(student)
                        ? "Đã lưu vào SharedPreferences" : "Không thể ghi dữ liệu SharedPreferences";
            } catch (RuntimeException error) {
                Log.e("StudentStorage", "Save Pref failed", error);
                message = "Lưu SharedPreferences thất bại";
            }
            finishOperation(message, null);
        });
    }

    private void loadStudentPref() {
        setBusy(true);
        executor.execute(() -> {
            try {
                Student student = preferences.load();
                String message = student != null ? "Đã tải dữ liệu từ SharedPreferences"
                        : "Chưa có sinh viên được lưu trong SharedPreferences";
                finishOperation(message, student);
            } catch (RuntimeException error) {
                Log.e("StudentStorage", "Load Pref failed", error);
                finishOperation("Tải SharedPreferences thất bại", null);
            }
        });
    }

    private void saveStudentSqlite() {
        Student student = readForm();
        if (student == null) return;
        setBusy(true);
        executor.execute(() -> {
            String message;
            try {
                database.save(student);
                message = "Đã lưu vào SQLite";
            } catch (RuntimeException error) {
                Log.e("StudentStorage", "Save SQLite failed", error);
                message = "Lưu SQLite thất bại";
            }
            finishOperation(message, null);
        });
    }

    private void loadStudentSqlite() {
        if (!required(edtId)) return;
        String id = text(edtId);
        setBusy(true);
        executor.execute(() -> {
            try {
                Student student = database.load(id);
                String message = student != null ? "Đã tải dữ liệu từ SQLite"
                        : "Không tìm thấy sinh viên có ID: " + id;
                finishOperation(message, student);
            } catch (RuntimeException error) {
                Log.e("StudentStorage", "Load SQLite failed", error);
                finishOperation("Tải SQLite thất bại", null);
            }
        });
    }

    private void finishOperation(String message, Student student) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            setBusy(false);
            if (student != null) displayStudent(student);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void setBusy(boolean busy) {
        btnSavePref.setEnabled(!busy);
        btnLoadPref.setEnabled(!busy);
        btnSaveSqlite.setEnabled(!busy);
        btnLoadSqlite.setEnabled(!busy);
        edtId.setEnabled(!busy);
        edtName.setEnabled(!busy);
        edtEmail.setEnabled(!busy);
        edtTel.setEnabled(!busy);
    }

    @Override
    protected void onDestroy() {
        executor.execute(() -> database.close());
        executor.shutdown();
        super.onDestroy();
    }
}
