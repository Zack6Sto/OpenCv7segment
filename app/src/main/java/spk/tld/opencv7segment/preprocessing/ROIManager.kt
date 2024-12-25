package spk.tld.opencv7segment.preprocessing

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class ROIManager {
    private var previewROI: Rect? = null

    fun setupROI(frameHeight: Int, frameWidth: Int): Rect {
        if (previewROI == null) {
            val roiHeight = (frameHeight / 4.5).toInt()
            val roiWidth = (roiHeight * 2).coerceAtMost(frameWidth - 20)
            val x = ((frameWidth - roiWidth) / 2).toInt()
            val y = (frameHeight * 0.4).toInt()
            previewROI = Rect(x, y, roiWidth, roiHeight)
        }
        return previewROI!!
    }

    fun drawGuidelines(outputFrame: Mat, frameHeight: Int, frameWidth: Int) {
        val optimalDistance = frameHeight * 0.4
        val tolerance = frameHeight * 0.05

        // วาดกรอบ ROI
        previewROI?.let { roi ->
            Imgproc.rectangle(
                outputFrame,
                roi,
                Scalar(255.0, 0.0, 0.0),
                2
            )
        }
    }
}