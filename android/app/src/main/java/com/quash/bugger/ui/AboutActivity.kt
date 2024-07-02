package com.quash.bugger.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.quash.bugger.data.RetrofitClient
import com.quash.bugger.databinding.ActivityAboutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AboutActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(binding.root)
        //RetrofitClient.client.listRepos("")
        val call : Call<Any> = RetrofitClient.client.listProduct("comments")
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
            startActivity(Intent(this@AboutActivity, HomeActivity::class.java))
        }
    }
}