package com.mcal.studio.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mcal.studio.R;
import com.mcal.studio.adapter.ProjectAdapter;
import com.mcal.studio.data.Preferences;
import com.mcal.studio.git.GitWrapper;
import com.mcal.studio.helper.Constants;
import com.mcal.studio.helper.DataValidator;
import com.mcal.studio.helper.ProjectManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity to show all main content
 */
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public static final int SETTINGS_CODE = 101;
    private static final int IMPORT_PROJECT = 102;

    /**
     * ProjectManager related stuff
     */
    String[] contents;
    ArrayList<String> contentsList;
    ProjectAdapter projectAdapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.project_list)
    RecyclerView projectsList;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.fab_create)
    FloatingActionButton cloneButton;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        contents = new File(Constants.PROJECT_ROOT).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && !name.equals(".git") && ProjectManager.isValid(name);
            }
        });

        if (contents != null) {
            contentsList = new ArrayList<>(Arrays.asList(contents));
        } else {
            contentsList = new ArrayList<>();
        }

        DataValidator.removeBroken(contentsList);
        projectAdapter = new ProjectAdapter(this, contentsList, coordinatorLayout, projectsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(projectAdapter);
        cloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] choices = {"Create a new project", "Clone a repository", "Import an external project"};
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Would you like to...")
                        .setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, choices), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        View rootView = View.inflate(MainActivity.this, R.layout.dialog_create, null);
                                        final AppCompatEditText name = rootView.findViewById(R.id.name);
                                        final AppCompatEditText packageName = rootView.findViewById(R.id.package_name);

                                        final AppCompatCheckBox androidx = rootView.findViewById(R.id.androidx);
                                        final AppCompatCheckBox firebase = rootView.findViewById(R.id.firebase);

                                        Preferences.setAndroidX(androidx.isChecked());
                                        Preferences.setFirebase(firebase.isChecked());

                                        name.setText(Preferences.getAppName());
                                        packageName.setText(Preferences.getAppPackageName());

                                        final AlertDialog createDialog = new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Create a new project")
                                                .setView(rootView)
                                                .setPositiveButton("CREATE", null)
                                                .setNegativeButton("CANCEL", null)
                                                .create();

                                        createDialog.show();
                                        createDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (DataValidator.validateCreate(MainActivity.this, name, packageName)) {
                                                    String appName = name.getText().toString();
                                                    String appPackageName = packageName.getText().toString();

                                                    Preferences.setStoreProject(appName, appPackageName);
                                                    ProjectManager.generate(
                                                            MainActivity.this,
                                                            appName,
                                                            appPackageName,
                                                            projectAdapter,
                                                            coordinatorLayout
                                                    );

                                                    createDialog.dismiss();
                                                }
                                            }
                                        });
                                        break;
                                    case 1:
                                        View cloneView = View.inflate(MainActivity.this, R.layout.dialog_clone, null);

                                        final AppCompatEditText file = cloneView.findViewById(R.id.clone_name);
                                        final AppCompatEditText remote = cloneView.findViewById(R.id.clone_url);
                                        final AppCompatEditText username = cloneView.findViewById(R.id.clone_username);
                                        final AppCompatEditText password = cloneView.findViewById(R.id.clone_password);

                                        file.setText(Preferences.getCloneName());
                                        remote.setText(Preferences.getRemote());

                                        final AlertDialog cloneDialog = new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Clone a repository")
                                                .setView(cloneView)
                                                .setPositiveButton("CLONE", null)
                                                .setNegativeButton(R.string.cancel, null)
                                                .create();

                                        cloneDialog.show();
                                        cloneDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (DataValidator.validateClone(MainActivity.this, file, remote)) {
                                                    String remoteStr = remote.getText().toString();
                                                    if (!remoteStr.contains("://")) {
                                                        remoteStr = "https://" + remoteStr;
                                                    }

                                                    String cloneName = file.getText().toString();
                                                    Preferences.setCloneName(cloneName);
                                                    Preferences.setRemote(remoteStr);

                                                    GitWrapper.clone(
                                                            MainActivity.this,
                                                            coordinatorLayout,
                                                            new File(Constants.PROJECT_ROOT + File.separator + cloneName),
                                                            projectAdapter,
                                                            remoteStr,
                                                            username.getText().toString(),
                                                            password.getText().toString()
                                                    );

                                                    cloneDialog.dismiss();
                                                }
                                            }
                                        });

                                        break;
                                    case 2:
                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                        intent.setType("file/*");
                                        if (intent.resolveActivity(getPackageManager()) != null) {
                                            startActivityForResult(intent, IMPORT_PROJECT);
                                        }

                                        break;
                                }
                            }
                        })
                        .show();
            }
        });

        projectsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    cloneButton.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && cloneButton.isShown()) cloneButton.hide();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, SETTINGS_CODE);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when returning from an external activity
     *
     * @param requestCode code used to request intent
     * @param resultCode  code returned from activity
     * @param data        data returned from activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTINGS_CODE:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case IMPORT_PROJECT:
                if (resultCode == RESULT_OK) {
                    Uri fileUri = data.getData();
                    if (fileUri != null) {
                        final File file = new File(fileUri.getPath());
                        View rootView = View.inflate(MainActivity.this, R.layout.dialog_import, null);

                        final AppCompatEditText name = rootView.findViewById(R.id.name_layout);
                        final AppCompatEditText packageName = rootView.findViewById(R.id.author_layout);

                        name.setText(file.getParentFile().getName());
                        packageName.setText(Preferences.getAppPackageName());

                        final AlertDialog createDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Import an external project")
                                .setIcon(R.drawable.ic_action_import)
                                .setView(rootView)
                                .setPositiveButton("IMPORT", null)
                                .setNegativeButton("CANCEL", null)
                                .create();

                        createDialog.show();
                        createDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (DataValidator.validateCreate(MainActivity.this, name, packageName)) {
                                    String appName = name.getText().toString();
                                    String appPackageName = packageName.getText().toString();

                                    Preferences.setStoreProject(appName, appPackageName);
                                    ProjectManager._import(
                                            file.getParentFile().getPath(),
                                            appName,
                                            projectAdapter,
                                            coordinatorLayout
                                    );

                                    createDialog.dismiss();
                                }
                            }
                        });
                    }
                }

                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        contentsList = new ArrayList<>(Arrays.asList(contents));
        DataValidator.removeBroken(contentsList);
        for (Iterator iterator = contentsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            if (!string.toLowerCase(Locale.getDefault()).contains(newText)) {
                iterator.remove();
            }
        }

        projectAdapter = new ProjectAdapter(MainActivity.this, contentsList, coordinatorLayout, projectsList);
        projectsList.setAdapter(projectAdapter);
        return true;
    }

    @Override
    public boolean onClose() {
        contentsList = new ArrayList<>(Arrays.asList(contents));
        DataValidator.removeBroken(contentsList);
        projectAdapter = new ProjectAdapter(MainActivity.this, contentsList, coordinatorLayout, projectsList);
        projectsList.setAdapter(projectAdapter);
        return false;
    }
}
