package com.quash.bugger.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.quash.bugger.data.ApiServices
import com.quash.bugger.data.RetrofitClient
import com.quash.bugger.data.RetrofitClient.client
import com.quash.bugger.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val call : Call<Any> = client.listProduct("users")
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
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
            //throw RuntimeException("This is a test crash.")

        }
    }


}