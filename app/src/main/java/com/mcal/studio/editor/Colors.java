package com.mcal.studio.editor;

class Colors {
    private final int colorKeyword = 0xfff92672;
    private final int colorParams = 0xff64cbf4;
    private final int colorEnding = 0xff9a79dd;
    private final int colorFunctions = 0xffed5c00;

    private final int colorBuiltin = 0xff72b000;
    private final int colorComment = 0xffa0a0a0;
    private final int colorStrings = 0xffed5c00;

    private final int colorBuiltinDark = 0xffa6e22e;
    private final int colorCommentDark = 0xff75715e;
    private final int colorStringsDark = 0xffe6db74;

    private final int colorLineShadow = 0x10000000;
    private final int colorNumber = 0xffa0a0a0;
    private final int colorBackground = 0xfff8f8f8;
    private final int colorText = 0xff222222;

    private final int colorLineShadowDark = 0x10FFFFFF;
    private final int colorNumberDark = 0xffd3d3d3;
    private final int colorBackgroundDark = 0xff222222;
    private final int colorTextDark = 0xfff8f8f8;

    private boolean darkTheme;

    Colors(boolean darkTheme) {
        this.darkTheme = darkTheme;
    }

    int getColorKeyword() {
        return colorKeyword;
    }

    int getColorParams() {
        return colorParams;
    }

    int getColorEnding() {
        return colorEnding;
    }

    int getColorFunctions() {
        return colorFunctions;
    }

    int getColorBuiltin() {
        return darkTheme ? colorBuiltin : colorBuiltinDark;
    }

    int getColorComment() {
        return darkTheme ? colorComment : colorCommentDark;
    }

    int getColorStrings() {
        return darkTheme ? colorStrings : colorStringsDark;
    }

    int getColorLineShadow() {
        return darkTheme ? colorLineShadow : colorLineShadowDark;
    }

    int getColorNumber() {
        return darkTheme ? colorNumber : colorNumberDark;
    }

    int getColorBackground() {
        return darkTheme ? colorBackground : colorBackgroundDark;
    }

    int getColorText() {
        return darkTheme ? colorText : colorTextDark;
    }
}