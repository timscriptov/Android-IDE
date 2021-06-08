package com.mcal.studio.editor.patterns;

import java.util.regex.Pattern;

public class PatternsXml {
    public static final int colorStartElement = 0xff303F9F;
    public static final int colorEndElement = 0xff303F9F;
    public static final int colorStartTag = 0xFF7B1FA2;
    public static final int colorStrings = 0xff43A047;
    public static final int colorEndTag = 0xff1545C0;

    private final Pattern XML_START_ELEMENT = Pattern.compile("(?<=<)[^/\\s>?]+(?=(?:>|\\s))");
    private final Pattern XML_END_ELEMENT = Pattern.compile("(?<=</).*?(?=>)");
    private final Pattern XML_COMMENTS = Pattern.compile("(?:<!--)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)(?:.|\\n)*?-->");
    private final Pattern AFTER_XMLNS = Pattern.compile("(?<=xmlns:)[^=]+");
    private final Pattern XMLNS = Pattern.compile("\\sxmlns:");
    private final Pattern XML_START_TAG = Pattern.compile("(?<=\\s)[^:\\s=]*?(?=:)");
    private final Pattern XML_END_TAG = Pattern.compile("\\b.\\w*?(?:=)");
    private final Pattern XML_STRINGS = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");

    public Pattern getPatternAfterXmlns() {
        return AFTER_XMLNS;
    }

    public Pattern getPatternXmlns() {
        return XMLNS;
    }

    public Pattern getPatternStartTag() {
        return XML_START_TAG;
    }

    public Pattern getPatternEndTag() {
        return XML_END_TAG;
    }




    public Pattern getPatternStartElement() {
        return XML_START_ELEMENT;
    }

    public Pattern getPatternEndElement() {
        return XML_END_ELEMENT;
    }

    public Pattern getPatternComments() {
        return XML_COMMENTS;
    }

    public Pattern getPatternStrings() {
        return XML_STRINGS;
    }
}