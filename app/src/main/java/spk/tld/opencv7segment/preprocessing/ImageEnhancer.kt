package spk.tld.opencv7segment.preprocessing

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class ImageEnhancer {
    fun enhance(roiGray: Mat): Mat {
        val enhanced = Mat()
        val contrastEnhancer = Imgproc.createCLAHE().apply {
            clipLimit = 3.0
            tilesGridSize = Size(8.0, 8.0)
        }
        contrastEnhancer.apply(roiGray, enhanced)

        val binary = Mat()
        // แปลงเป็นภาพขาว-ดำแบบปรับตัว (Adaptive Thresholding)
        Imgproc.adaptiveThreshold(
            enhanced,           // ภาพ input
            binary,            // ภาพ output
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,          // แปลงเป็นภาพ inverse (พื้นหลังดำ, ตัวอักษรขาว)
            15,                // ขนาดบล็อกที่ใช้คำนวณ threshold
            5.0                // ค่าคงที่ที่หักออกจากค่าเฉลี่ย
        )

        // ลดสัญญาณรบกวน (Noise Reduction)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        // ใช้ morphology operations เพื่อลดสัญญาณรบกวน
        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_CLOSE, kernel)  // ปิดช่องว่างเล็กๆ
        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, kernel)   // ลบจุดรบกวนขนาดเล็ก
        Imgproc.medianBlur(binary, binary, 3)  // ใช้ median filter เพื่อลดสัญญาณรบกวน

        enhanced.release()
        return binary
    }
}