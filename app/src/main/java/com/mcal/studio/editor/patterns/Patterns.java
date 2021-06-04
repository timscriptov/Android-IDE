package com.mcal.studio.editor.patterns;

import java.util.regex.Pattern;

public class Patterns {
    private final Pattern patternKeywords = Pattern.compile("\\b(a|address|app|applet|area|b|base|basefont|bgsound|big|blink|blockquote|body|br|button|caption|center|cite|code|col|colgroup|comment|dd|dfn|dir|div|dl|dt|em|embed|fieldset|font|form|frame|frameset|h1|h2|h3|h4|h5|h6|head|hr|html|htmlplus|hype|i|iframe|img|input|ins|del|isindex|kbd|label|legend|li|link|listing|map|marquee|menu|meta|multicol|nobr|noembed|noframes|noscript|ol|option|p|param|plaintext|pre|s|samp|script|select|small|sound|spacer|span|strike|strong|style|sub|sup|table|tbody|td|textarea|tfoot|th|thead|title|tr|tt|u|var|wbr|xmp|import)\\b");
    private final Pattern patternBuiltins = Pattern.compile("\\b(charset|lang|href|onclick|onmouseover|onmouseout|code|codebase|width|height|align|vspace|hspace|name|archive|mayscript|alt|shape|coords|target|nohref|size|color|face|src|loop|bgcolor|background|text|vlink|alink|bgproperties|topmargin|leftmargin|marginheight|marginwidth|onload|onunload|onfocus|onblur|stylesrc|scroll|clear|type|value|valign|span|compact|pluginspage|pluginurl|hidden|autostart|playcount|volume|controller|mastersound|starttime|endtime|point-size|weight|action|method|enctype|onsubmit|onreset|scrolling|noresize|frameborder|bordercolor|cols|rows|framespacing|border|noshade|longdesc|ismap|usemap|lowsrc|naturalsizeflag|nosave|dynsrc|controls|start|suppress|maxlength|checked|language|onchange|onkeypress|onkeyup|onkeydown|autocomplete|prompt|for|rel|rev|media|direction|behaviour|scrolldelay|scrollamount|http-equiv|content|gutter|defer|event|multiple|readonly|cellpadding|cellspacing|rules|bordercolorlight|bordercolordark|summary|colspan|rowspan|nowrap|halign|disabled|accesskey|tabindex|id|class)\\b");
    private final Pattern patternParams = Pattern.compile("\\b(azimuth|background-attachment|background-color|background-image|background-position|background-repeat|background|border-collapse|border-color|border-spacing|border-style|border-top|border-right|border-bottom|border-left|border-top-color|border-right-color|border-left-color|border-bottom-color|border-top-style|border-right-style|border-bottom-style|border-left-style|border-top-width|border-right-width|border-bottom-width|border-left-width|border-width|border|bottom|caption-side|clear|clip|color|content|counter-increment|counter-reset|cue-after|cue-before|cue|cursor|direction|display|elevation|empty-cells|float|font-family|font-size|font-style|font-variant|font-weight|font|height|left|letter-spacing|line-height|list-style-image|list-style-position|list-style-type|list-style|margin-left|margin-right|margin-top|margin-bottom|margin|max-height|max-width|min-height|min-width|orphans|outline-color|outline-style|outline-width|outline|overflow|padding-top|padding-right|padding-bottom|padding-left|padding|page-break-after|page-break-before|page-break-inside|pause-after|pause-before|pause|pitch-range|pitch|play-during|position|quotes|richness|right|speak-header|speak-numeral|speak-punctuation|speak|speech-rate|stress|table-layout|text-align|text-decoration|text-indent|text-transform|top|unicode-bidi|vertical-align|visibility|voice-family|volume|white-space|widows|width|word-spacing|z-index)\\b");
    private final Pattern patternComments = Pattern.compile("/\\**?\\*/|<!--.*");
    private final Pattern patternCommentsOther = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*");
    private final Pattern patternEndings = Pattern.compile("(em|rem|px|pt|%)");
    private final Pattern patternDatatypes = Pattern.compile("\\b(abstract|arguments|boolean|byte|char|class|const|double|enum|final|float|function|int|interface|long|native|package|private|protected|public|short|static|synchronized|transient|var|void|volatile)\\b");
    private final Pattern patternSymbols = Pattern.compile("(&|=|throw|new|for|if|else|>|<|^|\\+|-|\\s\\|\\s|break|try|catch|do|!|finally|default|case|switch|native|let|super|throws|return)");
    private final Pattern patternFunctions = Pattern.compile("n\\((.*?)\\)");
    private final Pattern patternNumbers = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    private final Pattern patternBooleans = Pattern.compile("\\b(true|false)\\b");
    private final Pattern patternStrings = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");

    public Pattern getPatternKeywords() {
        return patternKeywords;
    }

    public Pattern getPatternBuiltins() {
        return patternBuiltins;
    }

    public Pattern getPatternParams() {
        return patternParams;
    }

    public Pattern getPatternComments() {
        return patternComments;
    }

    public Pattern getPatternCommentsOther() {
        return patternCommentsOther;
    }

    public Pattern getPatternEndings() {
        return patternEndings;
    }

    public Pattern getPatternDatatypes() {
        return patternDatatypes;
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