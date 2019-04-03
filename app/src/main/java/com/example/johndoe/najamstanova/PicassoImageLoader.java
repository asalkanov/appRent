package com.example.johndoe.najamstanova;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import static com.example.johndoe.najamstanova.GlideOptions.fitCenterTransform;

/**
 * Created by veinhorn on 2/4/18.
 */

public class PicassoImageLoader implements MediaLoader {
    private String url;
    private Integer thumbnailWidth;
    private Integer thumbnailHeight;

    public PicassoImageLoader(String url) {
        this.url = url;
    }

    public PicassoImageLoader(String url, Integer thumbnailWidth, Integer thumbnailHeight) {
        this.url = url;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
    }

    @Override
    public boolean isImage() {
        return true;
    }

    @Override
    public void loadMedia(Context context, final ImageView imageView, final MediaLoader.SuccessCallback callback) {
        RequestOptions options = new RequestOptions();
        /*Picasso.get()
                //.load("file://" + url)        /* Potrebno samo ako se učitavaju slike s memorije uređaka
                .load(url)
                .placeholder(R.drawable.placeholder_image)
                .fit().centerInside()
                .into(imageView, new ImageCallback(callback));
        */
        Glide.with(context).load(url).apply(fitCenterTransform()).into(imageView);
    }

    @Override
    public void loadThumbnail(Context context, final ImageView thumbnailView, final MediaLoader.SuccessCallback callback) {
        Picasso.get()
                //.load("file://"+url)
                .load(url)
                .resize(thumbnailWidth == null ? 100 : thumbnailWidth,
                        thumbnailHeight == null ? 100 : thumbnailHeight)
                .placeholder(R.drawable.placeholder_image)
                .centerInside()
                .into(thumbnailView, new ImageCallback(callback));
    }

    private static class ImageCallback implements Callback {
        private final MediaLoader.SuccessCallback callback;

        public ImageCallback(SuccessCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess() {
            callback.onSuccess();
        }

        @Override
        public void onError(Exception e) {

        }


    }
}