package com.example.bininfo.model

import android.text.Editable
import retrofit2.http.GET
import retrofit2.http.Path

interface BINInfoApi {
    @GET("{id}")
    suspend fun getBINInfoByNumber(@Path("id") id: Editable): BINInfo
}