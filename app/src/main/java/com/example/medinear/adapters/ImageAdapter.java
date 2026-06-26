package com.example.medinear.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<String> imagePaths;
    private final int cellSizePx;

    public ImageAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.cellSizePx = (int) (120 * context.getResources().getDisplayMetrics().density);
    }

    @Override public int getCount()                    { return imagePaths.size(); }
    @Override public Object getItem(int position)      { return imagePaths.get(position); }
    @Override public long getItemId(int position)      { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(cellSizePx, cellSizePx));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        String path = imagePaths.get(position);

        // Sample down to thumbnail size — avoids OOM
        Bitmap bitmap = decodeSampledBitmap(path, cellSizePx, cellSizePx);

        if (bitmap == null) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            return imageView;
        }

        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:  matrix.postRotate(90);  break;
                case ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
                case ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
                default: break;
            }

            Bitmap rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) bitmap.recycle(); // free original if rotation made a new copy
            imageView.setImageBitmap(rotated);

        } catch (IOException e) {
            imageView.setImageBitmap(bitmap); // show unrotated rather than nothing
        }

        return imageView;
    }

    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int sampleSize = 1;
        while ((options.outWidth / sampleSize) > reqWidth
                || (options.outHeight / sampleSize) > reqHeight) {
            sampleSize *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(path, options);
    }
}