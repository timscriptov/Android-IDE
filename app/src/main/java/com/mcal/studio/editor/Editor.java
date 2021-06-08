package com.mcal.studio.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;

import com.mcal.studio.R;
import com.mcal.studio.data.Preferences;
import com.mcal.studio.editor.patterns.Patterns;
import com.mcal.studio.editor.patterns.PatternsGradle;
import com.mcal.studio.editor.patterns.PatternsJava;
import com.mcal.studio.editor.patterns.PatternsJson;
import com.mcal.studio.editor.patterns.PatternsXml;
import com.mcal.studio.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Editor class to handle code highlighting etc
 * Derived from: https://github.com/markusfisch/ShaderEditor/blob/master/app/src/main/java/de/markusfisch/android/shadereditor/widget/ShaderEditor.java
 */
public class Editor extends AppCompatMultiAutoCompleteTextView {

    private final String TAG = Editor.class.getSimpleName();

    /**
     * Handler used to update colours when code is changed
     */
    private final Handler updateHandler = new Handler();
    /**
     * Context used to get preferences
     */
    private final Context context;
    /**
     * Custom listener
     */
    public OnTextChangedListener onTextChangedListener = null;
    /**
     * Delay used to update code
     */
    public int updateDelay = 2000;
    int currentLine = 0;
    int lineDiff = 0;
    /**
     * Type of code set
     */
    private CodeType codeType;
    /**
     * Checks if code has been changed
     */
    private boolean fileModified = true;
    /**
     * Rect to represent each line
     */
    private Rect lineRect;
    /**
     * Paint to draw line numbers
     */
    private Paint numberPaint, lineShadowPaint;
    private boolean isHighlighting;

    private Colors colors;
    private Patterns patterns;
    private PatternsGradle patternsGradle;
    private PatternsJson patternsJson;
    private PatternsXml patternsXml;
    private PatternsJava patternsJava;
    /**
     * Runnable used to update colours when code is changed
     */
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isHighlighting) {
                Editable e = getText();
                if (onTextChangedListener != null)
                    onTextChangedListener.onTextChanged(e.toString());
                highlightWithoutChange(e);
            }
        }
    };
    private boolean hasLineNumbers;

    /**
     * Public constructor
     *
     * @param context used to get preferences
     */
    public Editor(Context context) {
        this(context, null);
    }

    /**
     * Public constructor
     *
     * @param context used to get preferences
     * @param attrs   not used
     */
    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    /**
     * Highlights given code and sets it as text
     *
     * @param text to be highlighted
     */
    public void setTextHighlighted(CharSequence text) {
        cancelUpdate();

        fileModified = false;
        setText(highlight(new SpannableStringBuilder(text)));
        fileModified = true;

        if (onTextChangedListener != null) onTextChangedListener.onTextChanged(text.toString());
    }

    /**
     * Code used to initialise editor
     */
    private void init() {
        colors = new Colors(!Preferences.getDarkThemeEditor());
        patterns = new Patterns();
        patternsGradle = new PatternsGradle();
        patternsJson = new PatternsJson();
        patternsXml = new PatternsXml();
        patternsJava = new PatternsJava();
        lineRect = new Rect();
        hasLineNumbers = Preferences.getShowLineNumbers();

        lineShadowPaint = new Paint();
        lineShadowPaint.setStyle(Paint.Style.FILL);
        lineShadowPaint.setColor(colors.getColorLineShadow());

        if (hasLineNumbers) {
            numberPaint = new Paint();
            numberPaint.setStyle(Paint.Style.FILL);
            numberPaint.setAntiAlias(true);
            numberPaint.setTextSize(ResourceHelper.dpToPx(context, 14));
            numberPaint.setTextAlign(Paint.Align.RIGHT);
            numberPaint.setColor(colors.getColorNumber());
        } else {
            int padding = ResourceHelper.dpToPx(context, 8);
            if (Build.VERSION.SDK_INT > 15) {
                setPaddingRelative(padding, padding, padding, 0);
            } else {
                setPadding(padding, padding, padding, 0);
            }
        }

        setLineSpacing(0, 1.2f);
        setBackgroundColor(colors.getColorBackground());
        setTextColor(colors.getColorText());
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Inconsolata-Regular.ttf"));
        setCustomSelectionActionModeCallback(new EditorCallback());
        setHorizontallyScrolling(!Preferences.isWordwrap());
        setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (fileModified && end - start == 1 && start < source.length() && dstart < dest.length()) {
                    char c = source.charAt(start);
                    if (c == '\n') return autoIndent(source, dest, dstart, dend);
                }

                return source;
            }
        }});

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setupAutoComplete();
            }
        });
    }

    /**
     * Prevent code from updating
     */
    private void cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable);
    }

    /**
     * Used in main runnable
     *
     * @param e text to be highlighted
     */
    private void highlightWithoutChange(Editable e) {
        fileModified = false;
        highlight(e);
        fileModified = true;
    }

    /**
     * Main method used for highlighting i.e. this is where the magic happens
     *
     * @param e text to be highlighted
     * @return highlighted text
     */
    private Editable highlight(Editable e) {
        isHighlighting = true;

        try {
            if (e.length() == 0) return e;
            if (hasSpans(e)) clearSpans(e);

            Matcher m;
            switch (codeType) {
                case HTML:
                    for (m = patterns.getPatternKeywords().matcher(e); m.find(); ) {
                        if (e.toString().charAt(m.start() - 1) == '<' || e.toString().charAt(m.start() - 1) == '/') {
                            e.setSpan(new ForegroundColorSpan(colors.getColorKeyword()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    for (m = patterns.getPatternBuiltins().matcher(e); m.find(); ) {
                        if (e.toString().charAt(m.start() - 1) == ' ' && e.toString().charAt(m.end()) == '=') {
                            e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    for (m = patterns.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternComments().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case CSS:
                    for (m = patterns.getPatternKeywords().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorKeyword()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternParams().matcher(e); m.find(); ) {
                        if (e.toString().charAt(m.end()) == ':') {
                            e.setSpan(new ForegroundColorSpan(colors.getColorParams()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    for (int index = e.toString().indexOf(":"); index >= 0; index = e.toString().indexOf(":", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorEnding()), index + 1, e.toString().indexOf(";", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("."); index >= 0; index = e.toString().indexOf(".", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), index + 1, e.toString().indexOf("{", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("#"); index >= 0; index = e.toString().indexOf("#", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), index + 1, e.toString().indexOf("{", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternEndings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorEnding()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternCommentsOther().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case JS:
                    for (m = patterns.getPatternDatatypes().matcher(e); m.find(); ) {
                        if (e.toString().charAt(m.end()) == ' ') {
                            e.setSpan(new ForegroundColorSpan(colors.getColorParams()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    for (m = patterns.getPatternFunctions().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorFunctions()), m.start() + 2, m.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternSymbols().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorKeyword()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("null"); index >= 0; index = e.toString().indexOf("null", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorEnding()), index, index + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternNumbers().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternBooleans().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patterns.getPatternCommentsOther().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case JAVA:
                    for (m = patternsJava.getPatternKeywords().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorKeyword()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJava.getPatternFunctions().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorFunctions()), m.start() + 2, m.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJava.getPatternSymbols().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorKeyword()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("null"); index >= 0; index = e.toString().indexOf("null", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorEnding()), index, index + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJava.getPatternNumbers().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJava.getPatternBooleans().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJava.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJava.getPatternCommentsOther().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case GRADLE:
                    for (m = patternsGradle.getPatternFunctions().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorFunctions()), m.start() + 2, m.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsGradle.getPatternSymbols().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorKeyword()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("null"); index >= 0; index = e.toString().indexOf("null", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorEnding()), index, index + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsGradle.getPatternNumbers().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsGradle.getPatternBooleans().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsGradle.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsGradle.getPatternCommentsOther().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case JSON:
                    for (m = patternsJson.getPatternFunctions().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorFunctions()), m.start() + 2, m.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (int index = e.toString().indexOf("null"); index >= 0; index = e.toString().indexOf("null", index + 1)) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorEnding()), index, index + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJson.getPatternNumbers().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJson.getPatternBooleans().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJson.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsJson.getPatternCommentsOther().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                case XML:
                    for (m = patternsXml.getPatternStartElement().matcher(e); m.find(); ) {
                        if (e.toString().charAt(m.start() - 1) == '<' || e.toString().charAt(m.start() - 1) == '/') {
                            e.setSpan(new ForegroundColorSpan(PatternsXml.colorStartElement), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    for (m = patternsXml.getPatternEndElement().matcher(e); m.find(); ) {
                        if (e.toString().charAt(m.start() - 1) == '<' || e.toString().charAt(m.start() - 1) == '/') {
                            e.setSpan(new ForegroundColorSpan(PatternsXml.colorEndElement), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    for (m = patternsXml.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsXml.getPatternStrings().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(PatternsXml.colorStrings), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsXml.getPatternStartTag().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(PatternsXml.colorStartTag), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsXml.getPatternEndTag().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(PatternsXml.colorEndTag), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsXml.getPatternComments().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(colors.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsXml.getPatternAfterXmlns().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(PatternsXml.colorStartTag), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    for (m = patternsXml.getPatternXmlns().matcher(e); m.find(); ) {
                        e.setSpan(new ForegroundColorSpan(PatternsXml.colorEndTag), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }

        isHighlighting = false;
        return e;
    }

    /**
     * Method to set the code type
     *
     * @param type of code
     */
    public void setType(CodeType type) {
        codeType = type;
    }

    /**
     * Removes all spans
     *
     * @param e text to be cleared
     */
    private void clearSpans(Editable e) {
        {
            ForegroundColorSpan spans[] = e.getSpans(0, e.length(), ForegroundColorSpan.class);
            for (int n = spans.length; n-- > 0; ) e.removeSpan(spans[n]);
        }
    }

    private boolean hasSpans(Editable e) {
        return e.getSpans(0, e.length(), ForegroundColorSpan.class).length > 0;
    }

    /**
     * Method used to draw line numbers onto code
     *
     * @param canvas used for drawing
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (hasLineNumbers) {
            int cursorLine = getCurrentCursorLine();
            int lineBounds;
            int lineHeight = getLineHeight();
            int lineCount = getLineCount();
            List<CharSequence> lines = getLines();

            for (int i = 0; i < lineCount; i++) {
                lineBounds = getLineBounds(i - lineDiff, lineRect);
                if (lines.get(i).toString().endsWith("\n") || i == lineCount - 1) {
                    if (hasLineNumbers)
                        canvas.drawText(String.valueOf(currentLine + 1), 64, lineBounds, numberPaint);
                    currentLine += 1;
                    lineDiff = 0;
                } else {
                    lineDiff += 1;
                }

                if (i == cursorLine) {
                    if (hasLineNumbers) {
                        canvas.drawRect(0, 10 + lineBounds - lineHeight, 72, lineBounds + 8, lineShadowPaint);
                    }
                }

                if (i == lineCount - 1) {
                    currentLine = 0;
                    lineDiff = 0;
                }
            }
        } else {
            int cursorLine = getCurrentCursorLine();
            int lineBounds;
            int lineHeight = getLineHeight();

            lineBounds = getLineBounds(cursorLine - lineDiff, lineRect);
            canvas.drawRect(0, 8 + lineBounds - lineHeight, getWidth(), lineBounds + 12, lineShadowPaint);
        }

        super.onDraw(canvas);
    }

    public List<CharSequence> getLines() {
        final List<CharSequence> lines = new ArrayList<>();
        final Layout layout = getLayout();

        if (layout != null) {
            final int lineCount = layout.getLineCount();
            final CharSequence text = layout.getText();

            for (int i = 0, startIndex = 0; i < lineCount; i++) {
                final int endIndex = layout.getLineEnd(i);
                lines.add(text.subSequence(startIndex, endIndex));
                startIndex = endIndex;
            }
        }
        return lines;
    }

    public int getCurrentCursorLine() {
        int selectionStart = Selection.getSelectionStart(getText());
        Layout layout = getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    /**
     * Method used for indenting code automatically
     *
     * @param source the main code
     * @param dest   the new code
     * @param dstart start of the code
     * @param dend   end of the code
     * @return indented code
     */
    private CharSequence autoIndent(CharSequence source, Spanned dest, int dstart, int dend) {
        String indent = "";
        int istart = dstart - 1;
        int iend;

        boolean dataBefore = false;
        int pt = 0;

        for (; istart > -1; --istart) {
            char c = dest.charAt(istart);

            if (c == '\n')
                break;

            if (c != ' ' &&
                    c != '\t') {
                if (!dataBefore) {
                    if (c == '{' ||
                            c == '+' ||
                            c == '-' ||
                            c == '*' ||
                            c == '/' ||
                            c == '%' ||
                            c == '^' ||
                            c == '=')
                        --pt;

                    dataBefore = true;
                }

                if (c == '(')
                    --pt;
                else if (c == ')')
                    ++pt;
            }
        }

        if (istart > -1) {
            char charAtCursor = dest.charAt(dstart);

            for (iend = ++istart;
                 iend < dend;
                 ++iend) {
                char c = dest.charAt(iend);

                if (charAtCursor != '\n' &&
                        c == '/' &&
                        iend + 1 < dend &&
                        dest.charAt(iend) == c) {
                    iend += 2;
                    break;
                }

                if (c != ' ' &&
                        c != '\t')
                    break;
            }

            indent += dest.subSequence(istart, iend);
        }

        if (pt < 0)
            indent += "\t";

        return source + indent;
    }

    private void setupAutoComplete() {
        String[] items = patterns.getPatternKeywords().pattern().replace("(", "").replace(")", "").substring(2, patterns.getPatternKeywords().pattern().length() - 2).split("\\|");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, items);
        setAdapter(adapter);

        setThreshold(1);
        setTokenizer(new Tokenizer() {
            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;

                while (i > 0 && text.charAt(i - 1) != '<') {
                    i--;
                }

                if (i < 1 || text.charAt(i - 1) != '<') {
                    return cursor;
                }

                return i;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();

                while (i < len) {
                    if (text.charAt(i) == ' ' || text.charAt(i) == '>' || text.charAt(i) == '/') {
                        return i;
                    } else {
                        i++;
                    }
                }

                return i;
            }

            @Override
            public CharSequence terminateToken(CharSequence text) {
                int i = text.length();

                while (i > 0 && text.charAt(i - 1) == ' ') {
                    i--;
                }

                if (i > 0 && text.charAt(i - 1) == ' ') {
                    return text;
                } else {
                    if (text instanceof Spanned) {
                        SpannableString sp = new SpannableString(text);
                        TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
                        return sp;
                    } else {
                        return text + "></" + text + ">";
                    }
                }
            }
        });

        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Layout layout = getLayout();
                int position = getSelectionStart();
                int line = layout.getLineForOffset(position);
                int baseline = layout.getLineBaseline(line);
                int bottom = getHeight();
                int x = (int) layout.getPrimaryHorizontal(position);

                if (x + (getWidth() / 2) > getWidth()) {
                    x = getWidth() / 2;
                }

                setDropDownVerticalOffset(baseline - bottom);
                setDropDownHorizontalOffset(x);

                setDropDownHeight(getHeight() / 3);
                setDropDownWidth(getWidth() / 2);
            }

            @Override
            public void afterTextChanged(Editable e) {
                cancelUpdate();

                if (!fileModified) return;

                updateHandler.postDelayed(updateRunnable, updateDelay);
            }
        });
    }

    public enum CodeType {
        HTML, CSS, JS, GRADLE, JSON, XML, JAVA
    }

    /**
     * Listens to when text is changed
     */
    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }

    private class EditorCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(0, 1, 3, R.string.refactor);
            menu.add(0, 2, 3, R.string.comment);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case 1:
                    String selected = getSelectedString();
                    View layout = View.inflate(context, R.layout.dialog_refactor, null);

                    final AppCompatEditText replaceFrom = layout.findViewById(R.id.replace_from);
                    final AppCompatEditText replaceTo = layout.findViewById(R.id.replace_to);
                    replaceFrom.setText(selected);

                    final AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(layout)
                            .setPositiveButton(R.string.replace, null)
                            .create();

                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String replaceFromStr = replaceFrom.getText().toString();
                            String replaceToStr = replaceTo.getText().toString();

                            if (replaceFromStr.isEmpty()) {
                                replaceFrom.setError(context.getString(R.string.empty_field_no_no));
                            } else if (replaceToStr.isEmpty()) {
                                replaceTo.setError(context.getString(R.string.empty_field_no_no));
                            } else {
                                setText(getText().toString().replace(replaceFromStr, replaceToStr));
                                dialog.dismiss();
                            }
                        }
                    });

                    return true;
                case 2:
                    String startComment = "", endComment = "";
                    switch (codeType) {
                        case HTML:
                            startComment = "<!-- ";
                            endComment = " -->";
                            break;
                        case XML:
                            startComment = "<!-- ";
                            endComment = " -->";
                            break;
                        case CSS:
                            startComment = "/* ";
                            endComment = " */";
                            break;
                        case JS:
                            startComment = "/** ";
                            endComment = " */";
                            break;
                        case JAVA:
                            startComment = "/* ";
                            endComment = " */";
                            break;
                        case JSON:
                            startComment = "/* ";
                            endComment = " */";
                            break;
                        case GRADLE:
                            startComment = "/* ";
                            endComment = " */";
                            break;
                    }

                    setText(getText().insert(getSelectionStart(), startComment).insert(getSelectionEnd(), endComment));
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        private String getSelectedString() {
            return getText().toString().substring(getSelectionStart(), getSelectionEnd());
        }
    }
}