package com.example.andrew.testtask.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.andrew.testtask.data.DatabaseHelper;
import com.example.andrew.testtask.models.Course;
import com.example.andrew.testtask.models.Student;

import java.util.ArrayList;
import java.util.List;

public class AsyncManageDataFragment extends Fragment {
    public static final String TAG = "AsyncManageDataFragment";

    private TasksStatusListener mStatusListener;

    private InsertIntoDbAsyncTask mInsertIntoDbAsyncTask;
    boolean isInsertTaskExecuting = false;

    private GetDataFromDbAsyncTask mGetDataFromDbAsyncTask;
    boolean isGettingDataFromDbTaskExecuting = false;

    private GetAllCoursesNamesAsyncTask mGetAllCoursesNamesAsyncTask;
    boolean isGettingAllCoursesNamesTaskExecuting = false;

    public AsyncManageDataFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TasksStatusListener) {
            mStatusListener = (TasksStatusListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStatusListener = null;
    }

    public void startInsertionTask(Context context, List<Student> students) {
        if (!isInsertTaskExecuting) {
            isInsertTaskExecuting = true;
            mInsertIntoDbAsyncTask = new InsertIntoDbAsyncTask(context);
            mInsertIntoDbAsyncTask.execute(students);
        }
    }

    public void cancelInsertionTask() {
        if (isInsertTaskExecuting) {
            isInsertTaskExecuting = false;
            mInsertIntoDbAsyncTask.cancel(true);
        }
    }

    public void startGettingDataTask(Context context, Object... obj) {
        if (!isGettingDataFromDbTaskExecuting) {
            isGettingDataFromDbTaskExecuting = true;
            mGetDataFromDbAsyncTask = new GetDataFromDbAsyncTask(context);
            mGetDataFromDbAsyncTask.execute(obj);
        }
    }

    public void cancelGettingDataTask() {
        if (isGettingDataFromDbTaskExecuting) {
            isGettingDataFromDbTaskExecuting = false;
            mGetDataFromDbAsyncTask.cancel(true);
        }
    }

    public void startGettingCoursesNamesTask(Context context) {
        if (!isGettingAllCoursesNamesTaskExecuting) {
            isGettingAllCoursesNamesTaskExecuting = true;
            mGetAllCoursesNamesAsyncTask = new GetAllCoursesNamesAsyncTask(context);
            mGetAllCoursesNamesAsyncTask.execute();
        }
    }

    public void cancelGettingCoursesNamesTask() {
        if (isGettingAllCoursesNamesTaskExecuting) {
            isGettingAllCoursesNamesTaskExecuting = false;
            mGetAllCoursesNamesAsyncTask.cancel(true);
        }
    }

    public interface TasksStatusListener {
        void onDataStartSave();
        void onDataSaved();
        void onDataLoadFinish(List<Student> students);
        void onCoursesNamesLoadFinish(List<String> coursesNames);
    }

    private class InsertIntoDbAsyncTask extends AsyncTask<List<Student>, Void, Void> {
        private Context mContext;
        private DatabaseHelper mDatabaseHelper;
        
        public InsertIntoDbAsyncTask(Context context) {
            mContext = context;
            mDatabaseHelper = new DatabaseHelper(mContext);
        }

        @Override
        protected void onPreExecute() {
            if (mStatusListener != null) {
                mStatusListener.onDataStartSave();
            }
        }

        @Override
        protected Void doInBackground(List<Student>... params) {
            List<Student> students = params[0];
            mDatabaseHelper.clearAllData();
            mDatabaseHelper.insertAllStudents(students);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isInsertTaskExecuting = false;
            if (mStatusListener != null) {
                mStatusListener.onDataSaved();
            }
        }
    }

    private class GetDataFromDbAsyncTask extends AsyncTask<Object, Void, List<Student>> {
        private Context mContext;
        private DatabaseHelper mDatabaseHelper;

        private List<Student> mStudents = new ArrayList<>();
        
        public GetDataFromDbAsyncTask(Context context) {
            mContext = context;
            mDatabaseHelper = new DatabaseHelper(mContext);
        }

        @Override
        protected List<Student> doInBackground(Object... params) {
            String courseName = (String) params[0];
            int mark = (int) params[1];
            int count = (int) params[2];
            int offset = (int) params[3];
            mStudents = mDatabaseHelper.getStudents(courseName, mark, count, offset);
            return mStudents;
        }

        @Override
        protected void onPostExecute(List<Student> students) {
            isGettingDataFromDbTaskExecuting = false;
            if (mStatusListener != null) {
                mStatusListener.onDataLoadFinish(students);
            }
        }
    }

    private class GetAllCoursesNamesAsyncTask extends AsyncTask<Void, Void, List<String>> {
        private Context mContext;
        private DatabaseHelper mDatabaseHelper;

        public GetAllCoursesNamesAsyncTask(Context context) {
            mContext = context;
            mDatabaseHelper = new DatabaseHelper(mContext);
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> coursesNames = mDatabaseHelper.getAllCourseNames();
            return coursesNames;
        }

        @Override
        protected void onPostExecute(List<String> coursesNames) {
            isGettingAllCoursesNamesTaskExecuting = false;
            if (mStatusListener != null) {
                mStatusListener.onCoursesNamesLoadFinish(coursesNames);
            }
        }
    }
}
