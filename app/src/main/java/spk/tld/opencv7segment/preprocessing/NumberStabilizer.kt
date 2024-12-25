package spk.tld.opencv7segment.preprocessing

import java.util.ArrayDeque

/**
 * คลาสสำหรับทำให้การอ่านตัวเลขมีความเสถียร
 * ตรวจสอบว่าตัวเลขที่อ่านได้ซ้ำกันติดต่อกันตามจำนวนที่กำหนดหรือไม่
 */
class NumberStabilizer(
    private val stableThreshold: Int = 3  // จำนวนครั้ง
) {
    // ใช้ ArrayDeque เก็บตัวเลขย้อนหลัง 5 ค่าล่าสุด
    private val previousNumbers = ArrayDeque<String>(5)

    /**
     * เพิ่มตัวเลขที่อ่านได้ล่าสุดและตรวจสอบความเสถียร
     * @param number ตัวเลขที่อ่านได้ล่าสุด
     * @return true ถ้าตัวเลขมีความเสถียร (ซ้ำกันตามจำนวน stableThreshold)
     */
    fun addNumber(number: String): Boolean {
        previousNumbers.addLast(number)  // เพิ่มตัวเลขล่าสุด
        if (previousNumbers.size > 5) {  // ถ้ามีมากกว่า 5 ตัว
            previousNumbers.removeFirst() // ลบตัวแรกออก
        }

        return isStable(number)  // ตรวจสอบความเสถียร
    }

    /**
     * ตรวจสอบว่าตัวเลขมีความเสถียรหรือไม่
     * @param currentNumber ตัวเลขที่จะตรวจสอบ
     * @return true ถ้าตัวเลขซ้ำกันติดต่อกันตามจำนวน stableThreshold
     */
    private fun isStable(currentNumber: String): Boolean {
        // ตรวจสอบว่ามีจำนวนตัวเลขมากกว่าหรือเท่ากับ threshold
        // และตัวเลขทั้งหมดต้องเหมือนกับตัวเลขปัจจุบัน
        return previousNumbers.size >= stableThreshold &&
                previousNumbers.all { it == currentNumber }
    }
}