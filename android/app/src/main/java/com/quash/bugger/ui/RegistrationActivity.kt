package com.quash.bugger.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.quash.bugger.data.RetrofitClient
import com.quash.bugger.databinding.ActivityRegistrionBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationActivity : AppCompatActivity() {
    private val binding by lazy { ActivityRegistrionBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.btnSignUp.isEnabled = true
        val call : Call<Any> = RetrofitClient.client.listProduct("posts")
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
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this@RegistrationActivity, AboutActivity::class.java))
        }
    }
}