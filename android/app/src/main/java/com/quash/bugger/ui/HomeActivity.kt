package com.quash.bugger.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.quash.bugger.data.RetrofitClient
import com.quash.bugger.databinding.ActivityHomeBinding
import com.quash.bugs.Quash
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val call : Call<Any> = RetrofitClient.client.listProduct("todos")
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    // Print the response in the Logcat
                    //Log.d(ContentValues.TAG, "API Response: ${response.body()}")
                } else {
                    //Log.e(ContentValues.TAG, "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.e(ContentValues.TAG, "Error: ${t.message}")
            }
        })
        binding.ivCard.setOnClickListener {
            throw NullPointerException()
        }
        binding.ivCard2.setOnClickListener {
            Quash.getInstance().onShakeDetected()
        }
    }
}