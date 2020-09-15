package com.gc.myscanner;


/**
 * @author gaurav
 */
public interface OnSelectStateListener<T> {


    void OnSelectStateChanged(boolean state, T file);


}
