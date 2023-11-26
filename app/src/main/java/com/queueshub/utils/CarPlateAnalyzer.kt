package com.queueshub.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText


class CarPlateAnalyzer(val mListener: OnPlateAnalyzerFinished) :
    ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        val rotationResult: Int = when (imageProxy.imageInfo.rotationDegrees) {
            0 -> {
                FirebaseVisionImageMetadata.ROTATION_0
            }
            90 -> {
                FirebaseVisionImageMetadata.ROTATION_90
            }
            180 -> {
                FirebaseVisionImageMetadata.ROTATION_180
            }
            270 -> {
                FirebaseVisionImageMetadata.ROTATION_270
            }
            else -> {
                FirebaseVisionImageMetadata.ROTATION_0
            }
        }
        if (mediaImage != null) {
            val image =
                FirebaseVisionImage.fromMediaImage(mediaImage, rotationResult)
            val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(listOf("ar"))
                .build()
            val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)
            val result = detector.processImage(image)
                .addOnFailureListener {
                    imageProxy.close()
                    it.printStackTrace()
                    // Task failed with an exception
                    // ...
                }.addOnSuccessListener { visionText ->
                    Logger.d("completed ${visionText.text}")
                    mListener.onAnalyzerDone(visionText.textBlocks)
                }.addOnCanceledListener {
                    imageProxy.close()
                    Logger.d("cancelled")

                }
            // Pass image to an ML Kit Vision API
            // ...
//            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//            val result = recognizer.process(image)
//                .addOnSuccessListener { visionText ->
//                    // Task completed successfully
//                    // ...
//
//                    val recognisedText = StringBuilder("");
//
//                    Timber.tag("blockText").d("completed $recognisedText")
//                    for (block in visionText.textBlocks) {
//                        recognisedText.append(block.getText() + "\n");
//                        val blockText = block.text
//                        val blockFrame = block.boundingBox
//
//                        for (line in block.lines) {
//                            val lineText = line.text
//                            //once you get the text elements, you can easily extract the text, email address, name mentioned
//                            //on the visiting card or any other image containing text.
//                            //Apply your logic accordingly.
//                            //One sample is shown below for extracting the number of the form:- 011-22246388
//                            for (element in line.elements) {
//                                var elementText = element.text
//
//                                if (elementText.contains('-')) {
//                                    val split = elementText.split('-')
//                                    var part1 = ""
//                                    var part2 = ""
//                                    split.let {
//                                        part1 = it[0]
//                                        part2 = it[1]
//                                    }
//                                    elementText = part1 + part2
//                                }
//
//                                if (!elementText.isEmpty()) {
//                                    var numeric = true
//                                    var num: Double = parseDouble("0")
//                                    try {
//                                        num = parseDouble(elementText)
//                                    } catch (e: NumberFormatException) {
//                                        numeric = false
//                                    }
//                                    if (numeric)
//                                        print("Phone number detected is $num")
//                                    else
//                                        print(" Phone number is not detected on the card.")
//                                }
//                            }
//                        }
//                    }
//                    mListener.onAnalyzerDone(visionText.textBlocks)
//                }
//                .addOnFailureListener { e ->
//                    e.printStackTrace()
//                    // Task failed with an exception
//                    // ...
//                }
//                .addOnCompleteListener {
//
//                    imageProxy.close()
//                }
//        }
        }
    }

}
interface OnPlateAnalyzerFinished {
    fun onAnalyzerDone(x: List<FirebaseVisionText.TextBlock>)
}
