package spk.tld.opencv7segment.utils

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object Utils {
    fun convertToGrayscale(image: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY)
        return gray
    }

    fun applyThreshold(image: Mat, threshold: Double): Mat {
        val binary = Mat()
        Imgproc.threshold(image, binary, threshold, 255.0, Imgproc.THRESH_BINARY)
        return binary
    }
}