package spk.tld.opencv7segment.preprocessing

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class DigitRecognizer {
    // คลาสสำหรับเก็บข้อมูลตำแหน่งและขนาดของแต่ละส่วนของตัวเลข 7-Segment
    data class SegmentDefinition(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val weight: Double
    )

    // กำหนดตำแหน่งของแต่ละ segment ใน 7-Segment Display
    private val segments = arrayOf(
        SegmentDefinition(8, 0, 14, 3, 1.2),   // ด้านบน
        SegmentDefinition(2, 3, 3, 18, 1.0),   // ซ้ายบน
        SegmentDefinition(25, 3, 3, 18, 1.0),  // ขวาบน
        SegmentDefinition(8, 21, 14, 3, 1.2),  // กลาง
        SegmentDefinition(2, 24, 3, 18, 1.0),  // ซ้ายล่าง
        SegmentDefinition(25, 24, 3, 18, 1.0), // ขวาล่าง
        SegmentDefinition(8, 42, 14, 3, 1.2)   // ด้านล่าง
    )

    // ฟังก์ชันหลักในการจดจำตัวเลข รับภาพตัวเลขและคืนค่าเป็นตัวเลข 0-9 หรือ -1 ถ้าไม่สามารถจดจำได้
    fun recognizeDigit(digit: Mat): Int {
        val normalized = normalizeDigit(digit)  // ปรับขนาดภาพให้เป็นมาตรฐาน

        // ตรวจสอบเร็วๆ สำหรับเลข 1 และ 7 เพราะมีรูปแบบที่ชัดเจน
        if (isOne(normalized)) return 1
        if (isSeven(normalized)) return 7

        // วิเคราะห์ segments และคืนค่าความจำเป็น
        val (activeSegments, segmentRatios) = analyzeSegments(normalized)
        normalized.release()  // คืนหน่วยความจำ

        return mapSegmentsToDigit(activeSegments, segmentRatios)
    }

    private fun normalizeDigit(digit: Mat): Mat {
        val normalized = Mat()
        Imgproc.resize(digit, normalized, Size(30.0, 45.0))
        return normalized
    }

    // ตรวจสอบว่าเป็นเลข 1 หรือไม่ โดยดูจากลักษณะเส้นด้านขวามาก แต่ด้านซ้ายน้อย
    private fun isOne(normalized: Mat): Boolean {
        val h = normalized.rows()
        val rightPart = normalized.submat(0, h, 2*normalized.cols()/3, normalized.cols())
        val leftPart = normalized.submat(0, h, 0, normalized.cols()/3)

        val rightPixels = Core.countNonZero(rightPart)  // นับจำนวนพิกเซลด้านขวา
        val leftPixels = Core.countNonZero(leftPart)    // นับจำนวนพิกเซลด้านซ้าย

        rightPart.release()
        leftPart.release()

        return rightPixels > h*0.6 && leftPixels < h*0.2
    }

    // ตรวจสอบว่าเป็นเลข 7 หรือไม่ โดยดูจากลักษณะเส้นด้านบนและด้านขวามาก แต่ด้านซ้ายน้อย
    private fun isSeven(normalized: Mat): Boolean {
        val h = normalized.rows()
        val w = normalized.cols()

        val topPart = normalized.submat(0, h/4, 0, w)
        val rightPart = normalized.submat(0, h, 2*w/3, w)
        val leftPart = normalized.submat(0, h, 0, w/3)

        val topPixels = Core.countNonZero(topPart)      // นับจำนวนพิกเซลด้านบน
        val rightPixels = Core.countNonZero(rightPart)  // นับจำนวนพิกเซลด้านขวา
        val leftPixels = Core.countNonZero(leftPart)    // นับจำนวนพิกเซลด้านซ้าย

        topPart.release()
        rightPart.release()
        leftPart.release()

        return topPixels > w*0.6 && rightPixels > h*0.5 && leftPixels < h*0.3
    }

    // วิเคราะห์แต่ละ segment ว่าเปิดหรือปิด และคำนวณอัตราส่วนของแต่ละ segment
    private fun analyzeSegments(normalized: Mat): Pair<BooleanArray, DoubleArray> {
        val activeSegments = BooleanArray(7)   // เก็บสถานะการเปิด/ปิดของแต่ละ segment
        val segmentRatios = DoubleArray(7)     // เก็บอัตราส่วนของแต่ละ segment

        segments.forEachIndexed { index, seg ->
            val roi = normalized.submat(        // ตัดเฉพาะส่วนที่สนใจ
                seg.y, seg.y + seg.height,
                seg.x, seg.x + seg.width
            )
            val whitePixels = Core.countNonZero(roi)  // นับจำนวนพิกเซลที่เปิด
            val total = roi.rows() * roi.cols()       // พื้นที่ทั้งหมดของ segment
            segmentRatios[index] = whitePixels.toDouble() / total * seg.weight  // คำนวณอัตราส่วน
            activeSegments[index] = segmentRatios[index] > 0.4                  // กำหนดว่า segment นี้เปิดหรือปิด
            roi.release()
        }

        return Pair(activeSegments, segmentRatios)
    }

    // แปลงรูปแบบ segments ที่เปิด/ปิดเป็นตัวเลข
/*    private fun mapSegmentsToDigit(segments: BooleanArray, ratios: DoubleArray): Int {
        // รูปแบบการเปิด/ปิดของ segments สำหรับตัวเลข 0-9
        val patterns = arrayOf(
            booleanArrayOf(true, true, true, false, true, true, true),    // 0: ทุก segment ยกเว้นกลาง
            booleanArrayOf(false, false, true, false, false, true, false), // 1: เฉพาะด้านขวา
            booleanArrayOf(true, false, true, true, true, false, true),    // 2
            booleanArrayOf(true, false, true, true, false, true, true),    // 3
            booleanArrayOf(false, true, true, true, false, true, false),   // 4
            booleanArrayOf(true, true, false, true, false, true, true),    // 5
            booleanArrayOf(true, true, false, true, true, true, true),     // 6
            booleanArrayOf(true, false, true, false, false, true, false),  // 7
            booleanArrayOf(true, true, true, true, true, true, true),      // 8: ทุก segment
            booleanArrayOf(true, true, true, true, false, true, true)      // 9
        )

        var bestMatch = -1
        var maxScore = 0.0

        // เปรียบเทียบกับรูปแบบทั้งหมดเพื่อหาตัวเลขที่ตรงที่สุด
        patterns.forEachIndexed { digit, pattern ->
            var score = 0.0
            var matches = 0
            var mismatches = 0

            // นับจำนวน segments ที่ตรงกัน
            for (i in pattern.indices) {
                if (pattern[i] == segments[i]) {
                    matches++
                    score += if (segments[i]) ratios[i] else 0.3
                } else {
                    mismatches++
                }
            }

            // เพิ่มคะแนนพิเศษตามลักษณะเฉพาะของแต่ละตัวเลข
            when (digit) {
                0 -> if (matches >= 6 && !segments[3]) score += 4.0         // ไม่มีขีดกลาง
                1 -> if (segments[2] && segments[5] && !segments[0] && !segments[4]) score += 4.0  // เส้นขวาชัดเจน
                2 -> if (matches >= 5 && segments[3]) score += 2.5          // มีขีดกลาง
                3 -> if (segments[0] && segments[3] && segments[6]) score += 2.5  // มีขีดบน กลาง ล่าง
                4 -> if (segments[2] && segments[3] && segments[5]) score += 2.5  // มีขีดขวาและกลาง
                5 -> if (segments[1] && segments[3] && segments[5]) score += 2.5  // มีขีดซ้ายบน กลาง ขวาล่าง
                6 -> if (matches >= 6 && segments[4]) score += 2.5          // มีขีดซ้ายล่าง
                7 -> if (segments[0] && segments[2] && segments[5]) score += 3.5  // มีขีดบน ขวาบน ขวาล่าง
                8 -> if (matches >= 7) score += 4.0                         // มีครบทุกขีด
                9 -> if (matches >= 6 && !segments[4]) score += 3.5         // ไม่มีขีดซ้ายล่าง
            }

            // เก็บค่าตัวเลขที่มีคะแนนสูงสุด
            if (score > maxScore) {
                maxScore = score
                bestMatch = digit
            }
        }

        return if (maxScore > 2.0) bestMatch else -1  // ถ้าคะแนนต่ำกว่า 2.0 ถือว่าไม่สามารถระบุตัวเลขได้
    }*/

    private fun mapSegmentsToDigit(segments: BooleanArray, ratios: DoubleArray): Int {
        val patterns = arrayOf(
            booleanArrayOf(true, true, true, false, true, true, true),    // 0
            booleanArrayOf(false, false, true, false, false, true, false), // 1
            booleanArrayOf(true, false, true, true, true, false, true),    // 2
            booleanArrayOf(true, false, true, true, false, true, true),    // 3
            booleanArrayOf(false, true, true, true, false, true, false),   // 4
            booleanArrayOf(true, true, false, true, false, true, true),    // 5
            booleanArrayOf(true, true, false, true, true, true, true),     // 6
            booleanArrayOf(true, true, true, false, false, true, false),   // 7: รูปแบบใหม่
            booleanArrayOf(true, false, true, false, false, true, false),  // 7: รูปแบบเดิม
            booleanArrayOf(true, true, true, true, true, true, true),      // 8
            booleanArrayOf(true, true, true, true, false, true, true)      // 9
        )

        var bestMatch = -1
        var maxScore = 0.0

        patterns.forEachIndexed { index, pattern ->
            var score = 0.0
            var matches = 0
            var mismatches = 0

            for (i in pattern.indices) {
                if (pattern[i] == segments[i]) {
                    matches++
                    score += if (segments[i]) ratios[i] else 0.3
                } else {
                    mismatches++
                }
            }

            // ปรับปรุงการให้คะแนนพิเศษ
            when {
                // เลข 0 - ตรวจสอบว่ามีเส้นด้านข้างครบ
                index == 0 -> {
                    if (matches >= 6 && !segments[3] && // ไม่มีเส้นกลาง
                        ((segments[1] && segments[2]) || // มีเส้นด้านข้างบนทั้งคู่
                                (segments[4] && segments[5]))) { // หรือมีเส้นด้านข้างล่างทั้งคู่
                        score += 4.0
                    }
                }
                // เลข 7 ทั้งสองรูปแบบ
                index == 7 || index == 8 -> {
                    if (segments[0] && segments[2] && segments[5] && // มีเส้นบน ขวาบน และขวาล่าง
                        !segments[3] && !segments[6] && // ไม่มีเส้นกลางและเส้นล่าง
                        segments[1] == pattern[1]) { // เช็คเส้นซ้ายบนตามรูปแบบ
                        score += 4.5
                    }
                }

                index == 1 -> if (segments[2] && segments[5] && !segments[0] && !segments[4]) score += 4.0
                index == 2 -> if (matches >= 5 && segments[3]) score += 2.5
                index == 3 -> if (segments[0] && segments[3] && segments[6]) score += 2.5
                index == 4 -> if (segments[2] && segments[3] && segments[5]) score += 2.5
                index == 5 -> if (segments[1] && segments[3] && segments[5]) score += 2.5
                index == 6 -> if (matches >= 6 && segments[4]) score += 2.5
                index == 9 -> if (matches >= 7) score += 4.0
                index == 10 -> if (matches >= 6 && !segments[4]) score += 3.5
            }

            if (score > maxScore) {
                maxScore = score
                bestMatch = when (index) {
                    7, 8 -> 7
                    in 9..10 -> index - 1
                    else -> index
                }
            }

        }

        return if (maxScore > 2.0) bestMatch else -1
    }
}