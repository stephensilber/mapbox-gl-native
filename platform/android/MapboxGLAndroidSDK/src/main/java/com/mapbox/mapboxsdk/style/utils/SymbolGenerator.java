package com.mapbox.mapboxsdk.style.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.mapbox.mapboxsdk.Mapbox;

/**
 * Utility class to generate Bitmaps for Symbol.
 * <p>
 * Bitmaps can be added to the map with {@link com.mapbox.mapboxsdk.maps.MapboxMap#addImage(String, Bitmap)}
 * </p>
 */
public final class SymbolGenerator {

  /**
   * Generate a Bitmap from an Android SDK View.
   *
   * @param view the View to be drawn to a Bitmap
   * @return the generated bitmap
   */
  public static Bitmap generate(@NonNull View view) {
    int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    view.measure(measureSpec, measureSpec);

    int measuredWidth = view.getMeasuredWidth();
    int measuredHeight = view.getMeasuredHeight();

    view.layout(0, 0, measuredWidth, measuredHeight);
    Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.TRANSPARENT);
    Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    return bitmap;
  }

  /**
   * Generate a Bitmap from a drawable resource.
   *
   * @param drawableRes the resource id of the drawable used as base
   * @return the generated bitmap
   */
  public static Bitmap generate(@IdRes int drawableRes){
    return BitmapFactory.decodeResource(Mapbox.getApplicationContext().getResources(), drawableRes);
  }
}
