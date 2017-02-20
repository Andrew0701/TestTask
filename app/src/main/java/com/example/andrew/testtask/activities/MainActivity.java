package com.example.andrew.testtask.activities;

import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.andrew.testtask.interfaces.ApiClient;
import com.example.andrew.testtask.interfaces.ApiInterface;
import com.example.andrew.testtask.data.DatabaseHelper;
import com.example.andrew.testtask.R;
import com.example.andrew.testtask.adapters.StudentAdapter;
import com.example.andrew.testtask.fragments.AsyncManageDataFragment;
import com.example.andrew.testtask.listeners.EndlessRecyclerOnScrollListener;
import com.example.andrew.testtask.models.Course;
import com.example.andrew.testtask.models.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AsyncManageDataFragment.TasksStatusListener,
        StudentAdapter.StudentAdapterListener {
    public static final String TAG = "MainActivity";

    private RecyclerView mStudentsRecyclerView;
    private StudentAdapter mStudentAdapter;
    private EndlessRecyclerOnScrollListener mEndlessOnScrollListener;

    private SlidingPaneLayout mFilterSlidingPaneLayout;

    private DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);

    private AsyncManageDataFragment mManageDataFragment;

    private static final int mRecordsPerPage = 20;

    private boolean mIsFiltered = false;
    private String mFilterCourseName;
    private int mFilterMark;

    private static final String KEY_IS_FILTERED = "mIsFiltered";
    private static final String KEY_FILTER_COURSE_NAME = "mFilterCourseName";
    private static final String KEY_FILTER_MARK = "mFilterMark";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();
        initAsyncManageDataFragment();
        if (savedInstanceState == null) {
            showLoadingAnimation();
            downloadData();
        } else {
            restorePreviousState(savedInstanceState);
        }
    }

    private void restorePreviousState(Bundle savedInstanceState) {
        mIsFiltered = savedInstanceState.getBoolean(KEY_IS_FILTERED);
        mFilterCourseName = savedInstanceState.getString(KEY_FILTER_COURSE_NAME);
        mFilterMark = savedInstanceState.getInt(KEY_FILTER_MARK);
        if (mIsFiltered) {
            getFilteredDataFromDb(0);
        } else {
            getDataFromDb(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_FILTERED, mIsFiltered);
        outState.putString(KEY_FILTER_COURSE_NAME, mFilterCourseName);
        outState.putInt(KEY_FILTER_MARK, mFilterMark);
    }

    private void initFields() {
        mStudentsRecyclerView = (RecyclerView) findViewById(R.id.rvStudentItems);
        mStudentAdapter = new StudentAdapter(this);
        mStudentsRecyclerView.setAdapter(mStudentAdapter);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mStudentsRecyclerView.setLayoutManager(linearLayoutManager);
        mEndlessOnScrollListener =
                new EndlessRecyclerOnScrollListener((LinearLayoutManager)linearLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                if (!mIsFiltered) {
                    getDataFromDb(currentPage);
                } else {
                    getFilteredDataFromDb(currentPage);
                }
            }
        };
        mStudentsRecyclerView.addOnScrollListener(mEndlessOnScrollListener);
        initFilterSlidePanelLayout();
    }

    private void initFilterSlidePanelLayout() {
        mFilterSlidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.splFilter);

        final List<String> courses = mDatabaseHelper.getAllCourseNames();
        ArrayAdapter<String> coursesAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        coursesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner coursesSpinner = (Spinner) findViewById(R.id.spinnerFiltersCourse);
        coursesSpinner.setAdapter(coursesAdapter);

        final EditText markEditText = (EditText) findViewById(R.id.etFiltersMark);

        findViewById(R.id.btnFilterOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseName = coursesSpinner.getSelectedItem().toString();
                int mark = -1;
                if (!markEditText.getText().toString().isEmpty()) {
                    mark = Integer.parseInt(markEditText.getText().toString());
                }
                hideKeyboard();
                mFilterSlidingPaneLayout.closePane();
                setFilter(courseName, mark);
            }
        });
        findViewById(R.id.btnFilterClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                mFilterSlidingPaneLayout.closePane();
                clearFilter();
            }
        });
    }

    private void initAsyncManageDataFragment() {
        FragmentManager fm = getSupportFragmentManager();
        mManageDataFragment = (AsyncManageDataFragment) fm.findFragmentByTag(AsyncManageDataFragment.TAG);

        if (mManageDataFragment == null) {
            mManageDataFragment = new AsyncManageDataFragment();
            fm.beginTransaction().add(mManageDataFragment, AsyncManageDataFragment.TAG).commit();
        }
    }

    private void downloadData() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Student>> call = apiInterface.getStudents();
        call.enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                List<Student> students = response.body();
                if (students != null) {
                    mManageDataFragment.startInsertionTask(getApplicationContext(), students);
                } else {
                    showMessage(R.string.message_no_elements);
                }
            }
            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                hideLoadingAnimation();
                showMessage(R.string.message_error_load_data);
            }
        });
    }

    private void showLoadingAnimation() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    private void hideLoadingAnimation() {
        LinearLayout loadingPanel = (LinearLayout) findViewById(R.id.loadingPanel);
        if (loadingPanel.getVisibility() == View.VISIBLE) {
            loadingPanel.setVisibility(View.GONE);
        }
    }

    private void getDataFromDb(int currentPage) {
        mManageDataFragment
                .startGettingDataTask(this, null, 0, mRecordsPerPage, mRecordsPerPage * currentPage);
    }

    private void getFilteredDataFromDb(int currentPage) {
        mManageDataFragment.startGettingDataTask(this, mFilterCourseName, mFilterMark,
                mRecordsPerPage, mRecordsPerPage * currentPage);
    }

    private void updateShowingStudents(List<Student> students) {
        if (students.isEmpty()) {
            showMessage(R.string.message_no_elements);
        } else {
            hideMessage();
            mStudentAdapter.updateAdapterData(students);
        }
    }

    private void showMessage(int text) {
        TextView message = (TextView) findViewById(R.id.tvMessage);
        message.setVisibility(View.VISIBLE);
        message.setText(text);
    }

    private void hideMessage() {
        TextView message = (TextView) findViewById(R.id.tvMessage);
        message.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            showFilterPanel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterPanel() {
        if (mFilterSlidingPaneLayout.isOpen()) {
            mFilterSlidingPaneLayout.closePane();
        } else {
            mFilterSlidingPaneLayout.openPane();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager
                    imm = (InputMethodManager)this.getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    private void setFilter(String filterCourseName, int filterMark) {
        mIsFiltered = true;
        mFilterCourseName = filterCourseName;
        mFilterMark = filterMark;
        mStudentAdapter.resetState();
        mEndlessOnScrollListener.resetState();
        showLoadingAnimation();
        getFilteredDataFromDb(0);
    }

    private void clearFilter() {
        mIsFiltered = false;
        mFilterCourseName = null;
        mFilterMark = 0;
        mStudentAdapter.resetState();
        mEndlessOnScrollListener.resetState();
        showLoadingAnimation();
        getDataFromDb(0);
    }

    @Override
    public void onInfoButtonClick(Student student) {
        showStudentInfoDialog(student);
    }

    private void showStudentInfoDialog(Student student) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_show_courses, null);

        String[] from = new String[] {"course_name", "mark"};
        int[] to = new int[] { R.id.tvCourseName, R.id.tvCourseMark};

        ListView coursesListView = (ListView) dialogView.findViewById(R.id.lvDialogShowCoursesCourseItems);
        SimpleAdapter coursesAdapter =
                new SimpleAdapter(this, coursesToMap(student.getCourses()), R.layout.item_course, from, to);
        coursesListView.setAdapter(coursesAdapter);

        String averageMark = " " + String.valueOf(calculateAverage(student.getCourses()));
        ((TextView) dialogView.findViewById(R.id.tvDialogShowCoursesAverageMarkValue)).setText(averageMark);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_courses)
                .setView(dialogView)
                .show();
        ((Button) dialogView.findViewById(R.id.btnShowCoursesOk))
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private static List<HashMap<String, String>> coursesToMap(List<Course> courses) {
        List<HashMap<String, String>> coursesMap = new ArrayList<>();
        for (Course course : courses) {
            HashMap<String, String> map = new HashMap<>();
            map.put("course_name", course.getName());
            map.put("mark", String.valueOf(course.getMark()));
            coursesMap.add(map);
        }
        return coursesMap;
    }

    private static double calculateAverage(List<Course> courses) {
        double sum = 0;
        if (courses != null) {
            for (Course course : courses) {
                sum += course.getMark();
            }
            return sum / courses.size();
        }
        return sum;
    }

    @Override
    public void onDataStartSave() {
        showLoadingAnimation();
    }

    @Override
    public void onDataSaved() {
        hideLoadingAnimation();
        getDataFromDb(0);
    }

    @Override
    public void onDataLoadFinish(List<Student> students) {
        updateShowingStudents(students);
        hideLoadingAnimation();
    }
}
