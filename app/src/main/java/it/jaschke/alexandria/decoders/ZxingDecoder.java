package it.jaschke.alexandria.decoders;

import android.graphics.Rect;
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
    // Command to execute in chain
    InterfaceCommand mCommand;
    // A POJO for the necessary preview data
    private CameraPreviewData mCameraPreviewData;
    // YUV data source
    private PlanarYUVLuminanceSource source;
    // Bitmap type for ZXing
    private BinaryBitmap binaryBitmap;
    // The MultiFormatReader used to decode
    private MultiFormatReader multiFormatReader;

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

        Rect cropRect = mCameraPreviewData.getRotatedBoundingRect(mCameraPreviewData.getBoundingRectF());

        byte[] mBytes = mCameraPreviewData.getBytes();
        int previewWidth = mCameraPreviewData.getSize().width;
        int previewHeight = mCameraPreviewData.getSize().height;

        Result result = null;
        source = new PlanarYUVLuminanceSource(mBytes, previewWidth, previewHeight,
                cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), false);

        if (source != null) {

            binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = multiFormatReader.decodeWithState(binaryBitmap);
                Log.d(LOG_TAG, "READER TRIED");
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }

            // Queue the scan preview
            mCommand.execute();
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

    public interface InterfaceCommand {

        void execute();
    }
}
