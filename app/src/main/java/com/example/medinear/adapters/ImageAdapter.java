package com.example.medinear.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    // NEW - callback so the Activity (which owns storage) handles the actual delete
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private final Context context;
    private final ArrayList<String> imagePaths;
    private final int cellSizePx;
    private final int deleteBtnSizePx; // NEW
    private OnDeleteClickListener deleteClickListener; // NEW

    public ImageAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.cellSizePx = (int) (120 * context.getResources().getDisplayMetrics().density);
        this.deleteBtnSizePx = (int) (28 * context.getResources().getDisplayMetrics().density); // NEW
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) { // NEW
        this.deleteClickListener = listener;
    }

    @Override public int getCount()                    { return imagePaths.size(); }
    @Override public Object getItem(int position)      { return imagePaths.get(position); }
    @Override public long getItemId(int position)      { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout cell; // CHANGED - wrap ImageView + delete button in a FrameLayout
        ImageView imageView;
        ImageView btnDelete;

        if (convertView == null) {
            cell = new FrameLayout(context);
            cell.setLayoutParams(new ViewGroup.LayoutParams(cellSizePx, cellSizePx));

            imageView = new ImageView(context);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(cellSizePx, cellSizePx));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setId(View.generateViewId());
            cell.addView(imageView);

            btnDelete = new ImageView(context);
            FrameLayout.LayoutParams btnParams =
                    new FrameLayout.LayoutParams(deleteBtnSizePx, deleteBtnSizePx);
            btnParams.gravity = Gravity.TOP | Gravity.END;
            int margin = (int) (4 * context.getResources().getDisplayMetrics().density);
            btnParams.setMargins(0, margin, margin, 0);
            btnDelete.setLayoutParams(btnParams);
            btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
            btnDelete.setBackgroundColor(Color.parseColor("#B3000000")); // semi-transparent so it's visible on any photo
            btnDelete.setPadding(6, 6, 6, 6);
            btnDelete.setId(View.generateViewId());
            cell.addView(btnDelete);

            cell.setTag(new int[]{imageView.getId(), btnDelete.getId()});
        } else {
            cell = (FrameLayout) convertView;
            int[] ids = (int[]) cell.getTag();
            imageView = cell.findViewById(ids[0]);
            btnDelete = cell.findViewById(ids[1]);
        }

        String path = imagePaths.get(position);

        Bitmap bitmap = decodeSampledBitmap(path, cellSizePx, cellSizePx);

        if (bitmap == null) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        } else {
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
                if (rotated != bitmap) bitmap.recycle();
                imageView.setImageBitmap(rotated);

            } catch (IOException e) {
                imageView.setImageBitmap(bitmap);
            }
        }

        // NEW - delete button click, delegates to Activity for confirmation + actual deletion
        btnDelete.setOnClickListener(v -> {
            int currentPosition = imagePaths.indexOf(path);
            if (deleteClickListener != null && currentPosition != -1) {
                deleteClickListener.onDeleteClick(currentPosition);
            }
        });

        return cell;
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