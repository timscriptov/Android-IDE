package com.mcal.studio.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mcal.studio.R;
import com.mcal.studio.editor.Editor;
import com.mcal.studio.helper.ResourceHelper;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment used to edit files
 */
public class EditorFragment extends Fragment {

    /**
     * Log TAG
     */
    private static final String TAG = EditorFragment.class.getSimpleName();
    @BindView(R.id.file_content)
    Editor editText;
    @BindView(R.id.symbol_tab)
    FloatingActionButton symbolTab;
    @BindView(R.id.symbol_one)
    Button symbolOne;
    @BindView(R.id.symbol_two)
    Button symbolTwo;
    @BindView(R.id.symbol_three)
    Button symbolThree;
    @BindView(R.id.symbol_four)
    Button symbolFour;
    @BindView(R.id.symbol_five)
    Button symbolFive;
    @BindView(R.id.symbol_six)
    Button symbolSix;
    @BindView(R.id.symbol_seven)
    Button symbolSeven;
    @BindView(R.id.symbol_eight)
    Button symbolEight;
    private Unbinder unbinder;

    /**
     * public Constructor
     */
    public EditorFragment() {
        setRetainInstance(true);
    }

    /**
     * Called when fragment view is created
     *
     * @param inflater           used to inflate layout resource
     * @param container          parent view
     * @param savedInstanceState state to be restored
     * @return inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String location = getArguments().getString("location");
        File file = null;
        if (location != null) {
            file = new File(location);
        }

        if (file == null || !file.exists()) {
            TextView textView = new TextView(getActivity());
            int padding = ResourceHelper.dpToPx(getActivity(), 48);
            textView.setPadding(padding, padding, padding, padding);
            textView.setGravity(Gravity.CENTER);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alert_error, 0, 0, 0);
            textView.setText(R.string.file_problem);
            return textView;
        }

        View rootView = inflater.inflate(R.layout.fragment_editor, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        String filename = file.getName();
        editText.setTextSize(16);
        editText.setTypeface(Typeface.MONOSPACE);
        if (filename.endsWith(".html")) {
            editText.setType(Editor.CodeType.HTML);
            setSymbol(editText, symbolTab, "\t\t");
            setSymbol(editText, symbolOne, "<");
            setSymbol(editText, symbolTwo, "/");
            setSymbol(editText, symbolThree, ">");
            setSymbol(editText, symbolFour, "\"");
            setSymbol(editText, symbolFive, "=");
            setSymbol(editText, symbolSix, "!");
            setSymbol(editText, symbolSeven, "-");
            setSymbol(editText, symbolEight, "/");
        } else if (filename.endsWith(".css")) {
            editText.setType(Editor.CodeType.CSS);
            setSymbol(editText, symbolTab, "\t\t\t\t");
            setSymbol(editText, symbolOne, "{");
            setSymbol(editText, symbolTwo, "}");
            setSymbol(editText, symbolThree, ":");
            setSymbol(editText, symbolFour, ",");
            setSymbol(editText, symbolFive, "#");
            setSymbol(editText, symbolSix, ".");
            setSymbol(editText, symbolSeven, ";");
            setSymbol(editText, symbolEight, "-");
        } else if (filename.endsWith(".js")) {
            editText.setType(Editor.CodeType.JS);
            setSymbol(editText, symbolTab, "\t\t\t\t");
            setSymbol(editText, symbolOne, "{");
            setSymbol(editText, symbolTwo, "}");
            setSymbol(editText, symbolThree, "(");
            setSymbol(editText, symbolFour, ")");
            setSymbol(editText, symbolFive, "!");
            setSymbol(editText, symbolSix, "=");
            setSymbol(editText, symbolSeven, ":");
            setSymbol(editText, symbolEight, "?");
        } else if (filename.endsWith(".gradle")) {
            editText.setType(Editor.CodeType.GRADLE);
            setSymbol(editText, symbolTab, "\t\t\t\t");
            setSymbol(editText, symbolOne, "{");
            setSymbol(editText, symbolTwo, "}");
            setSymbol(editText, symbolThree, "(");
            setSymbol(editText, symbolFour, ")");
            setSymbol(editText, symbolFive, "!");
            setSymbol(editText, symbolSix, "=");
            setSymbol(editText, symbolSeven, ":");
            setSymbol(editText, symbolEight, "?");
        } else if (filename.endsWith(".json")) {
            editText.setType(Editor.CodeType.JSON);
            setSymbol(editText, symbolTab, "\t\t\t\t");
            setSymbol(editText, symbolOne, "{");
            setSymbol(editText, symbolTwo, "}");
            setSymbol(editText, symbolThree, "(");
            setSymbol(editText, symbolFour, ")");
            setSymbol(editText, symbolFive, "!");
            setSymbol(editText, symbolSix, "=");
            setSymbol(editText, symbolSeven, ":");
            setSymbol(editText, symbolEight, "?");
        } else if (filename.endsWith(".xml")) {
            editText.setType(Editor.CodeType.XML);
            setSymbol(editText, symbolTab, "\t\t");
            setSymbol(editText, symbolOne, "<");
            setSymbol(editText, symbolTwo, "/");
            setSymbol(editText, symbolThree, ">");
            setSymbol(editText, symbolFour, "\"");
            setSymbol(editText, symbolFive, "=");
            setSymbol(editText, symbolSix, "!");
            setSymbol(editText, symbolSeven, "-");
            setSymbol(editText, symbolEight, "/");
        } else if (filename.endsWith(".java")) {
            editText.setType(Editor.CodeType.JAVA);
            setSymbol(editText, symbolTab, "\t\t\t\t");
            setSymbol(editText, symbolOne, "{");
            setSymbol(editText, symbolTwo, "}");
            setSymbol(editText, symbolThree, "(");
            setSymbol(editText, symbolFour, ")");
            setSymbol(editText, symbolFive, "!");
            setSymbol(editText, symbolSix, "=");
            setSymbol(editText, symbolSeven, ":");
            setSymbol(editText, symbolEight, "?");
        }

        String contents = getContents(location);
        editText.setTextHighlighted(contents);
        final File finalFile = file;
        editText.onTextChangedListener = new Editor.OnTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                try {
                    FileUtils.writeStringToFile(finalFile, editText.getText().toString(), Charset.defaultCharset(), false);
                } catch (IOException e) {
                    Log.wtf(TAG, e.toString());
                }
            }
        };

        return rootView;
    }

    /**
     * Method to set symbol
     *
     * @param editor editor to set for
     * @param button which button to set to
     * @param symbol which symbol to set
     */
    private void setSymbol(Editor editor, Button button, String symbol) {
        button.setText(symbol);
        button.setOnClickListener(new SymbolClickListener(editor, symbol));
    }

    /**
     * Method to set symbol
     *
     * @param editor editor to set for
     * @param button which image button to set to
     * @param symbol which symbol to set
     */
    private void setSymbol(Editor editor, ImageButton button, String symbol) {
        button.setOnClickListener(new SymbolClickListener(editor, symbol));
    }

    private String getContents(String location) {
        try {
            InputStream inputStream = new FileInputStream(location);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append('\n');
            }

            return builder.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return "Unable to read file!";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Listener to add symbol to text
     */
    private class SymbolClickListener implements View.OnClickListener {

        /**
         * Editor to add symbol to
         */
        private Editor mEditor;

        /**
         * Symbol to add to editor
         */
        private String mSymbol;

        /**
         * Constructor
         *
         * @param editor see mEditor
         * @param symbol see mSymbol
         */
        SymbolClickListener(Editor editor, String symbol) {
            mEditor = editor;
            mSymbol = symbol;
        }

        /**
         * Called when view is clicked
         *
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v) {
            int start = Math.max(mEditor.getSelectionStart(), 0);
            int end = Math.max(mEditor.getSelectionEnd(), 0);
            mEditor.getText().replace(Math.min(start, end), Math.max(start, end),
                    mSymbol, 0, mSymbol.length());
        }
    }
}
