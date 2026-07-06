package com.example.medinear.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.exifinterface.media.ExifInterface;

import com.example.medinear.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private final Context context;
    private final ArrayList<String> imagePaths;
    private final int cellSizePx;
    private final int deleteBtnSizePx;
    private final int cellPaddingPx; // NEW - gives room for the delete circle to sit half-outside the photo
    private OnDeleteClickListener deleteClickListener;

    public ImageAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
        float density = context.getResources().getDisplayMetrics().density;
        this.cellSizePx = (int) (120 * density);
        this.deleteBtnSizePx = (int) (30 * density); // CHANGED - bigger tap target (was 28dp, functionally similar but clearer visual)
        this.cellPaddingPx = (int) (6 * density);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout cell;
        ImageView imageView;
        ImageView btnDelete;

        if (convertView == null) {
            int outerSize = cellSizePx + cellPaddingPx;

            cell = new FrameLayout(context);
            cell.setLayoutParams(new ViewGroup.LayoutParams(outerSize, outerSize));
            cell.setPadding(cellPaddingPx, cellPaddingPx, 0, 0); // NEW - room for delete circle to overlap corner

            imageView = new ImageView(context);
            FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(cellSizePx, cellSizePx);
            imageView.setLayoutParams(imgParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.bg_gallery_cell); // NEW - rounded frame
            imageView.setClipToOutline(true); // NEW - clips bitmap to rounded corners
            imageView.setId(View.generateViewId());
            cell.addView(imageView);

            btnDelete = new ImageView(context);
            FrameLayout.LayoutParams delParams =
                    new FrameLayout.LayoutParams(deleteBtnSizePx, deleteBtnSizePx);
            delParams.gravity = Gravity.TOP | Gravity.END;
            btnDelete.setLayoutParams(delParams);
            btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
            btnDelete.setColorFilter(Color.WHITE); // NEW - forces icon white for contrast on red circle
            btnDelete.setBackgroundResource(R.drawable.bg_delete_circle); // CHANGED - solid red circle, not translucent black square
            btnDelete.setElevation(4f); // NEW - subtle shadow so it reads as "floating" above photo
            int iconPad = (int) (5 * context.getResources().getDisplayMetrics().density);
            btnDelete.setPadding(iconPad, iconPad, iconPad, iconPad);
            btnDelete.setId(View.generateViewId());
            cell.addView(btnDelete);

            cell.setTag(new int[]{
                    imageView.getId(),
                    btnDelete.getId()
            });
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
                if (!path.startsWith("content://")) {
                    ExifInterface exif = new ExifInterface(path);
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    Matrix matrix = new Matrix();
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }
                    Bitmap rotated = Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.getWidth(),
                            bitmap.getHeight(),
                            matrix,
                            true);
                    if (rotated != bitmap) {
                        bitmap.recycle();
                    }
                    imageView.setImageBitmap(rotated);
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                imageView.setImageBitmap(bitmap);
            }
        }

        btnDelete.setOnClickListener(v -> {
            int currentPosition = imagePaths.indexOf(path);
            if (deleteClickListener != null && currentPosition != -1) {
                deleteClickListener.onDeleteClick(currentPosition);
            }
        });

        return cell;
    }

    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        try {
            if (path.startsWith("content://")) {
                Uri uri = Uri.parse(path);
                InputStream inputStream =
                        context.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bitmap == null) {
                    return null;
                }
                return Bitmap.createScaledBitmap(
                        bitmap,
                        reqWidth,
                        reqHeight,
                        true
                );
            } else {
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}