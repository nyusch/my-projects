package com.example.testtaskforfocusstart

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.testtaskforfocusstart.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.buttonGet.setOnClickListener {
            val cardnumber = binding.cardbin.text
            Log.d("MyLog", "$cardnumber")
            getBinList("$cardnumber")
        }

    }

    private fun getBinList(name: String) {
        val url = "https://lookup.binlist.net/$name"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET,
            url,
            { response ->
                val obj = JSONObject(response)
                Log.d("MyLog", "Response: $response")
                parseData(obj)
            },
            {
                Log.d("MyLog", "Volley error: $it")
            }
        )
        queue.add(stringRequest)
    }

    @SuppressLint("SetTextI18n")
    private fun parseData(result: JSONObject) {
        val mainObject = result
        var length: String? = ""
        var luhn: String?= ""
        var scheme : String?= ""
        var type : String?= ""
        var brand : String?= ""
        var prepaid : String?= ""
        var numeric : String?= ""
        var alpha2 : String?= ""
        var countryName : String?= ""
        var emoji : String?= ""
        var currency : String?= ""
        var latitude : String?= ""
        var longitude : String?= ""
        var bankName : String?= ""
        var url : String?= ""
        var phone : String?= ""
        var city : String?= ""
        try {
            length = mainObject.getJSONObject("number").getString("length")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            luhn = mainObject.getJSONObject("number").getString("luhn")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            scheme = mainObject.getString("scheme")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            type = mainObject.getString("type")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            brand = mainObject.getString("brand")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            prepaid = mainObject.getString("prepaid")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            numeric = mainObject.getJSONObject("country").getString("numeric")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            alpha2 = mainObject.getJSONObject("country").getString("alpha2")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            countryName = mainObject.getJSONObject("country").getString("name")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            emoji = mainObject.getJSONObject("country").getString("emoji")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            currency = mainObject.getJSONObject("country").getString("currency")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            latitude = mainObject.getJSONObject("country").getString("latitude")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            longitude = mainObject.getJSONObject("country").getString("longitude")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            bankName = mainObject.getJSONObject("bank").getString("name")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            url = mainObject.getJSONObject("bank").getString("url")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            phone = mainObject.getJSONObject("bank").getString("phone")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }
        try {
            city = mainObject.getJSONObject("bank").getString("city")
        } catch (e: JSONException) {
            Log.d("MyLog", "$e")
        }

        val item = CardModel(length, luhn, scheme, type, brand, prepaid, numeric, alpha2, countryName, emoji, currency, latitude, longitude, bankName, url, phone, city)
        binding.temp.visibility = View.VISIBLE
        binding.temp.text =
            "\nSCHEME / NETWORK: ${item.scheme}" +
                    "\nTYPE : ${item.type}" +
                    "\nBRAND: ${item.brand}" +
                    "\nPREPAID: ${item.prepaid}" +
                    "\nCARD NUMBER" +
                    "\nLENGTH: ${item.length} \nLUHN: ${item.luhn} \n" +
                    "\nCOUNTRY" +
                    "\n${item.emoji} ${item.countryName}" +
                    "\nlatitude: ${item.latitude} \nlongitude: ${item.longitude} \n" +
                    "\nBANK" +
                    "\n${item.bankName} \n${item.city}" +
                    "\n${item.url}" +
                    "\n${item.phone}"
        Log.d("MyLog", "bank : ${item.bankName}")

    }

}