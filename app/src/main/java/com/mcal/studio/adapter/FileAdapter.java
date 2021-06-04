package com.mcal.studio.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mcal.studio.R;
import com.mcal.studio.helper.ResourceHelper;

import java.io.File;
import java.util.List;

/**
 * Adapter to load main files into editor
 */
public class FileAdapter extends ArrayAdapter<String> {

    /**
     * Names of main files to edit
     */
    private List<String> openFiles;

    /**
     * Public constructor for adapter
     */
    public FileAdapter(Context context, List<String> files) {
        super(context, android.R.layout.simple_list_item_1, files);
        openFiles = files;
    }

    /**
     * View is created
     *
     * @param position    item position
     * @param convertView reuseable view
     * @param parent      parent view
     * @return view to display
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rootView;
        if (convertView == null) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_project, parent, false);
        } else {
            rootView = convertView;
        }

        ImageView imageView = rootView.findViewById(R.id.file_icon);
        TextView textView = rootView.findViewById(R.id.file_title);

        int resource = ResourceHelper.getIcon(new File(openFiles.get(position)));
        imageView.setImageResource(resource);
        textView.setText(getPageTitle(position));
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        return rootView;
    }

    /**
     * Dropdown view is created
     *
     * @param position    item position
     * @param convertView reuseable view
     * @param parent      parent view
     * @return view to display in dropdown
     */
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View rootView;
        if (convertView == null) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_project, parent, false);
        } else {
            rootView = convertView;
        }

        ImageView imageView = rootView.findViewById(R.id.file_icon);
        TextView textView = rootView.findViewById(R.id.file_title);

        imageView.setImageResource(ResourceHelper.getIcon(new File(openFiles.get(position))));
        textView.setText(getPageTitle(position));

        return rootView;
    }

    /**
     * Method to remove folder name from file
     *
     * @param position item position
     * @return new page title
     */
    private CharSequence getPageTitle(int position) {
        return new File(openFiles.get(position)).getName();
    }

    /**
     * Gets count of files open
     *
     * @return array size
     */
    @Override
    public int getCount() {
        return openFiles.size();
    }
}
