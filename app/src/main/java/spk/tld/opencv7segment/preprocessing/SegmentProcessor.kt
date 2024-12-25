package spk.tld.opencv7segment.preprocessing

import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class SegmentProcessor {
    private val roiManager = ROIManager()           // จัดการพื้นที่ ROI
    private val imageEnhancer = ImageEnhancer()     // ปรับปรุงคุณภาพภาพ
    private val contourFinder = DigitContourFinder() // ค้นหาเส้นขอบตัวเลข
    private val digitRecognizer = DigitRecognizer()  // จดจำตัวเลข
    private val numberStabilizer = NumberStabilizer() // ทำให้ผลลัพธ์เสถียร
    private val uiRenderer = UIRenderer()            // แสดงผลบน UI

    // ฟังก์ชันหลักในการประมวลผลแต่ละเฟรม
    fun processFrame(grayFrame: Mat, outputFrame: Mat) {
        try {
            // หาความสูงและความกว้างของเฟรม
            val frameHeight = outputFrame.rows()
            val frameWidth = outputFrame.cols()

            // ตั้งค่าและวาด ROI
            val roi = roiManager.setupROI(frameHeight, frameWidth)
            roiManager.drawGuidelines(outputFrame, frameHeight, frameWidth)
            // uiRenderer.drawGuidelines(outputFrame, frameHeight, frameWidth)

            // ประมวลผลภาพ
            val roiGray = Mat(grayFrame, roi)  // ตัดเฉพาะส่วน ROI
            val binary = imageEnhancer.enhance(roiGray)  // ปรับปรุงภาพ
            val digitContours = contourFinder.findDigitContours(binary)  // หาเส้นขอบตัวเลข

            // จดจำตัวเลขแต่ละตัว
            var currentNumber = ""
            digitContours.forEach { contour ->
                val rect = Imgproc.boundingRect(contour)  // หากรอบรอบตัวเลข
                val digit = binary.submat(rect)  // ตัดเฉพาะส่วนตัวเลข

                // คำนวณตำแหน่งจริงบนภาพเต็ม
                val absoluteRect = Rect(
                    rect.x + roi.x,
                    rect.y + roi.y,
                    rect.width,
                    rect.height
                )

                // จดจำตัวเลข
                val number = digitRecognizer.recognizeDigit(digit)
                if (number != -1) {
                    currentNumber += number
                    // วาดกรอบสีเขียวรอบตัวเลขที่พบ
                    Imgproc.rectangle(
                        outputFrame,
                        absoluteRect,
                        Scalar(0.0, 255.0, 0.0),
                        2
                    )
                }
            }

            // ตรวจสอบความเสถียรและแสดงผล
            if (currentNumber.isNotEmpty()) {
                val isStable = numberStabilizer.addNumber(currentNumber)
                uiRenderer.drawResult(outputFrame, currentNumber, isStable)
            }

            // แสดงคำแนะนำถ้าไม่พบตัวเลข
            uiRenderer.drawInstructions(outputFrame, digitContours.isEmpty())

            // คืนหน่วยความจำ
            roiGray.release()
            binary.release()
            digitContours.forEach { it.release() }

        } catch (e: Exception) {
            Log.e(TAG, "Error in processFrame: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SegmentProcessor"
    }
}
