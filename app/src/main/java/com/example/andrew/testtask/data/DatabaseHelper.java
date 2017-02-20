package com.example.andrew.testtask.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.example.andrew.testtask.models.Course;
import com.example.andrew.testtask.models.Student;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "Students";
    private static final String COMMA_SEP = ",";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String AUTOINCREMENT = " AUTOINCREMENT";

    private static final String SQL_CREATE_TABLE_STUDENT =
            "CREATE TABLE " + StudentEntry.TABLE_NAME + " (" +
                    StudentEntry._ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
                    StudentEntry.COLUMN_ID + TEXT_TYPE + COMMA_SEP +
                    StudentEntry.COLUMN_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    StudentEntry.COLUMN_LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    StudentEntry.COLUMN_BIRTHDAY + TEXT_TYPE + " )";

    private static final String SQL_CREATE_TABLE_STUDENT_COURSES =
            "CREATE TABLE " + StudentCoursesEntry.TABLE_NAME + " (" +
                    StudentCoursesEntry._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    StudentCoursesEntry.COLUMN_STUDENT_ID + INTEGER_TYPE + COMMA_SEP +
                    StudentCoursesEntry.COLUMN_COURSE_NAME + TEXT_TYPE + COMMA_SEP +
                    StudentCoursesEntry.COLUMN_MARK + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_STUDENT_TABLE =
            "DROP TABLE IF EXISTS " + StudentEntry.TABLE_NAME;

    private static final String SQL_DELETE_STUDENT_COURSES_TABLE =
            "DROP TABLE IF EXISTS " + StudentCoursesEntry.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_STUDENT);
        db.execSQL(SQL_CREATE_TABLE_STUDENT_COURSES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_STUDENT_TABLE);
        db.execSQL(SQL_DELETE_STUDENT_COURSES_TABLE);

        db.execSQL(SQL_CREATE_TABLE_STUDENT);
        db.execSQL(SQL_CREATE_TABLE_STUDENT_COURSES);
    }

    public void insertAllStudents(List<Student> students) {
        long studentId = getLastStudentId();
        String sql = "INSERT INTO "+ StudentEntry.TABLE_NAME +" VALUES (?,?,?,?,?);";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        for (Student student: students) {
            statement.clearBindings();
            statement.bindLong(1, studentId++);
            statement.bindString(2, student.getId());
            statement.bindString(3, student.getFirstName());
            statement.bindString(4, student.getLastName());
            statement.bindString(5, student.getBirthday());
            statement.execute();
            ContentValues values = new ContentValues();
            for (Course course : student.getCourses()) {
                values.put(StudentCoursesEntry.COLUMN_STUDENT_ID, studentId);
                values.put(StudentCoursesEntry.COLUMN_COURSE_NAME, course.getName());
                values.put(StudentCoursesEntry.COLUMN_MARK, course.getMark());
                db.insert(StudentCoursesEntry.TABLE_NAME, null, values);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private long getLastStudentId() {
        int lastId = 0;
        final String query = "SELECT MAX(_id) AS _id from " + StudentEntry.TABLE_NAME;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            lastId = cursor.getInt(0);
        }
        cursor.close();
        return lastId;
    }

    public List<Student> getStudents(int count, int offset) {
        List<Student> students = new ArrayList<>();
        final String query = "SELECT * FROM " + StudentEntry.TABLE_NAME + " LIMIT " + count +
                " OFFSET " + offset;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Student student = cursorToStudent(cursor);
                students.add(student);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return students;
    }

    public List<Student> getStudents(String courseName, int mark, int count, int offset) {
        if (courseName == null) {
            return getStudents(count, offset);
        }
        List<Integer> studentsId = new ArrayList<>();
        String markCondition = "";
        if (mark >= 0) {
            markCondition = " AND " + StudentCoursesEntry.COLUMN_MARK + " = " + mark;
        }
        final String query = "SELECT " + StudentCoursesEntry.COLUMN_STUDENT_ID + " FROM " +
                StudentCoursesEntry.TABLE_NAME + " WHERE " + StudentCoursesEntry.COLUMN_COURSE_NAME +
                " = " + "\"" + courseName + "\"" + markCondition + " LIMIT " + count + " OFFSET " + offset;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                studentsId.add(cursor.getInt(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return getStudentsById(studentsId);
    }

    public List<Student> getStudentsById(List<Integer> studentsId) {
        List<Student> students = new ArrayList<>();
        final String query = "SELECT * FROM " + StudentEntry.TABLE_NAME + " WHERE " +
                StudentEntry._ID + " IN (" + TextUtils.join(", ", studentsId) + ")";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Student student = cursorToStudent(cursor);
                students.add(student);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return students;
    }

    private Student cursorToStudent(Cursor cursor) {
        Student student = new Student();
        int studentId = Integer.parseInt(cursor.getString(0));
        student.setId(cursor.getString(1));
        student.setFirstName(cursor.getString(2));
        student.setLastName(cursor.getString(3));
        student.setBirthday(cursor.getString(4));
        List<Course> courses = getStudentCourses(studentId);
        student.setCourses(courses);
        return student;
    }

    private List<Course> getStudentCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        final String query = "SELECT " + StudentCoursesEntry.COLUMN_COURSE_NAME + COMMA_SEP +
                StudentCoursesEntry.COLUMN_MARK + " FROM " + StudentCoursesEntry.TABLE_NAME +
                " WHERE " + StudentCoursesEntry.COLUMN_STUDENT_ID + " = " + studentId;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Course course = cursorToCourse(cursor);
                courses.add(course);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return courses;
    }

    private Course cursorToCourse(Cursor cursor) {
        Course course = new Course();
        course.setName(cursor.getString(0));
        course.setMark(cursor.getInt(1));
        return course;
    }

    public List<String> getAllCourseNames() {
        List<String> courses = new ArrayList<>();
        final String query = "SELECT DISTINCT " + StudentCoursesEntry.COLUMN_COURSE_NAME +
                " FROM " + StudentCoursesEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String courseName = cursor.getString(0);
                courses.add(courseName);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return courses;
    }

    public void clearAllData(){
        final String queryDeleteStudents = "DELETE FROM " + StudentEntry.TABLE_NAME;
        final String queryDeleteStudentCourses = "DELETE FROM " + StudentCoursesEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(queryDeleteStudents);
        db.execSQL(queryDeleteStudentCourses);
    }

    public static class StudentEntry implements BaseColumns {
        public static final String TABLE_NAME = "student";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_BIRTHDAY = "birthday";
    }

    public static class StudentCoursesEntry implements BaseColumns {
        public static final String TABLE_NAME = "student_courses";
        public static final String COLUMN_STUDENT_ID = "student_id";
        public static final String COLUMN_COURSE_NAME = "course_name";
        public static final String COLUMN_MARK = "mark";
    }
}
