package com.mcal.studio.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mcal.studio.R;
import com.mcal.studio.adapter.RemotesAdapter;
import com.mcal.studio.git.GitWrapper;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RemotesActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.remotes_layout)
    CoordinatorLayout remotesLayout;
    @BindView(R.id.remotes_list)
    RecyclerView remotesList;
    @BindView(R.id.new_remote)
    FloatingActionButton newRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remotes);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        final File repo = new File(getIntent().getStringExtra("project_file"));
        final RemotesAdapter remotesAdapter = new RemotesAdapter(this, remotesLayout, repo);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(remotesList.getContext(),
                layoutManager.getOrientation());
        remotesList.addItemDecoration(dividerItemDecoration);
        remotesList.setLayoutManager(layoutManager);
        remotesList.setAdapter(remotesAdapter);

        newRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View cloneView = View.inflate(RemotesActivity.this, R.layout.dialog_remote_add, null);
                final AppCompatEditText file = cloneView.findViewById(R.id.clone_name);
                final AppCompatEditText remote = cloneView.findViewById(R.id.clone_url);

                new AlertDialog.Builder(RemotesActivity.this)
                        .setTitle("Add remote")
                        .setView(cloneView)
                        .setPositiveButton(R.string.git_add, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GitWrapper.addRemote(remotesLayout, repo, file.getText().toString(), remote.getText().toString());
                                remotesAdapter.add(file.getText().toString(), remote.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        remotesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    newRemote.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && newRemote.isShown()) newRemote.hide();
            }
        });
    }
}
