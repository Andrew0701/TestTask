package com.example.andrew.testtask.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.andrew.testtask.R;
import com.example.andrew.testtask.models.Student;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    public static final String TAG = "StudentAdapter";

    private Context mContext;
    private List<Student> mStudents = new ArrayList<>();

    private StudentAdapterListener mListener;

    private int lastPosition = -1;

    public StudentAdapter(Context context) {
        mContext = context;
        if (context instanceof StudentAdapterListener) {
            mListener = (StudentAdapterListener) context;
        }
    }

    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StudentAdapter.ViewHolder holder, int position) {
        final Student student = mStudents.get(position);

        holder.mStudentName.setText(student.getFirstName() + " " + student.getLastName());
        holder.mStudentBirthday.setText(createDateString(student.getBirthday()));
        holder.mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onInfoButtonClick(student);
            }
        });

        setAnimation(holder.mContainer, position);
    }

    @Override
    public int getItemCount() {
        return mStudents.size();
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        holder.clearAnimation();
    }

    public void updateAdapterData(List<Student> newItems) {
        int positionStart = mStudents.size();
        int itemCount = positionStart + newItems.size();
        mStudents.addAll(newItems);
        notifyItemInserted(itemCount);
    }

    public void resetState() {
        lastPosition = -1;
        mStudents.clear();
        notifyDataSetChanged();
    }

    private String createDateString(String value) {
        Date date = new Date(Long.parseLong(value));
        return new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(date);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mContainer;
        public TextView mStudentName;
        public TextView mStudentBirthday;
        public ImageButton mInfoButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mContainer = (RelativeLayout) itemView.findViewById(R.id.containerItemStudent);
            mStudentName = (TextView) itemView.findViewById(R.id.tvStudentName);
            mStudentBirthday = (TextView) itemView.findViewById(R.id.tvStudentBirthday);
            mInfoButton = (ImageButton) itemView.findViewById(R.id.btnStudentInfo);
        }

        public void clearAnimation() {
            mContainer.clearAnimation();
        }
    }

    public interface StudentAdapterListener {
        void onInfoButtonClick(Student student);
    }
}
