package com.mcal.studio.git;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

public class PullTask extends GitTask {

    private static final String TAG = PullTask.class.getSimpleName();

    PullTask(Context context, View view, File repo, String[] values) {
        super(context, view, repo, values);
        id = 5;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Git git = GitWrapper.getGit(rootView, repo);
        if (git != null) {
            try {
                git.pull()
                        .setRemote(params[0])
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(params[1], params[2]))
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

        return false;
    }
}
