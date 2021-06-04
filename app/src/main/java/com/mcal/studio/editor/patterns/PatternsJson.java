package com.mcal.studio.editor.patterns;

import java.util.regex.Pattern;

public class PatternsJson {
    private final Pattern patternCommentsOther = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*");
    private final Pattern patternFunctions = Pattern.compile("n\\((.*?)\\)");
    private final Pattern patternNumbers = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    private final Pattern patternBooleans = Pattern.compile("\\b(true|false)\\b");
    private final Pattern patternStrings = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");

    public Pattern getPatternCommentsOther() {
        return patternCommentsOther;
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