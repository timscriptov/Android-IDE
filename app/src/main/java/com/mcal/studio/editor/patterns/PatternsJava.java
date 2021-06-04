package com.mcal.studio.editor.patterns;

import java.util.regex.Pattern;

public class PatternsJava {
    private final Pattern patternComments = Pattern.compile("/\\**?\\*/|<!--.*");
    private final Pattern patternCommentsOther = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*");
    private final Pattern patternKeywords = Pattern.compile("\\b(import|extends|abstract|arguments|boolean|byte|char|class|const|double|enum|final|float|function|int|interface|long|native|package|private|protected|public|short|static|synchronized|transient|var|void|volatile)\\b");
    private final Pattern patternSymbols = Pattern.compile("(&|=|throw|new|for|if|else|>|<|^|\\+|-|\\s\\|\\s|break|try|catch|do|!|finally|default|case|switch|native|let|super|throws|return)");
    private final Pattern patternFunctions = Pattern.compile("n\\((.*?)\\)");
    private final Pattern patternNumbers = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    private final Pattern patternBooleans = Pattern.compile("\\b(true|false)\\b");
    private final Pattern patternStrings = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");

    public Pattern getPatternKeywords() {
        return patternKeywords;
    }

    public Pattern getPatternComments() {
        return patternComments;
    }

    public Pattern getPatternCommentsOther() {
        return patternCommentsOther;
    }

    public Pattern getPatternSymbols() {
        return patternSymbols;
    }

    public Pattern getPatternFunctions() {
        return patternFunctions;
    }

    public Pattern getPatternNumbers() {
        return patternNumbers;
    }

    public Pattern getPatternBooleans() {
        return patternBooleans;
    }

    public Pattern getPatternStrings() {
        return patternStrings;
    }
}