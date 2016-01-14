package it.jaschke.alexandria.decoders;

import android.graphics.Rect;
import android.graphics.RectF;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import it.jaschke.alexandria.camera.CameraPreviewData;

/**
 * Created by Oti Rowland on 1/13/2016.
 */
public class ZBarDecoder implements InterfaceDecoder {

    // Class logging Identifier
    private final String LOG_TAG = ZBarDecoder.class.getSimpleName();

    // A POJO for the necessary preview data
    private CameraPreviewData mCameraPreviewData;
    // ZBar scanner object
    private ImageScanner mImageScanner;


    static {
        System.loadLibrary("iconv");
    }

    public ZBarDecoder(CameraPreviewData mCameraPreviewData) {
        this.mCameraPreviewData = mCameraPreviewData;
        this.mImageScanner = new ImageScanner();
        createDecodeHints();
    }

    @Override
    public String decode() {

        String result = decodeWithZbar(mCameraPreviewData);

        return result;
    }

    public String decodeWithZbar(CameraPreviewData mBitmapData) {

        RectF cropRectF = mBitmapData.getCropRectF();

        byte[] mBytes = mBitmapData.getBytes();
        int previewWidth = mBitmapData.getSize().width;
        int previewHeight = mBitmapData.getSize().height;

        Rect cropRect = new Rect();
        cropRectF.roundOut(cropRect);


        createDecodeHints();

        Image barcode = new Image(previewWidth, previewHeight, "Y800");
        barcode.setData(mBytes);

        if (null != cropRect) {
            barcode.setCrop(cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
        }

        int result = mImageScanner.scanImage(barcode);
        String resultStr = null;

        if (result != 0) {
            SymbolSet syms = mImageScanner.getResults();
            for (Symbol sym : syms) {
                resultStr = sym.getData();
            }
        }

        return resultStr;
    }

    private void createDecodeHints() {
        // Enable all modes for now
        mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 1);
    }
}
