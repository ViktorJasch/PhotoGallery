package com.example.photogallery.network;

import android.net.Uri;
import android.util.Log;

import com.example.photogallery.model.GalleryItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;


public class FlickrFetch {
    private static final String TAG = "FlickrFetch";
    private static final String API_KEY = "7020c53414408b063f438efeaa15a64c";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    /**получает низкоуровневые данные по url и возвращается их в виде массивы байт*/
    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        //создаем соединение по переданному URL адрессу (на данном этапе НЕ происходит непосредственного подключения)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //А вот тут уже непосредственно осуществляется связь с конечной точкой
            //Получем с него поток данных
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            //пока в потоке есть данные - зачитываем их в buffer. Как только данные закончатся, вернется -1
            //по мере "зачитывания" данные записываются из буфера в выходной поток
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            //закрываем выходной поток
            out.close();
            //возвращаем массив байт, переписанных с потока нашего connection в выходной поток
            return out.toByteArray();
        } finally {
            //всё что хотели - сделали, разрываем соединение
            connection.disconnect();
        }
    }

    /**Данный метод переписывает байт-массив, возвращенный getUrlBytes в строку*/
    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    /**Метод возвращает список GalleryItem объектов*/
    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> galleryItems = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject jsonPhotosObject = jsonObject.getJSONObject("photos");
            Log.i(TAG, "parseItems: got a jsonPhotosObject: " + jsonPhotosObject);
            JSONArray jsonPhotosArray = jsonPhotosObject.getJSONArray("photo");
            Log.i(TAG, "parseItems: got a jsonPhotosArray: " + jsonPhotosArray);

            Gson gson = new Gson();
            Type galleryItemsType = new TypeToken<ArrayList<GalleryItem>>(){}.getType();
            galleryItems = gson.fromJson(jsonPhotosArray.toString(), galleryItemsType);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return galleryItems;
    }

    private String buildUrl(String method, String query){
        Log.i(TAG, "buildUrl: URI before build: " + ENDPOINT);
        Uri.Builder builder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);

        if(method.equals(SEARCH_METHOD)){
            builder.appendQueryParameter("text", query);
        }

        return builder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhoto(){
        String url = buildUrl(FETCH_RECENT_METHOD, null);
        Log.i(TAG, "buildUrl: fetch URI after build: " + url);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhoto(String query){
        String url = buildUrl(SEARCH_METHOD, query);
        Log.i(TAG, "buildUrl: search URI after build: " + url);
        return downloadGalleryItems(url);
    }
}
