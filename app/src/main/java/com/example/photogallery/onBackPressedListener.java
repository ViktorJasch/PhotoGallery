package com.example.photogallery;

/**
 * C данным интерфейсом будем реализовывать кликабельность кнопки back во фрагменте PhotoPageFragment.
 * Другими словами, необходимо сделать так, чтобы учитывался backStack элемента webView.
 */

public interface onBackPressedListener {
    /**
     * Данный метод необходим для того, чтобы обеспечить кликабельность кнопки back во фрагменте
     * @return Возвращает true, если у webView есть backStack и false в ином случае
     */
    public boolean onBackPressed();
}
