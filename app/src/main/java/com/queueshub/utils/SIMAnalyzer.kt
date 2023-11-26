package com.queueshub.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.text.isDigitsOnly
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class SIMAnalyzer(val mListener: OnSIMAnalyzerFinished) :
    ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            val result = scanner.process(image).addOnSuccessListener { barcodes ->
                var phone = ""
                var serial = ""
                for (barcode in barcodes) {
                    val displayValue = barcode.displayValue
                    if (displayValue?.isValidEgyptianPhoneNumber() == true) {
                        phone = displayValue
                    } else {
                        if ((displayValue?.length ?: 0) > 11) {
                            serial = displayValue!!
                        } else {
                            mListener.onTryAgain()
                        }
                    }
                }
                mListener.onAnalyzerDone(serial, phone)

            }.addOnFailureListener {
                it.printStackTrace()
                // Task failed with an exception
                // ...
            }.addOnCompleteListener {
                Logger.d("completed")

            }.addOnCanceledListener {
                Logger.d("cancelled")

            }
        }
    }
}

interface OnSIMAnalyzerFinished {
    fun onAnalyzerDone(serial: String?, gsm: String?)
    fun onTryAgain()
}
