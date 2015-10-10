package com.aluxian.codementor;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DataViewHolder<T> extends RecyclerView.ViewHolder {

    private T data;

    public DataViewHolder(View itemView) {
        super(itemView);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
