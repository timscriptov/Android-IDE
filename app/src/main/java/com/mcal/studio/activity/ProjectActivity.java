package com.mcal.studio.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.mcal.studio.R;
import com.mcal.studio.adapter.FileAdapter;
import com.mcal.studio.adapter.GitLogsAdapter;
import com.mcal.studio.builder.ApkMaker;
import com.mcal.studio.builder.BuildCallback;
import com.mcal.studio.data.Preferences;
import com.mcal.studio.fragment.EditorFragment;
import com.mcal.studio.fragment.ImageFragment;
import com.mcal.studio.git.GitWrapper;
import com.mcal.studio.helper.Clipboard;
import com.mcal.studio.helper.Constants;
import com.mcal.studio.helper.ProjectManager;
import com.mcal.studio.helper.ResourceHelper;
import com.mcal.studio.utils.InstallProvider;
import com.mcal.studio.utils.Utils;
import com.mcal.studio.widget.DiffView;
import com.mcal.studio.widget.holder.FileTreeHolder;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to work on selected project
 */
public class ProjectActivity extends AppCompatActivity {

    private static final String TAG = ProjectActivity.class.getSimpleName();

    private static final int VIEW_CODE = 99;

    /**
     * Intent code to import image
     */
    private static final int IMPORT_FILE = 101;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.file_browser)
    LinearLayout fileBrowser;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.name)
    TextView headerTitle;
    @BindView(R.id.root_overflow)
    ImageButton overflow;
    /**
     * Currently open files
     */
    private ArrayList<String> openFiles;
    /**
     * Spinner and Adapter to handle files
     */
    private Spinner fileSpinner;
    private ArrayAdapter<String> fileAdapter;
    /**
     * Drawer related stuffs
     */
    private ActionBarDrawerToggle toggle;
    /**
     * ProjectManager definitions
     */
    private String projectName;
    private File projectDir, indexFile;
    private TreeNode rootNode;
    private AndroidTreeView treeView;
    private ProgressDialog progressDialog;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        projectName = getIntent().getStringExtra("project");
        projectDir = new File(Constants.PROJECT_ROOT + File.separator + projectName);
        indexFile = ProjectManager.getIndexFile(projectName);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("files")) {
            openFiles = getIntent().getStringArrayListExtra("files");
        } else {
            openFiles = new ArrayList<>();
            openFiles.add(indexFile.getPath());
        }

        fileSpinner = new Spinner(this);
        fileAdapter = new FileAdapter(this, openFiles);
        fileSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        fileSpinner.setAdapter(fileAdapter);
        toolbar.addView(fileSpinner);
        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_fragment, getFragment(openFiles.get(position)))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.action_drawer_open, R.string.action_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                headerTitle.setText(projectDir.getName());
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        rootNode = TreeNode.root();
        setupFileTree(rootNode, projectDir);
        treeView = new AndroidTreeView(ProjectActivity.this, rootNode);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultViewHolder(FileTreeHolder.class);
        treeView.setDefaultContainerStyle(R.style.AppTheme_TreeNodeStyle);
        treeView.setDefaultNodeClickListener(new TreeNode.TreeNodeClickListener() {
            @Override
            public void onClick(TreeNode node, Object value) {
                FileTreeHolder.FileTreeItem item = (FileTreeHolder.FileTreeItem) value;
                if (node.isLeaf() && item.file.isFile()) {
                    if (openFiles.contains(item.file.getPath())) {
                        setFragment(item.file.getPath(), false);
                        drawerLayout.closeDrawers();
                    } else {
                        if (!ProjectManager.isBinaryFile(item.file)) {
                            setFragment(item.file.getPath(), true);
                            drawerLayout.closeDrawers();
                        } else if (ProjectManager.isImageFile(item.file)) {
                            setFragment(item.file.getPath(), true);
                            drawerLayout.closeDrawers();
                        } else {
                            Snackbar.make(drawerLayout, R.string.not_text_file, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        treeView.setDefaultNodeLongClickListener(new TreeNode.TreeNodeLongClickListener() {
            @Override
            public boolean onLongClick(final TreeNode node, Object value) {
                final FileTreeHolder.FileTreeItem item = (FileTreeHolder.FileTreeItem) value;
                switch (item.file.getName()) {
                    case "build.gradle":
                        return false;
                    default:
                        new AlertDialog.Builder(ProjectActivity.this)
                                .setTitle(getString(R.string.delete) + " " + item.file.getName() + "?")
                                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final boolean[] delete = {true, false};
                                        final String file = item.file.getName();
                                        final TreeNode parent = node.getParent();
                                        treeView.removeNode(node);
                                        removeFragment(item.file.getPath());

                                        final Snackbar snackbar = Snackbar.make(
                                                drawerLayout,
                                                "Deleted " + file + ".",
                                                Snackbar.LENGTH_LONG
                                        );

                                        snackbar.setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                delete[0] = false;
                                                snackbar.dismiss();
                                            }
                                        });

                                        snackbar.addCallback(new Snackbar.Callback() {
                                            @Override
                                            public void onDismissed(Snackbar snackbar, int event) {
                                                super.onDismissed(snackbar, event);
                                                if (!delete[1]) {
                                                    if (delete[0]) {
                                                        if (item.file.isDirectory()) {
                                                            try {
                                                                FileUtils.deleteDirectory(item.file);
                                                            } catch (IOException e) {
                                                                Log.e(TAG, e.toString());
                                                            }
                                                        } else {
                                                            if (!item.file.delete()) {
                                                                Log.e(TAG, "Failed to delete " + item.file.getPath());
                                                            }
                                                        }
                                                    } else {
                                                        treeView.addNode(parent, node);
                                                    }

                                                    delete[1] = true;
                                                }
                                            }
                                        });

                                        snackbar.show();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();

                        return true;
                }
            }
        });

        fileBrowser.addView(treeView.getView());
        headerTitle.setText(projectDir.getName());

        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(ProjectActivity.this, overflow);
                menu.getMenuInflater().inflate(R.menu.menu_file_options, menu.getMenu());
                menu.getMenu().findItem(R.id.action_copy).setVisible(false);
                menu.getMenu().findItem(R.id.action_cut).setVisible(false);
                menu.getMenu().findItem(R.id.action_rename).setVisible(false);
                menu.getMenu().findItem(R.id.action_paste).setEnabled(Clipboard.getInstance().getCurrentFile() != null);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_new_file:
                                View newFileRootView = View.inflate(ProjectActivity.this, R.layout.dialog_input_single, null);
                                final AppCompatEditText fileName = newFileRootView.findViewById(R.id.input_text);
                                fileName.setHint(R.string.file_name);

                                final AlertDialog newFileDialog = new AlertDialog.Builder(ProjectActivity.this)
                                        .setTitle("New file")
                                        .setView(newFileRootView)
                                        .setPositiveButton(R.string.create, null)
                                        .setNegativeButton(R.string.cancel, null)
                                        .create();

                                newFileDialog.show();
                                newFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (fileName.getText().toString().isEmpty()) {
                                            fileName.setError("Please enter a file name");
                                        } else {
                                            newFileDialog.dismiss();
                                            String fileStr = fileName.getText().toString();
                                            File newFile = new File(projectDir, fileStr);
                                            try {
                                                FileUtils.writeStringToFile(newFile, "\n", Charset.defaultCharset());
                                            } catch (IOException e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }

                                            Snackbar.make(drawerLayout, "Created " + fileStr + ".", Snackbar.LENGTH_SHORT).show();
                                            TreeNode newFileNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(newFile), newFile, drawerLayout));
                                            rootNode.addChild(newFileNode);
                                            treeView.setRoot(rootNode);
                                            treeView.addNode(rootNode, newFileNode);
                                        }
                                    }
                                });

                                return true;
                            case R.id.action_new_folder:
                                View newFolderRootView = View.inflate(ProjectActivity.this, R.layout.dialog_input_single, null);
                                final AppCompatEditText folderName = newFolderRootView.findViewById(R.id.input_text);
                                folderName.setHint(R.string.folder_name);

                                final AlertDialog newFolderDialog = new AlertDialog.Builder(ProjectActivity.this)
                                        .setTitle("New folder")
                                        .setView(newFolderRootView)
                                        .setPositiveButton(R.string.create, null)
                                        .setNegativeButton(R.string.cancel, null)
                                        .create();

                                newFolderDialog.show();
                                newFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (folderName.getText().toString().isEmpty()) {
                                            folderName.setError("Please enter a folder name");
                                        } else {
                                            newFolderDialog.dismiss();
                                            String folderStr = folderName.getText().toString();
                                            File newFolder = new File(projectDir, folderStr);
                                            try {
                                                FileUtils.forceMkdir(newFolder);
                                            } catch (IOException e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }

                                            Snackbar.make(drawerLayout, "Created " + folderStr + ".", Snackbar.LENGTH_SHORT).show();
                                            TreeNode newFolderNode = new TreeNode(new FileTreeHolder.FileTreeItem(R.drawable.ic_folder, newFolder, drawerLayout));
                                            rootNode.addChild(newFolderNode);
                                            treeView.setRoot(rootNode);
                                            treeView.addNode(rootNode, newFolderNode);
                                        }
                                    }
                                });

                                return true;
                            case R.id.action_paste:
                                File currentFile = Clipboard.getInstance().getCurrentFile();
                                TreeNode currentNode = Clipboard.getInstance().getCurrentNode();
                                FileTreeHolder.FileTreeItem currentItem = (FileTreeHolder.FileTreeItem) currentNode.getValue();
                                switch (Clipboard.getInstance().getType()) {
                                    case COPY:
                                        if (currentFile.isDirectory()) {
                                            try {
                                                FileUtils.copyDirectoryToDirectory(currentFile, projectDir);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            try {
                                                FileUtils.copyFileToDirectory(currentFile, projectDir);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }

                                        Snackbar.make(drawerLayout, "Successfully copied " + currentFile.getName() + ".", Snackbar.LENGTH_SHORT).show();
                                        File copyFile = new File(projectDir, currentFile.getName());
                                        TreeNode copyNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(copyFile), copyFile, currentItem.view));
                                        rootNode.addChild(copyNode);
                                        treeView.setRoot(rootNode);
                                        treeView.addNode(rootNode, copyNode);
                                        break;
                                    case CUT:
                                        if (currentFile.isDirectory()) {
                                            try {
                                                FileUtils.moveDirectoryToDirectory(currentFile, projectDir, false);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            try {
                                                FileUtils.moveFileToDirectory(currentFile, projectDir, false);
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                                Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }

                                        Snackbar.make(drawerLayout, "Successfully moved " + currentFile.getName() + ".", Snackbar.LENGTH_SHORT).show();
                                        Clipboard.getInstance().setCurrentFile(null);
                                        File cutFile = new File(projectDir, currentFile.getName());
                                        TreeNode cutNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(cutFile), cutFile, currentItem.view));
                                        rootNode.addChild(cutNode);
                                        treeView.setRoot(rootNode);
                                        treeView.addNode(rootNode, cutNode);
                                        treeView.removeNode(Clipboard.getInstance().getCurrentNode());
                                        break;
                                }
                                return true;
                        }

                        return false;
                    }
                });

                menu.show();
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(0x00000000);
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(projectName);
            this.setTaskDescription(description);
        }
    }

    private void setupFileTree(TreeNode root, File f) {
        File[] files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return !name.startsWith(".");
            }
        });

        for (File file : files) {
            if (file.isDirectory()) {
                TreeNode folderNode = new TreeNode(new FileTreeHolder.FileTreeItem(R.drawable.ic_folder, file, drawerLayout));
                setupFileTree(folderNode, file);
                root.addChild(folderNode);
            } else {
                TreeNode fileNode = new TreeNode(new FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(file), file, drawerLayout));
                root.addChild(fileNode);
            }
        }
    }

    private void removeFragment(String file) {
        openFiles.remove(file);
        fileAdapter.remove(file);
        fileAdapter.notifyDataSetChanged();
    }

    /**
     * Open file when selected by setting the correct fragment
     *
     * @param file file to open
     * @param add  whether to add to adapter
     */
    private void setFragment(String file, boolean add) {
        if (add) {
            fileAdapter.add(file);
            fileAdapter.notifyDataSetChanged();
        }

        fileSpinner.setSelection(fileAdapter.getPosition(file), true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.editor_fragment, getFragment(file))
                .commit();
    }

    /**
     * Method to get the type of fragment dependent on the file type
     *
     * @param title file name
     * @return fragment to be committed
     */
    public Fragment getFragment(String title) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", fileAdapter.getCount());
        bundle.putString("location", title);
        if (ProjectManager.isImageFile(new File(title))) {
            return Fragment.instantiate(this, ImageFragment.class.getName(), bundle);
        } else {
            return Fragment.instantiate(this, EditorFragment.class.getName(), bundle);
        }
    }

    /**
     * Used to enable/disable certain git functions
     * based on whether project is a git repo
     *
     * @param menu menu to work with
     * @return whether preparation is handled correctly
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isGitRepo = new File(projectDir, ".git").exists() && new File(projectDir, ".git").isDirectory();
        boolean canCommit = false;
        boolean canCheckout = false;
        boolean hasRemotes = false;
        boolean isHtml = ((String) fileSpinner.getSelectedItem()).endsWith(".html");
        if (isGitRepo) {
            canCommit = GitWrapper.canCommit(drawerLayout, projectDir);
            canCheckout = GitWrapper.canCheckout(drawerLayout, projectDir);
            hasRemotes = GitWrapper.getRemotes(drawerLayout, projectDir) != null &&
                    GitWrapper.getRemotes(drawerLayout, projectDir).size() > 0;
        }

        menu.findItem(R.id.action_view).setEnabled(isHtml);
        menu.findItem(R.id.action_git_add).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_commit).setEnabled(canCommit);
        menu.findItem(R.id.action_git_push).setEnabled(hasRemotes);
        menu.findItem(R.id.action_git_pull).setEnabled(hasRemotes);
        menu.findItem(R.id.action_git_log).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_diff).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_status).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_branch).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_remote).setEnabled(isGitRepo);
        menu.findItem(R.id.action_git_branch_checkout).setEnabled(canCheckout);

        return true;
    }

    /**
     * Called after activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    /**
     * Called when config is changed
     *
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called when back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Called when menu is created
     *
     * @param menu object that holds menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_project, menu);
        return true;
    }

    /**
     * Called when menu item is selected
     *
     * @param item selected menu item
     * @return true if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.run:
                compileProject();
                return true;
            case R.id.action_run:
                Intent runIntent = new Intent(ProjectActivity.this, WebActivity.class);
                runIntent.putExtra("url", "file:///" + indexFile.getPath());
                runIntent.putExtra("appName", projectName);
                startActivity(runIntent);
                return true;
            case R.id.action_view:
                Intent viewIntent = new Intent(ProjectActivity.this, ViewActivity.class);
                viewIntent.putExtra("html_path", openFiles.get(fileSpinner.getSelectedItemPosition()));
                startActivityForResult(viewIntent, VIEW_CODE);
                return true;
            case R.id.action_import_file:
                Intent fontIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fontIntent.setType("file/*");
                if (fontIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(fontIntent, IMPORT_FILE);
                }
                return true;
            case R.id.action_git_init:
                GitWrapper.init(ProjectActivity.this, projectDir, drawerLayout);
                return true;
            case R.id.action_git_add:
                GitWrapper.add(drawerLayout, projectDir);
                return true;
            case R.id.action_git_commit:
                View view = View.inflate(ProjectActivity.this, R.layout.dialog_input_single, null);
                final AppCompatEditText editText = view.findViewById(R.id.input_text);
                editText.setHint(R.string.commit_message);

                final AlertDialog commitDialog = new AlertDialog.Builder(ProjectActivity.this)
                        .setTitle(R.string.git_commit)
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(R.string.git_commit, null)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .create();

                commitDialog.show();
                commitDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!editText.getText().toString().isEmpty()) {
                            GitWrapper.commit(ProjectActivity.this, drawerLayout, projectDir, editText.getText().toString());
                            commitDialog.dismiss();
                        } else {
                            editText.setError(getString(R.string.commit_message_empty));
                        }
                    }
                });
                return true;
            case R.id.action_git_push:
                View pushView = View.inflate(ProjectActivity.this, R.layout.dialog_push, null);
                final Spinner spinner = pushView.findViewById(R.id.remotes_spinner);
                final CheckBox dryRun = pushView.findViewById(R.id.dry_run);
                final CheckBox force = pushView.findViewById(R.id.force);
                final CheckBox thin = pushView.findViewById(R.id.thin);
                final CheckBox tags = pushView.findViewById(R.id.tags);
                final AppCompatEditText pushUsername = pushView.findViewById(R.id.push_username);
                final AppCompatEditText pushPassword = pushView.findViewById(R.id.push_password);
                spinner.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, GitWrapper.getRemotes(drawerLayout, projectDir)));

                new AlertDialog.Builder(ProjectActivity.this)
                        .setTitle("Push changes")
                        .setView(pushView)
                        .setPositiveButton("PUSH", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                GitWrapper.push(ProjectActivity.this, drawerLayout, projectDir, (String) spinner.getSelectedItem(), new boolean[]{dryRun.isChecked(), force.isChecked(), thin.isChecked(), tags.isChecked()}, pushUsername.getText().toString(), pushPassword.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                return true;
            case R.id.action_git_pull:
                View pullView = View.inflate(ProjectActivity.this, R.layout.dialog_pull, null);
                final Spinner spinner1 = pullView.findViewById(R.id.remotes_spinner);
                final AppCompatEditText pullUsername = pullView.findViewById(R.id.pull_username);
                final AppCompatEditText pullPassword = pullView.findViewById(R.id.pull_password);
                spinner1.setAdapter(new ArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, GitWrapper.getRemotes(drawerLayout, projectDir)));

                new AlertDialog.Builder(ProjectActivity.this)
                        .setTitle("Push changes")
                        .setView(pullView)
                        .setPositiveButton("PULL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                GitWrapper.pull(ProjectActivity.this, drawerLayout, projectDir, (String) spinner1.getSelectedItem(), pullUsername.getText().toString(), pullPassword.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                return true;
            case R.id.action_git_log:
                List<RevCommit> commits = GitWrapper.getCommits(drawerLayout, projectDir);
                View layoutLog = View.inflate(this, R.layout.sheet_logs, null);
                if (Preferences.isNightModeEnabled()) {
                    layoutLog.setBackgroundColor(0xFF333333);
                }

                RecyclerView logsList = layoutLog.findViewById(R.id.logs_list);
                RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
                RecyclerView.Adapter adapter = new GitLogsAdapter(ProjectActivity.this, commits);

                logsList.setLayoutManager(manager);
                logsList.setAdapter(adapter);

                BottomSheetDialog dialogLog = new BottomSheetDialog(this);
                dialogLog.setContentView(layoutLog);
                dialogLog.show();
                return true;
            case R.id.action_git_diff:
                final int[] chosen = {-1, -1};
                final List<RevCommit> commitsToDiff = GitWrapper.getCommits(drawerLayout, projectDir);
                final CharSequence[] commitNames = new CharSequence[commitsToDiff.size()];
                for (int i = 0; i < commitNames.length; i++) {
                    commitNames[i] = commitsToDiff.get(i).getShortMessage();
                }

                new AlertDialog.Builder(ProjectActivity.this)
                        .setTitle("Choose first commit")
                        .setSingleChoiceItems(commitNames, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                chosen[0] = i;
                                new AlertDialog.Builder(ProjectActivity.this)
                                        .setTitle("Choose second commit")
                                        .setSingleChoiceItems(commitNames, -1, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                                chosen[1] = i;
                                                SpannableString string = GitWrapper.diff(drawerLayout, projectDir, commitsToDiff.get(chosen[0]).getId(), commitsToDiff.get(chosen[1]).getId());
                                                View rootView = View.inflate(ProjectActivity.this, R.layout.dialog_diff, null);
                                                DiffView diffView = rootView.findViewById(R.id.diff_view);
                                                diffView.setDiffText(string);

                                                new AlertDialog.Builder(ProjectActivity.this)
                                                        .setView(rootView)
                                                        .show();
                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();

                return true;
            case R.id.action_git_status:
                View layoutStatus = View.inflate(this, R.layout.item_git_status, null);
                if (Preferences.isNightModeEnabled()) {
                    layoutStatus.setBackgroundColor(0xFF333333);
                }

                TextView conflict, added, changed, missing, modified, removed, uncommitted, untracked, untrackedFolders;
                conflict = layoutStatus.findViewById(R.id.status_conflicting);
                added = layoutStatus.findViewById(R.id.status_added);
                changed = layoutStatus.findViewById(R.id.status_changed);
                missing = layoutStatus.findViewById(R.id.status_missing);
                modified = layoutStatus.findViewById(R.id.status_modified);
                removed = layoutStatus.findViewById(R.id.status_removed);
                uncommitted = layoutStatus.findViewById(R.id.status_uncommitted);
                untracked = layoutStatus.findViewById(R.id.status_untracked);
                untrackedFolders = layoutStatus.findViewById(R.id.status_untracked_folders);

                GitWrapper.status(drawerLayout, projectDir, conflict, added, changed, missing, modified, removed, uncommitted, untracked, untrackedFolders);

                BottomSheetDialog dialogStatus = new BottomSheetDialog(this);
                dialogStatus.setContentView(layoutStatus);
                dialogStatus.show();
                return true;
            case R.id.action_git_branch_new:
                View branchView = View.inflate(ProjectActivity.this, R.layout.dialog_git_branch, null);
                final EditText editText5 = branchView.findViewById(R.id.branch_name);
                final CheckBox checkBox = branchView.findViewById(R.id.checkout);
                checkBox.setText(R.string.checkout);

                final AlertDialog branchDialog = new AlertDialog.Builder(ProjectActivity.this)
                        .setTitle("New branch")
                        .setView(branchView)
                        .setPositiveButton(R.string.create, null)
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                branchDialog.show();
                branchDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!editText5.getText().toString().isEmpty()) {
                            GitWrapper.createBranch(ProjectActivity.this, drawerLayout, projectDir, editText5.getText().toString(), checkBox.isChecked());
                            branchDialog.dismiss();
                        } else {
                            editText5.setError(getString(R.string.branch_name_empty));
                        }
                    }
                });
                return true;
            case R.id.action_git_branch_remove:
                final List<Ref> branchesList = GitWrapper.getBranches(drawerLayout, projectDir);
                final CharSequence[] itemsMultiple = new CharSequence[branchesList.size()];
                for (int i = 0; i < itemsMultiple.length; i++) {
                    itemsMultiple[i] = branchesList.get(i).getName();
                }

                final boolean[] checkedItems = new boolean[itemsMultiple.length];
                final List<String> toDelete = new ArrayList<>();

                new AlertDialog.Builder(this)
                        .setMultiChoiceItems(itemsMultiple, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                if (b) {
                                    toDelete.add(itemsMultiple[i].toString());
                                } else {
                                    toDelete.remove(itemsMultiple[i].toString());
                                }
                            }
                        })
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                GitWrapper.deleteBranch(drawerLayout, projectDir, toDelete.toArray(new String[toDelete.size()]));
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.close, null)
                        .setTitle("Delete branches")
                        .show();

                return true;
            case R.id.action_git_branch_checkout:
                final List<Ref> branches = GitWrapper.getBranches(drawerLayout, projectDir);
                int checkedItem = -1;
                CharSequence[] items = new CharSequence[branches.size()];
                for (int i = 0; i < items.length; i++) {
                    items[i] = branches.get(i).getName();
                }

                for (int i = 0; i < items.length; i++) {
                    String branch = GitWrapper.getCurrentBranch(drawerLayout, projectDir);
                    if (branch != null) {
                        if (branch.equals(items[i])) {
                            checkedItem = i;
                        }
                    }
                }

                new AlertDialog.Builder(this)
                        .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                GitWrapper.checkout(ProjectActivity.this, drawerLayout, projectDir, branches.get(i).getName());
                            }
                        })
                        .setNegativeButton(R.string.close, null)
                        .setTitle("Checkout branch")
                        .show();

                return true;
            case R.id.action_git_remote:
                Intent remoteIntent = new Intent(ProjectActivity.this, RemotesActivity.class);
                remoteIntent.putExtra("project_file", projectDir.getPath());
                startActivity(remoteIntent);
                return true;
            case R.id.action_analyze:
                Intent analyzeIntent = new Intent(ProjectActivity.this, AnalyzeActivity.class);
                analyzeIntent.putExtra("project_file", projectDir.getPath());
                startActivity(analyzeIntent);
                return true;
        }

        return false;
    }

    private void compileProject() {
        if (projectDir.exists() || projectDir.isDirectory() || new File(projectDir, "app/build.json").exists()) {
            ApkMaker maker = new ApkMaker(ProjectActivity.this);
            maker.setProjectDir(projectDir.getAbsolutePath());
            maker.setBuildListener(new BuildCallback() {
                @SuppressLint("InvalidWakeLockTag")
                @Override
                public void onStart() {
                    progressDialog = new ProgressDialog(ProjectActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BUILDER");
                    wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    wakeLock.release();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ProjectActivity.this);
                    dialog.setTitle("Errors");
                    dialog.setMessage(message);
                    dialog.setPositiveButton("Ok", null);
                    dialog.create().show();
                }

                @Override
                public void onProgress(String progress) {
                    progressDialog.setMessage(progress);
                }

                @Override
                public void onSuccess(File apk) {
                    progressDialog.dismiss();
                    wakeLock.release();
                    Utils.toast(getApplicationContext(), "Build Complete.");
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ProjectActivity.this);
                    dialog.setTitle("Success");
                    dialog.setMessage("The project has been compiled successfully. Install it?");
                    dialog.setPositiveButton("Install", (p1, p2) -> {
                        InstallProvider.install(ProjectActivity.this, new File(projectDir + File.separator + "app" + File.separator + "release" + File.separator + "app-signed.apk"));
                    });
                    dialog.setNegativeButton("Cancel", null);
                    dialog.create().show();
                }
            });
            maker.build();
        }
    }

    /**
     * Handle different results from intents
     *
     * @param requestCode code used to start intent
     * @param resultCode  result given by intent
     * @param data        data given by intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMPORT_FILE:
                if (resultCode == RESULT_OK) {
                    final Uri fileUri = data.getData();
                    View view = View.inflate(ProjectActivity.this, R.layout.dialog_input_single, null);
                    final AppCompatEditText editText = view.findViewById(R.id.input_text);
                    editText.setHint(R.string.file_name);

                    final AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.name)
                            .setView(view)
                            .setCancelable(false)
                            .setPositiveButton(R.string.import_not_java, null)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .create();

                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (editText.getText().toString().isEmpty()) {
                                editText.setError("Please enter a name");
                            } else {
                                dialog.dismiss();
                                if (ProjectManager.importFile(ProjectActivity.this, projectName, fileUri, editText.getText().toString())) {
                                    Snackbar.make(drawerLayout, R.string.file_success, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(drawerLayout, R.string.file_fail, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }

                break;
            case VIEW_CODE:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(ProjectActivity.this, ProjectActivity.class);
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        intent.putExtras(extras);
                    }

                    intent.addFlags(getIntent().getFlags());
                    intent.putStringArrayListExtra("files", openFiles);
                    startActivity(intent);
                    finish();
                }

                break;
        }

        setupFileTree(rootNode, projectDir);
        treeView.setRoot(rootNode);
    }
}
