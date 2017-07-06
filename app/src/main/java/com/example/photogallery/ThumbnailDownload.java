package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.photogallery.network.FlickrFetch;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Виктор on 13.12.2016.
 */

public class ThumbnailDownload<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownload";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private Handler mResponseHolder;
    //потокобезопасная хеш-таблица, которая содержит в качестве ключа - ViewHolder, а значения - url_s адрес картинки
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbDownloaded(T obj, Bitmap thumbnail);
    }

    public void setThumbDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownload(Handler responseHandler){
        super(TAG);
        //полуачем объект handler UI потока, который может таки изменять интерфейс
        mResponseHolder = responseHandler;
    }

    public void queueThumbnail(T obj, String url){
        Log.i(TAG, "queueThumbnail: Got a URL: " + url);

        //если в коллекции уже содержится такой Holder и при этом переданный URL = null
        //(это возможно, если фото не имеет формата url_s), этот Holder удаляется
        if(url == null)
            mRequestMap.remove(obj);
        //Иначе добавляется новый элемент (или этот элемент заменяет старый) с ключом переданного Holder
        //Handler составляет сообщение с меткой MESSAGE_DOWNLOAD и obj = PhotoHandler
        //метод sendToTarget закрепляет за собой mRequestHandler в качестве обработчика
        else{
            //закидываем а таблицу новый элемент
            mRequestMap.put(obj, url);
            //составляем сообщение. Так как оно создается из Handler, то он и является обработчиком
            //sendToTarget - метод класса Message, отправляет сообщение объекту, обозначенному в поле target сообщения
            //В данном случае это Handler
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, obj)
                .sendToTarget();
        }
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MESSAGE_DOWNLOAD:
                        T obj = (T) msg.obj;
                        Log.i(TAG, "handleMessage: Got a request for URL: " + mRequestMap.get(obj));
                        handleRequest(obj);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void clearQueue(){
        mRequestMap.clear();
    }

    /**
     * Данный метод по переданному obj получает url адресс миниатюры, загружает данные и строит по этим данным картинку (Factory)
     * удаляет из хеш-таблицы используемый элемент
     * Запускает работу mResponseHandler, который связан с главным потоком, следовательно, может его обновлять
     * @param obj данный параметр представляет собой ViewHolder, который передается как идентификатор для загрузки
     */
    private void handleRequest(final T obj){
        try{
            final String url = mRequestMap.get(obj);
            if(url == null)
                return;

            byte[] byteMaps = new FlickrFetch().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(byteMaps, 0, byteMaps.length);
            Log.i(TAG, "handleRequest: bitmap created");
            /*
              Данная запись аналогична слудующей:
                     Runnable myRunnable = new Runnable() {
                     public void run() {
                        что-то вытворяем
                }
            };
            Message m = mHandler.obtainMessage();
            m.callback = myRunnable;
             callback - тот код, который будет выполнен, при получении хэндлером данного сообщения*/
            mResponseHolder.post(new Runnable() {
                @Override
                public void run() {
                    //вездесущая на всякислучайная проверка. Так как холдеры постоянно перерабатываются,
                    //может оказаться, что данный холдер уже запросил картинку с другого url адресса
                    // (то есть из тела onBindViewHolder был вызван метод queueThumbnail) и следует уже ее обробатывать
                    if(mRequestMap.get(obj) != url)
                        return;

                    //удаляем из хеш-таблицы используемый элемент
                    mRequestMap.remove(obj);
                    //вызывается метод слушателя с передачей холдера и картинки для загрузки
                    mThumbnailDownloadListener.onThumbDownloaded(obj, bitmap);
                }
            });
        } catch (IOException ioe){
            Log.e(TAG, "handleRequest: Error download file", ioe);
        }
    }
}
