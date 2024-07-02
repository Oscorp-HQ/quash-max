package com.quash.bugger.data

import retrofit2.Call
import retrofit2.http.GET

interface ApiServices {
    @GET("{user}")
    fun listProduct(@retrofit2.http.Path("user") user: String?): Call<Any>
}