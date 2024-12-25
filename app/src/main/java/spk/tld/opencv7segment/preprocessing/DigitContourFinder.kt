package spk.tld.opencv7segment.preprocessing

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class DigitContourFinder {
    // ฟังก์ชันหลักสำหรับค้นหาเส้นขอบของตัวเลข
    // รับภาพขาวดำ(binary) เป็น input และคืนค่าเป็น List ของเส้นขอบที่ผ่านการกรองแล้ว
    fun findDigitContours(binary: Mat): List<MatOfPoint> {
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()

        Imgproc.findContours(
            binary.clone(),
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        val validDigits = filterAndSortContours(contours, binary)
        hierarchy.release()

        return validDigits
    }
    // ฟังก์ชันสำหรับกรองขนาด, สัดส่วน และความสูง และเรียงลำดับเส้นขอบ
    private fun filterAndSortContours(contours: List<MatOfPoint>, binary: Mat): List<MatOfPoint> {
        return contours
            .filter { contour ->
                val rect = Imgproc.boundingRect(contour)
                val aspectRatio = rect.width.toDouble() / rect.height.toDouble()
                val area = Imgproc.contourArea(contour)

                area > 300 && // ลดขนาดขั้นต่ำ
                        area < 8000 && // เพิ่มขนาดสูงสุด
                        aspectRatio > 0.35 && aspectRatio < 0.9 && // ปรับสัดส่วนให้ยืดหยุ่นขึ้น
                        rect.height > binary.rows() * 0.15 // เพิ่มความสูงขั้นต่ำ

            }
            .sortedBy { Imgproc.boundingRect(it).x }
    }

}