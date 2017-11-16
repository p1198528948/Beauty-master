package com.dante.girls.picture;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.girls.R;
import com.dante.girls.lib.RatioImageView;
import com.dante.girls.model.Image;
import com.dante.girls.utils.Imager;
import com.dante.girls.utils.UiUtils;

/**
 * Adapter of picture list.
 */

class PictureAdapter extends BaseQuickAdapter<Image, BaseViewHolder> {

    private Fragment context;

    PictureAdapter(int layoutId, Fragment context) {
        super(layoutId, null);
        this.context = context;
//        setHasStableIds(true);
    }

    public static String optimizeTitle(String title) {
        return title.replace("A区：", "")
                .replace("APIC.IN", "")
                .replace("-A区", "")
                .replace("APIC-IN", "")
                .replace("下载", "")
                .replace("动漫", "")
                .replace("壁纸", "")
                .replace("图片", "")
                .trim();
    }

    @Override
    public long getItemId(int position) {
        try {
            return getItem(position).hashCode();
        } catch (ArrayIndexOutOfBoundsException e) {
            return position;
        }
    }

    @Override
    protected void convert(final BaseViewHolder holder, final Image image) {
        final ImageView imageView = holder.getView(R.id.picture);
        if (imageView instanceof RatioImageView
                && image.width != 0) {
            ((RatioImageView) imageView).setOriginalSize(image.width, image.height);
        }
        ViewCompat.setTransitionName(imageView, image.url);

        final View post = holder.getView(R.id.post);
        final TextView title = holder.getView(R.id.title);
        if (post != null) {
            title.setText(optimizeTitle(image.title));
        }
        RequestListener<String, Bitmap> listener = new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                if (e != null) {
                    UiUtils.showSnackLong(imageView.getRootView(), e.getMessage(), R.string.retry,
                            v -> Imager.load(context, image.url, imageView));
                }

                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (post != null) {
                    Palette.from(resource).generate(palette -> {
                        int color = palette.getDarkMutedColor(ContextCompat.getColor(mContext, R.color.cardview_dark_background));
                        title.setBackgroundColor(color);
                        title.setVisibility(View.VISIBLE);
                    });
                }
                return false;
            }
        };
        Imager.load(context, image.url, imageView, listener);

    }


}
