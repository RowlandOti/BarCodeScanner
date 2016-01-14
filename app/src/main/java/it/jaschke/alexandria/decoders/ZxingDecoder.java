package it.jaschke.alexandria.decoders;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import it.jaschke.alexandria.camera.CameraPreviewData;
import it.jaschke.alexandria.camera.ScanFormatManager;

/**
 * Created by Oti Rowland on 1/13/2016.
 */
public class ZXingDecoder implements InterfaceDecoder {

    // Class logging Identifier
    private final String LOG_TAG = ZXingDecoder.class.getSimpleName();

    // A POJO for the necessary preview data
    private CameraPreviewData mCameraPreviewData;
    // The MultiFormatReader used to decode
    private MultiFormatReader multiFormatReader;
    // Command to execute in chain
    InterfaceCommand mCommand;

    public interface InterfaceCommand {

        void execute(PlanarYUVLuminanceSource source);
    }

    public ZXingDecoder(CameraPreviewData mCameraPreviewData, InterfaceCommand command) {
        this.mCameraPreviewData = mCameraPreviewData;
        this.multiFormatReader = new MultiFormatReader();
        this.mCommand = command;
        // Set the hints to use
        multiFormatReader.setHints(createDecodeHints());
    }

    @Override
    public String decode() {

        String result = decodeWithZxing(mCameraPreviewData);

        return result;
    }

    public String decodeWithZxing(CameraPreviewData mCameraPreviewData) {

        RectF cropRectF = mCameraPreviewData.getCropRectF();

        byte[] mBytes = mCameraPreviewData.getBytes();
        int previewWidth = mCameraPreviewData.getSize().width;
        int previewHeight = mCameraPreviewData.getSize().height;

        Rect cropRect = new Rect();
        cropRectF.roundOut(cropRect);

        Result result = null;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(mBytes, previewWidth, previewHeight,
                cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), false);

        if (source != null) {
            // Queue the scan preview
            mCommand.execute(source);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = multiFormatReader.decodeWithState(binaryBitmap);
                Log.d(LOG_TAG, "READER TRIED");
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }
        return result != null ? result.getText() : null;
    }

    private Map<DecodeHintType, Object> createDecodeHints() {
        // Map object for hints
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<>();
        // Use both BarCodes and QRCodes
        decodeFormats.addAll(ScanFormatManager.getBarCodeFormats());
        decodeFormats.addAll(ScanFormatManager.getQrCodeFormats());
        // Map above to all possible formarts allowed
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        return hints;
    }
}
