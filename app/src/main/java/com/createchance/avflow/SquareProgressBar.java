package com.createchance.avflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * The basic {@link SquareProgressBar}. This class includes all the methods you
 * need to modify your {@link SquareProgressBar}.
 *
 * @author
 * @since 1.0.0
 */
public class SquareProgressBar extends CardView {

    private ImageView imageView;
    private View mMask;
    private final SquareProgressView bar;
    private boolean opacity = false;
    private boolean isFadingOnProgress = false;

    /**
     * New SquareProgressBar.
     *
     * @param context  the {@link Context}
     * @param attrs    an {@link AttributeSet}
     * @param defStyle a defined style.
     * @since 1.0.0
     */
    public SquareProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_square_progressbar, this, true);
        bar = (SquareProgressView) findViewById(R.id.squareProgressBar);
        imageView = (ImageView) findViewById(R.id.img_default);
        mMask = findViewById(R.id.img_mask);
        bar.bringToFront();
    }

    /**
     * New SquareProgressBar.
     *
     * @param context the {@link Context}
     * @param attrs   an {@link AttributeSet}
     * @since 1.0.0
     */
    public SquareProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_square_progressbar, this, true);
        bar = (SquareProgressView) findViewById(R.id.squareProgressBar);
        imageView = (ImageView) findViewById(R.id.img_default);
        mMask = findViewById(R.id.img_mask);
        bar.bringToFront();
    }

    /**
     * New SquareProgressBar.
     *
     * @param context
     * @since 1.0.0
     */
    public SquareProgressBar(Context context) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_square_progressbar, this, true);
        bar = (SquareProgressView) findViewById(R.id.squareProgressBar);
        imageView = (ImageView) findViewById(R.id.img_default);
        mMask = findViewById(R.id.img_mask);
        bar.bringToFront();
    }

    /**
     * Sets the image of the {@link SquareProgressBar}. Must be a valid
     * ressourceId.
     *
     * @param image the image as a ressourceId
     * @since 1.0
     */
    public void setImage(int image) {
        imageView.setImageResource(image);
    }

    public void setImage(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    public void setImageSize(int width,int height) {
        ViewGroup.LayoutParams imgParams = imageView.getLayoutParams();
        imgParams.width = width;
        imgParams.height = height;
        imageView.setLayoutParams(imgParams);
    }

    /**
     * Sets the image scale type according to {@link ScaleType}.
     *
     * @param scale the image ScaleType
     * @author thiagokimo
     * @since 1.3.0
     */
    public void setImageScaleType(ScaleType scale) {
        imageView.setScaleType(scale);
    }

    /**
     * Sets the progress of the {@link SquareProgressBar}. If opacity is
     * selected then here it sets it.
     *
     * @param progress the progress
     * @since 1.0.0
     */
    public void setProgress(double progress) {
        bar.setProgress(progress);
        if (opacity) {
            if (isFadingOnProgress) {
                setOpacity(100 - (int) progress);
            } else {
                setOpacity((int) progress);
            }
        } else {
            setOpacity(100);
        }

        // set mask alpha
        mMask.setAlpha((float) (0.5 * (100 - progress) / 100));
    }

    /**
     * Sets the colour of the {@link SquareProgressBar} to a predefined android
     * holo color. <br/>
     * <b>Examples:</b>
     * <ul>
     * <li>holo_blue_bright</li>
     * <li>holo_blue_dark</li>
     * <li>holo_blue_light</li>
     * <li>holo_green_dark</li>
     * <li>holo_green_light</li>
     * <li>holo_orange_dark</li>
     * <li>holo_orange_light</li>
     * <li>holo_purple</li>
     * <li>holo_red_dark</li>
     * <li>holo_red_light</li>
     * </ul>
     *
     * @param androidHoloColor
     * @since 1.0.0
     */
    public void setHoloColor(int androidHoloColor) {
        bar.setColor(getContext().getResources().getColor(androidHoloColor));
    }

    /**
     * Sets the colour of the {@link SquareProgressBar}. YOu can give it a
     * hex-color string like <i>#C9C9C9</i>.
     *
     * @param color the colour of the {@link SquareProgressBar}
     * @since 1.1.0
     */
    public void setColor(int color) {
        bar.setColor(color);
    }

    /**
     * This sets the colour of the {@link SquareProgressBar} with a RGB colour.
     *
     * @param r red
     * @param g green
     * @param b blue
     * @since 1.1.0
     */
    public void setColorRGB(int r, int g, int b) {
        bar.setColor(Color.rgb(r, g, b));
    }

    /**
     * This sets the colour of the {@link SquareProgressBar} with a RGB colour.
     * Works when used with
     * <code>android.graphics.Color.rgb(int, int, int)</code>
     * <p>
     * r
     * red
     * g
     * green
     * b
     * blue
     *
     * @since 1.4.0
     */
    public void setColorRGB(int rgb) {
        bar.setColor(rgb);
    }

    /**
     * This sets the width of the {@link SquareProgressBar}.
     *
     * @param width in Dp
     * @since 1.1.0
     */
    public void setWidth(int width) {
        bar.setWidthInDp(width);
    }

    /**
     * Activates the drawing of rounded corners with a given radius.
     *
     * @since 1.6.2
     */
    public void setRoundedCorners(boolean useRoundedCorners, float radius){
        bar.setRoundedCorners(useRoundedCorners, radius);
    }

    /**
     * Returns a boolean if rounded corners is active or not.
     *
     * @return true if rounded corners is active.
     * @since 1.6.2
     */
    public boolean isRoundedCorners(){
        return bar.isRoundedCorners();
    }

    /**
     * This sets the alpha of the image in the view. Actually I need to use the
     * deprecated method here as the new one is only available for the API-level
     * 16. And the min API level of this library is 14.
     * <p>
     * Use this only as private method.
     *
     * @param progress the progress
     */
    private void setOpacity(int progress) {
        imageView.setAlpha((int) (2.55 * progress));
    }

    /**
     * Returns the {@link ImageView} that the progress gets drawn around.
     *
     * @return the main ImageView
     * @since 1.6.0
     */
    public ImageView getImageView() {
        return imageView;
    }

}
