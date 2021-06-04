package com.mcal.studio.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mcal.studio.R;
import com.mcal.studio.activity.ProjectActivity;
import com.mcal.studio.helper.Constants;
import com.mcal.studio.helper.ProjectManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to list all projects
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.MyViewHolder> {

    /**
     * Context used for various purposes such as loading files and inflating layouts
     */
    private Context context;

    /**
     * Array of objects to fill list
     */
    private ArrayList<String> projects;

    private CoordinatorLayout layout;

    private RecyclerView recyclerView;
    private String projectName;
    private File projectDir;

    /**
     * public Constructor
     *
     * @param context  loading files and inflating etc
     * @param projects objects to fill list
     */
    public ProjectAdapter(Context context, ArrayList<String> projects, CoordinatorLayout layout, RecyclerView recyclerView) {
        this.context = context;
        this.projects = projects;
        this.layout = layout;
        this.recyclerView = recyclerView;
    }

    public void insert(String project) {
        projects.add(project);
        int position = projects.indexOf(project);
        notifyItemInserted(position);
        recyclerView.scrollToPosition(position);
    }

    public void remove(int position) {
        projects.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * When view holder is created
     *
     * @param parent   parent view
     * @param viewType type of view
     * @return ProjectAdapter.ViewHolder
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Collections.sort(projects);
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new MyViewHolder(itemView);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder   view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        projectName = projects.get(holder.getAdapterPosition());
        projectDir = new File(Constants.PROJECT_ROOT + File.separator + projectName);

        holder.name.setText(projectName);
        holder.path.setText(projectDir.toString());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProjectActivity.class);
                intent.putExtra("project", projects.get(holder.getAdapterPosition()));
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                if (Build.VERSION.SDK_INT >= 21) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                }

                ((AppCompatActivity) context).startActivityForResult(intent, 0);
            }
        });

        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.delete) + " " + projects.get(holder.getAdapterPosition()) + "?")
                        .setMessage(R.string.change_undone)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String project = projects.get(holder.getAdapterPosition());
                                ProjectManager.deleteProject(project);
                                remove(holder.getAdapterPosition());

                                Snackbar.make(
                                        layout,
                                        "Deleted " + project + ".",
                                        Snackbar.LENGTH_LONG
                                ).show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                return true;
            }
        });
    }

    /**
     * Gets number of projects
     *
     * @return array size
     */
    @Override
    public int getItemCount() {
        return projects.size();
    }

    /**
     * View holder class for logs
     */
    class MyViewHolder extends RecyclerView.ViewHolder {

        /**
         * Views for view holder
         */
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.path)
        TextView path;
        @BindView(R.id.project_layout)
        LinearLayout layout;

        /**
         * public Constructor
         *
         * @param view root view
         */
        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
