package org.applab.ledgerlink.utils;

/**
 * Created by Joseph Capito on 7/23/2015.
 */
public class Size {

    private int height;
    private int width;

    public Size(int width, int height){
        this.width = width;
        this.height = height;
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }
}
