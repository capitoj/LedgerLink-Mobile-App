package org.applab.digitizingdata.helpers;

/**
 * Created by Moses on 6/25/13.
 */
public class MenuItem {
    private String menuName;
    private String menuCaption;

    public MenuItem(String name, String caption) {
        menuName = name;
        menuCaption = caption;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public String getMenuCaption() {
        return this.menuCaption;
    }

    public void setMenuName(String value) {
        this.menuName = value;
    }

    public void setMenuCaption(String value) {
        this.menuCaption = value;
    }
}
