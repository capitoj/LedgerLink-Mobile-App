package org.applab.digitizingdata.fontutils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * A subclass of {@link android.widget.EditText} which has a custom {@link org.applab.digitizingdata.fontutils.TextStyle} applied.
 */
public class TypefaceEditText extends EditText {

    public TypefaceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypefaceManager.getInstance().applyTypeface(this, context, attrs);
    }

    /**
     * Convenience method in case I need to change the font from code as well.
     * @param textStyle
     */
    public void setTextStyle(TextStyle textStyle) {
        TypefaceManager.getInstance().applyTypeface(this, textStyle);
    }
}