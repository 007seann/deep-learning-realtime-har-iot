package com.specknet.pdiotapp.preprocessing

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/preprocess")
    fun sendSensorData(@Body sensorData: ModelSensorData): Call<List<TestResponseData>>
}