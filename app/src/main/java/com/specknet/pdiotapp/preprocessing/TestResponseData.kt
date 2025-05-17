package com.specknet.pdiotapp.preprocessing

data class TestResponseData(
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val accelMag: Float,
    val jerkX: Float,
    val jerkY: Float,
    val jerkZ: Float,
    val jerkMag: Float,
    val pcaAccel: Float,
    val pcaJerk: Float
) {

    fun toArray(): FloatArray {
        return floatArrayOf(
            accelX,
            accelY,
            accelZ,
            accelMag,
            jerkX,
            jerkY,
            jerkZ,
            jerkMag,
            pcaAccel,
            pcaJerk
        )
    }
}
