package com.example.photogallery.mvp.photos;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by viktor on 07.07.17.
 */

public class CustomScrollListener extends RecyclerView.OnScrollListener {
    private static final String TAG = "CustomScrollListener";
    private GridLayoutManager layoutManager;
    private LoadingListener listener;
    boolean isLoading;

    public CustomScrollListener(GridLayoutManager layoutManager, LoadingListener listener){
        this.layoutManager = layoutManager;
        this.listener = listener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int visibleItemCount = layoutManager.getChildCount();//смотрим сколько элементов на экране
        int totalItemCount = layoutManager.getItemCount();//сколько всего элементов
        int firstVisibleItems = layoutManager.findFirstVisibleItemPosition();//какая позиция первого элемента

        //проверяем, грузим мы что-то или нет, эта переменная должна устанавливаться
        //вне класса  OnScrollListener
        if (!isLoading) {
            if ( (visibleItemCount+firstVisibleItems) >= totalItemCount) {
                Log.d(TAG, "onScrolled: totalItems = " + totalItemCount + "\n" +
                        "visibleItemCount+firstVisibleItems = " + (visibleItemCount+firstVisibleItems));
                isLoading = true;//ставим флаг что мы попросили еще элемены
                if(listener != null){
                    listener.needMoreElements();//тут я использовал калбэк который просто говорит наружу что нужно еще элементов и с какой позиции начинать загрузку
                }
            }
        }
    }

    public interface LoadingListener {
        void needMoreElements();
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isLoading() {
        return isLoading;
    }
}
