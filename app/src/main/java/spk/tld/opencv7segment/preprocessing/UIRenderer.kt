package spk.tld.opencv7segment.preprocessing

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class UIRenderer {
    fun drawGuidelines(outputFrame: Mat, frameHeight: Int, frameWidth: Int) {
        // วาดเส้นกึ่งกลางแนวตั้ง
        Imgproc.line(
            outputFrame,
            Point(frameWidth / 2.0, 0.0),
            Point(frameWidth / 2.0, frameHeight.toDouble()),
            Scalar(0.0, 255.0, 255.0),
            1
        )

        // วาดเส้นแสดงระยะที่เหมาะสม
        val optimalDistance = frameHeight * 0.4
        val upperBound = optimalDistance - (frameHeight * 0.03)
        val lowerBound = optimalDistance + (frameHeight * 0.03)

        Imgproc.line(
            outputFrame,
            Point(0.0, upperBound),
            Point(frameWidth.toDouble(), upperBound),
            Scalar(0.0, 255.0, 255.0),
            1
        )

        Imgproc.line(
            outputFrame,
            Point(0.0, lowerBound),
            Point(frameWidth.toDouble(), lowerBound),
            Scalar(0.0, 255.0, 255.0),
            1
        )
    }
    fun drawResult(outputFrame: Mat, number: String, isStable: Boolean) {
        if (isStable) {
            // ตรวจจับตัวเลข
            Imgproc.putText(
                outputFrame,
                number,
                Point(10.0, 50.0),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                2.0,
                Scalar(0.0, 255.0, 0.0),
                3
            )

            // Draw success indicator
            Imgproc.circle(
                outputFrame,
                Point(outputFrame.cols() - 50.0, 50.0),
                20,
                Scalar(0.0, 255.0, 0.0),
                -1
            )
        }
    }

    fun drawInstructions(outputFrame: Mat, shouldShowInstructions: Boolean) {
        if (shouldShowInstructions) {
            Imgproc.putText(
                outputFrame,
                "Place LCD in frame",
                Point(10.0, outputFrame.rows() - 30.0),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.7,
                Scalar(0.0, 255.0, 255.0),
                2
            )
        }
    }
}