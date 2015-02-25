package org.applab.ledgerlink.fontutils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * A subclass of {@link android.widget.Button} which has a custom {@link TextStyle} applied.
 */
public class TypefaceButton extends Button {

    public TypefaceButton(Context context, AttributeSet attrs) {
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