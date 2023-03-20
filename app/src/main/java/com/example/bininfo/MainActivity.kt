package com.example.bininfo

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View.*
import com.example.bininfo.adapter.RequestAdapter
import com.example.bininfo.databinding.ActivityMainBinding
import com.example.bininfo.model.BINInfoApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val adapter = RequestAdapter()
    lateinit var requestList: MutableSet<String>
    var pref : SharedPreferences? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = getSharedPreferences("Request", MODE_PRIVATE)

        requestList = pref?.getStringSet("bin", mutableSetOf<String>())!!

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://lookup.binlist.net").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val binInfoApi = retrofit.create(BINInfoApi::class.java)

        var latitude = ""
        var longitude = ""

        binding.button.setOnClickListener {
            val binNumber = binding.etBinNumber.text
            if (binNumber.length >= 8) {
                requestList.add(binNumber.toString())
                saveData(requestList)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val bankInfo = binInfoApi.getBINInfoByNumber(binNumber)
                        latitude = bankInfo.country.latitude.toString()
                        longitude = bankInfo.country.longitude.toString()
                        runOnUiThread {
                            binding.tvScheme.text = "SCHEME / NETWORK" + System.lineSeparator() + (bankInfo.scheme ?: "?")
                            binding.tvBrand.text = "BRAND" + System.lineSeparator() + (bankInfo.brand ?: "?")
                            binding.tvCardNumber.text = "CARD NUMBER" + System.lineSeparator() + "LENGTH" + System.lineSeparator() +
                                    (if (bankInfo.number.length == 0) "?" else bankInfo.number.length) + System.lineSeparator() +
                                    "LUHN" + System.lineSeparator() + if (bankInfo.number.luhn) "Yes" else "No"
                            binding.tvType.text = "TYPE" + System.lineSeparator() + (bankInfo.type ?: "?")
                            binding.tvPrepaid.text = "PREPAID" + System.lineSeparator() + if (bankInfo.prepaid) "Yes" else "No"
                            binding.tvCountry.text = "COUNTRY" + System.lineSeparator() + bankInfo.country.emoji + " " + bankInfo.country.name + System.lineSeparator() +
                                    "(latitude: " + bankInfo.country.latitude + ", longitude: " + bankInfo.country.longitude + ")"
                            binding.tvBank.text = "BANK" + System.lineSeparator() + (bankInfo.bank.name ?: "?") + System.lineSeparator() + (bankInfo.bank.city ?: "")
                            binding.tvUrl.text = bankInfo.bank.url ?: ""
                            binding.tvPhone.text = bankInfo.bank.phone ?: ""
                        }
                    } catch (e: HttpException) {
                        runOnUiThread {
                            binding.tvScheme.text = "Пожалуйста, проверьте корректность введенного BIN или попробуйте позже"
                            binding.tvBrand.text = ""
                            binding.tvCardNumber.text = ""
                            binding.tvType.text = ""
                            binding.tvPrepaid.text = ""
                            binding.tvCountry.text = ""
                            binding.tvBank.text = ""
                            binding.tvUrl.text = ""
                            binding.tvPhone.text = ""
                        }
                    }
                }
            } else {
                binding.tvScheme.text = "Пожалуйста, введите корректный BIN"
                binding.tvBrand.text = ""
                binding.tvCardNumber.text = ""
                binding.tvType.text = ""
                binding.tvPrepaid.text = ""
                binding.tvCountry.text = ""
                binding.tvBank.text = ""
                binding.tvUrl.text = ""
                binding.tvPhone.text = ""
            }
        }

        binding.tvUrl.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://" + binding.tvUrl.text))
            startActivity(intent)
        }

        binding.tvPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + binding.tvPhone.text))
            startActivity(intent)
        }

        binding.tvCountry.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$latitude,$longitude"))
            startActivity(intent)
        }

        binding.tvHistory.setOnClickListener {
            if (!requestList.isEmpty()) {
                binding.rvHistory.visibility = VISIBLE
                binding.tvHide.visibility = VISIBLE
                binding.tvClear.visibility = VISIBLE
                init()
            }
        }

        binding.tvHide.setOnClickListener {
            binding.rvHistory.visibility = GONE
            binding.tvHide.visibility = GONE
            binding.tvClear.visibility = GONE
        }

        binding.tvClear.setOnClickListener {
            clearRequestHistory()
            binding.tvHide.visibility = GONE
            binding.tvClear.visibility = GONE
        }

    }

    fun saveData(requestList: MutableSet<String>) {
        val editor = pref?.edit()
        editor?.putStringSet("bin", requestList)
        editor?.apply()
    }

    fun clearRequestHistory() {
        binding.rvHistory.visibility = GONE
        requestList.clear()
        val editor = pref?.edit()
        editor?.clear()
        editor?.apply()
    }

    private fun init() {
        binding.apply {
            rvHistory.layoutManager = LinearLayoutManager(this@MainActivity)
            rvHistory.adapter = adapter
            adapter.setList(requestList)
        }
    }
}