package com.mcal.studio.git;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.mcal.studio.R;
import com.mcal.studio.adapter.ProjectAdapter;
import com.mcal.studio.helper.ProjectManager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

public class CloneTask extends GitTask {

    private static final String TAG = CloneTask.class.getSimpleName();
    private ProjectAdapter projectAdapter;

    CloneTask(Context context, View view, File repo, ProjectAdapter adapter) {
        super(context, view, repo, new String[]{"Cloning repository", "", ""});
        projectAdapter = adapter;
        id = 3;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            Git.cloneRepository()
                    .setURI(strings[0])
                    .setDirectory(repo)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(strings[1], strings[2]))
                    .setProgressMonitor(new BatchingProgressMonitor() {
                        @Override
                        protected void onUpdate(String taskName, int workCurr) {

                        }

                        @Override
                        protected void onEndTask(String taskName, int workCurr) {

                        }

                        @Override
                        protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
                            publishProgress(taskName, String.valueOf(percentDone), String.valueOf(workCurr), String.valueOf(workTotal));
                        }

                        @Override
                        protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
                            publishProgress(taskName, String.valueOf(workCurr), String.valueOf(workTotal));
                        }
                    })
                    .call();
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(rootView, e.toString(), Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            if (!ProjectManager.isValid(repo.getName())) {
                builder.setContentText("The repo was successfully cloned but it doesn't seem to be a " + context.getString(R.string.app_name) + " project.");
            } else {
                projectAdapter.insert(repo.getPath().substring(repo.getPath().lastIndexOf("/") + 1, repo.getPath().length()));
                builder.setContentText("Successfully cloned.");
            }
        } else {
            builder.setContentText("Unable to clone repo.");
        }

        builder.setProgress(0, 0, false);
        manager.notify(id, builder.build());
    }
}
