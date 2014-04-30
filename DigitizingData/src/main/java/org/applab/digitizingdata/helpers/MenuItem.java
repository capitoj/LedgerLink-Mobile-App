package org.applab.digitizingdata.helpers;

/**
 * Created by Moses on 6/25/13.
 */
public class MenuItem {
    private String menuName;
    private String menuCaption;
    private int menuImage;

    public MenuItem(String name, String caption) {
        menuName = name;
        menuCaption = caption;
    }

    public MenuItem(String name, String caption, int image) {
        menuName = name;
        menuCaption = caption;
        menuImage = image;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public String getMenuCaption() {
        return this.menuCaption;
    }

    public int getMenuImage() {
        return this.menuImage;
    }

    public void setMenuName(String value) {
        this.menuName = value;
    }

    public void setMenuCaption(String value) { 
	this.menuCaption = value;
	}

    public void setMenuImage(int value) { this.menuImage = value; }
}
