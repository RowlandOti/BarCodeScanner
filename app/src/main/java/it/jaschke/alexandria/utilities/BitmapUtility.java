package it.jaschke.alexandria.utilities;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Oti Rowland on 1/12/2016.
 */
public class BitmapUtility {

    public static Bitmap rotateBitmap(Bitmap src, float degree) {
        // create new matrix object
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        // return new bitmap rotated using matrix
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }
}
