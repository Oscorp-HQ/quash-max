package com.quash.bugger.data

/*
import com.quash.bugs.Quash
*/
import com.quash.bugs.Quash
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val BASE_URL = "https://dummyjson.com/"
    private var retrofit: Retrofit? = null


    fun getOkHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        return logger
    }

    fun getHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        Quash.getInstance().getNetworkInterceptor()?.let {
            builder.addInterceptor(it)
        }
        return builder.build()
    }

    val client: ApiServices
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiServices::class.java)
        }
}