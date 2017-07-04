package com.example.photogallery;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhotoPageFragment extends VisibleFragment implements onBackPressedListener {
    private static final String ARG_URI = "photo_page_url";
    private static final String TAG = "PhotoPageFragment";

    private Uri mUri;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoPageFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_page, container, false);

        mWebView = (WebView) view.findViewById(R.id.fragment_photo_page_web_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_photo_progress_bar);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        mWebView.loadUrl(mUri.toString());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }


        });
        mProgressBar.setMax(100);

        return view;

    }

    @Override
    public boolean onBackPressed() {
        Log.i(TAG, "onBackPressed: Начинаем обробатывать нажатие на back");
        //Защита. Если родительская активность имеет более одного фрагмента, то при нажатии back у другого
        //фрагмента данный метод все равно будет вызван. Так как mWebView будет равна null, то словим
        //NullPointerException. Поэтому необходимо проверить, является ли именно этот фрагмент в фокусе
        if(!this.isVisible())
            return false;

        if(mWebView.canGoBack()){
            mWebView.goBack();
            Log.i(TAG, "onBackPressed: webView имеет backStack, возвращаем предыдущую страницу и возвращаем true");
            return true;
        }
        return false;
    }
}
