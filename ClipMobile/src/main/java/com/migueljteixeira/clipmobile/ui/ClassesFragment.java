package com.migueljteixeira.clipmobile.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.migueljteixeira.clipmobile.R;
import com.migueljteixeira.clipmobile.adapters.ClassListViewAdapter;
import com.migueljteixeira.clipmobile.entities.Student;
import com.migueljteixeira.clipmobile.entities.StudentClass;
import com.migueljteixeira.clipmobile.settings.ClipSettings;
import com.migueljteixeira.clipmobile.util.tasks.GetStudentClassesTask;
import com.uwetrottmann.androidutils.AndroidUtils;

import java.util.List;

import butterknife.ButterKnife;

public class ClassesFragment extends BaseFragment
        implements GetStudentClassesTask.OnTaskFinishedListener<Student> {
    
    private ListView mListView;
    private GetStudentClassesTask mTask;
    private ClassListViewAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classes, container, false);
        ButterKnife.bind(this, view);

        mListView = (ListView) view.findViewById(R.id.list_view);

        // Show progress spinner
        showProgressSpinner(true);

        // Start AsyncTask
        mTask = new GetStudentClassesTask(getActivity(), ClassesFragment.this);
        AndroidUtils.executeOnPool(mTask);

        return view;
    }

    @Override
    public void onTaskFinished(Student result) {
        if(!isAdded())
            return;

        showProgressSpinner(false);

        // Server is unavailable right now
        if(result == null) return;

        mListView.setAdapter(getAdapterItems(result));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem item = (ListViewItem) adapter.getItem(position);

                // Save class selected and internal classId
                ClipSettings.saveStudentClassSelected(getActivity(), item.number);
                ClipSettings.saveStudentClassIdSelected(getActivity(), item.id);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment fragment = new ClassesDocsFragment();

                // Replace current fragment by ClassesDocsFragment
                fm.beginTransaction().replace(R.id.content_frame, fragment,
                        ClassesDocsFragment.FRAGMENT_TAG).commit();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cancelTasks(mTask);
    }

    private ClassListViewAdapter getAdapterItems(Student result) {
        adapter = new ClassListViewAdapter(getActivity());

        int semester = ClipSettings.getSemesterSelected(getActivity());
        if (result.getClasses() == null || result.getClasses().get(semester) == null)
            return adapter;

        List<StudentClass> classes = result.getClasses().get(semester);
        for (StudentClass c : classes)
            adapter.add(new ListViewItem(c.getId(), c.getName(), c.getNumber()));

        return adapter;
    }

    public static class ListViewItem {
        public String id, name, number;

        public ListViewItem(String id, String name, String number) {
            this.id = id;
            this.name = name;
            this.number = number;
        }
    }

}
