package com.specknet.pdiotapp.preprocessing

data class RawAccelGyroData(val accelX: Float, val accelY: Float, val accelZ: Float,
                            val gyroX: Float, val gyroY: Float, val gyroZ: Float) {


    fun toFloatArray(): FloatArray {
        return floatArrayOf(accelX, accelY, accelZ, gyroX, gyroY, gyroZ)
    }
}