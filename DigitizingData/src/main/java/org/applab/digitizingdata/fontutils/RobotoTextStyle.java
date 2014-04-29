package org.applab.digitizingdata.fontutils;

/**
 * Implementation of {@link TextStyle} defining the possible values for the 'textStyle' attribute
 * using the Roboto font.
 * Created by evelina on 16/01/2014.
 */
public enum RobotoTextStyle implements TextStyle {

    NORMAL("normal", "fonts/roboto-regular.ttf"),
    LIGHT("light", "fonts/roboto-light.ttf"),
    BOLD("bold", "fonts/roboto-bold.ttf"),
    CONDENSED("condensed", "fonts/robotocondensed-regular.ttf"),
    CONDENSED_LIGHT("condensedLight", "fonts/robotocondensed-light.ttf"),
    CONDENSED_BOLD("condensedBold", "fonts/robotocondensed-bold.ttf");

    private String mName;
    private String mFontName;

    RobotoTextStyle(String name, String fontName) {
        mName = name;
        mFontName = fontName;
    }

    @Override
    public String getFontName() {
        return mFontName;
    }

    @Override
    public String getName() {
        return mName;
    }
}