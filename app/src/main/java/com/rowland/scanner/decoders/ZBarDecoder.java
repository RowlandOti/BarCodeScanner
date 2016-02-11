package com.rowland.scanner.decoders;

import android.graphics.Rect;

import com.rowland.scanner.camera.CameraPreviewData;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * Created by Oti Rowland on 1/13/2016.
 */
public class ZBarDecoder implements InterfaceDecoder {

    static {
        System.loadLibrary("iconv");
    }

    // Class logging Identifier
    private final String LOG_TAG = ZBarDecoder.class.getSimpleName();
    // Command to execute in chain
    InterfaceCommand mCommand;
    // A POJO for the necessary preview data
    private CameraPreviewData mCameraPreviewData;
    // ZBar scanner object
    private ImageScanner mImageScanner;

    public ZBarDecoder(CameraPreviewData mCameraPreviewData, InterfaceCommand command) {
        this.mCameraPreviewData = mCameraPreviewData;
        this.mCommand = command;
        this.mImageScanner = new ImageScanner();
        createDecodeHints();
    }

    @Override
    public String decode() {

        String result = decodeWithZbar(mCameraPreviewData);

        return result;
    }

    public String decodeWithZbar(CameraPreviewData mBitmapData) {

        Rect cropRect = mBitmapData.getRotatedBoundingRect(mBitmapData.getBoundingRectF());

        byte[] mBytes = mBitmapData.getBytes();
        int previewWidth = mBitmapData.getSize().width;
        int previewHeight = mBitmapData.getSize().height;

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

        // Queue the scan preview
        mCommand.execute();

        return resultStr;
    }

    private void createDecodeHints() {
        // Enable all modes for now
        mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 1);
    }

    public interface InterfaceCommand {

        void execute();
    }
}
