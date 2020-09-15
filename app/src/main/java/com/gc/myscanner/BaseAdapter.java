package com.gc.myscanner;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;


/**
 * @author gaurav
 */
public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    Context mContext;
    protected ArrayList<T> mList;
    protected ArrayList<T> newDataList;
    protected OnSelectStateListener<T> mListener;

    public BaseAdapter(Context ctx, ArrayList<T> list) {
        mContext = ctx;
        mList = list;
    }

    public void add(List<T> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void add(T file) {
        mList.add(file);
        notifyDataSetChanged();
    }

    public void add(int index, T file) {
        mList.add(index, file);
        notifyDataSetChanged();
    }

    public void refresh(List<T> list) {
        newDataList = new ArrayList<>();
        newDataList.addAll(list);
        mList.clear();
        mList.addAll(newDataList);
        notifyDataSetChanged();
    }

    public void refresh(T file) {
        mList.clear();
        mList.add(file);
        notifyDataSetChanged();
    }

    public List<T> getDataSet() {
        return mList;
    }

    public void setOnSelectStateListener(OnSelectStateListener<T> listener) {
        mListener = listener;
    }


}
