package com.mcal.studio.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.mcal.studio.R;
import com.mcal.studio.git.GitWrapper;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RemotesAdapter extends RecyclerView.Adapter<RemotesAdapter.RemotesHolder> {

    private ArrayList<String> remotesList;
    private Context context;
    private View remotesView;
    private File repo;

    public RemotesAdapter(Context context, View view, File repo) {
        remotesList = GitWrapper.getRemotes(view, repo);
        this.context = context;
        this.remotesView = view;
        this.repo = repo;
    }

    @Override
    public RemotesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote, parent, false);
        return new RemotesHolder(view);
    }

    @Override
    public void onBindViewHolder(final RemotesHolder holder, final int position) {
        holder.name.setText(remotesList.get(position));
        holder.url.setText(GitWrapper.getRemoteUrl(remotesView, repo, remotesList.get(position)));
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View pullView = View.inflate(context, R.layout.dialog_pull, null);
                final Spinner spinner1 = pullView.findViewById(R.id.remotes_spinner);
                final TextInputEditText pullUsername = pullView.findViewById(R.id.pull_username);
                final TextInputEditText pullPassword = pullView.findViewById(R.id.pull_password);
                spinner1.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, remotesList));

                new AlertDialog.Builder(context)
                        .setTitle("Fetch from remote")
                        .setView(pullView)
                        .setPositiveButton("FETCH", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                GitWrapper.fetch(context, remotesView, repo, (String) spinner1.getSelectedItem(), pullUsername.getText().toString(), pullPassword.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int newPos = holder.getAdapterPosition();
                new AlertDialog.Builder(context)
                        .setTitle("Remove " + remotesList.get(newPos) + "?")
                        .setMessage("This remote will be removed permanently.")
                        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GitWrapper.removeRemote(remotesView, repo, remotesList.get(newPos));
                                remotesList.remove(remotesList.get(newPos));
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return remotesList.size();
    }

    public void add(String remote, String url) {
        GitWrapper.addRemote(remotesView, repo, remote, url);
        remotesList.add(remote);
        notifyDataSetChanged();
    }

    class RemotesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.remote_name)
        TextView name;
        @BindView(R.id.remote_url)
        TextView url;
        View rootView;

        RemotesHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            rootView = view;
        }
    }
}
