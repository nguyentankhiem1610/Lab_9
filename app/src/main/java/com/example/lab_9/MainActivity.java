package com.example.lab_9;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private EditText edtId, edtName, edtEmail, edtTel;
    private Button btnSave, btnLoad;
    private RadioButton rbPreferences, rbSQLite;
    private TextView tvHint;
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
        btnSave = findViewById(R.id.btnSave);
        btnLoad = findViewById(R.id.btnLoad);
        rbPreferences = findViewById(R.id.rbPreferences);
        rbSQLite = findViewById(R.id.rbSQLite);
        tvHint = findViewById(R.id.tvHint);
        preferences = new StudentPreferences(getApplicationContext());
        database = new StudentDatabaseHelper(getApplicationContext());

        if (savedInstanceState != null) {
            rbSQLite.setChecked(savedInstanceState.getBoolean("sqlite", false));
            rbPreferences.setChecked(!rbSQLite.isChecked());
        }
        rbPreferences.setOnCheckedChangeListener((button, checked) -> updateHint());
        rbSQLite.setOnCheckedChangeListener((button, checked) -> updateHint());
        updateHint();
        btnSave.setOnClickListener(view -> saveStudent());
        btnLoad.setOnClickListener(view -> loadStudent());
    }

    private void updateHint() {
        tvHint.setText(rbSQLite.isChecked() ? R.string.hint_sqlite : R.string.hint_preferences);
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
        // ID va so dien thoai la String de giu so 0 o dau.
        return new Student(text(edtId), text(edtName), text(edtEmail), text(edtTel));
    }

    private void displayStudent(Student student) {
        edtId.setText(student.getId());
        edtName.setText(student.getName());
        edtEmail.setText(student.getEmail());
        edtTel.setText(student.getTel());
    }

    private void saveStudent() {
        Student student = readForm();
        if (student == null) return;
        boolean useSQLite = rbSQLite.isChecked();
        setBusy(true);
        executor.execute(() -> {
            String message;
            try {
                if (useSQLite) {
                    database.save(student);
                    message = "Đã lưu vào SQLite";
                } else {
                    message = preferences.save(student)
                            ? "Đã lưu vào SharedPreferences" : "Không thể ghi dữ liệu";
                }
            } catch (RuntimeException error) {
                Log.e("StudentStorage", "Save failed", error);
                message = "Lưu thất bại. Vui lòng thử lại";
            }
            finishOperation(message, null);
        });
    }

    private void loadStudent() {
        boolean useSQLite = rbSQLite.isChecked();
        if (useSQLite && !required(edtId)) return;
        String id = text(edtId);
        setBusy(true);
        executor.execute(() -> {
            try {
                Student student = useSQLite ? database.load(id) : preferences.load();
                String message = student != null ? "Đã tải dữ liệu"
                        : useSQLite ? "Không tìm thấy sinh viên có ID: " + id
                        : "Chưa có sinh viên được lưu";
                finishOperation(message, student);
            } catch (RuntimeException error) {
                Log.e("StudentStorage", "Load failed", error);
                finishOperation("Tải thất bại. Vui lòng thử lại", null);
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
        btnSave.setEnabled(!busy);
        btnLoad.setEnabled(!busy);
        rbPreferences.setEnabled(!busy);
        rbSQLite.setEnabled(!busy);
        edtId.setEnabled(!busy);
        edtName.setEnabled(!busy);
        edtEmail.setEnabled(!busy);
        edtTel.setEnabled(!busy);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("sqlite", rbSQLite.isChecked());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // Dong database sau khi cac cong viec dang cho da hoan tat.
        executor.execute(() -> database.close());
        executor.shutdown();
        super.onDestroy();
    }
}
