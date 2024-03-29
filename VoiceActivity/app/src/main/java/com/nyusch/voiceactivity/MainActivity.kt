package com.nyusch.voiceactivity

import android.content.Intent
import android.content.SharedPreferences.Editor
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    val TAG: String = "MainActivity";
    lateinit var requestInput: TextInputEditText
    lateinit var podsAdapter: SimpleAdapter
    lateinit var progressBar: ProgressBar
    lateinit var waEngine: WAEngine
    lateinit var textToSpeech: TextToSpeech
    var isTtsReady : Boolean = false
    val VOICE_RECOGNITION_REQUEST_CODE: Int = 777
    val pods = mutableListOf<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initWolframEngine()
        initTts()
    }

    fun initViews() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        requestInput = findViewById(R.id.text_input_edit)
        requestInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                 pods.clear()
                podsAdapter.notifyDataSetChanged()

                val question = requestInput.text.toString()
                askWolfram(question)
            }
            return@setOnEditorActionListener false
        }

        val podsList: ListView = findViewById(R.id.pods_list)
        podsAdapter = SimpleAdapter(
            applicationContext,
            pods,
            R.layout.item_pod,
            arrayOf("Title", "Content"),
            intArrayOf(R.id.title, R.id.content)
        )
        podsList.adapter = podsAdapter
        podsList.setOnItemClickListener {parent, view, position, id ->
            if (isTtsReady){ Log.d(TAG, "itsready")
                val title = pods[position]["Title"]
                val content = pods[position]["Content"]
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, title)
            }
        }

        val voice_input_button: FloatingActionButton = findViewById(R.id.voice_input_button)
        voice_input_button.setOnClickListener {
           // Log.d(TAG, "FAB")
            pods.clear()
            podsAdapter.notifyDataSetChanged()

            if (isTtsReady)
            {
                textToSpeech.stop()
            }
            showVoiceInputDialog()
        }
        progressBar = findViewById(R.id.progress_bar)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_stop -> {
                //Log.d(TAG, "actions_stop")
                if (isTtsReady)
                {
                    textToSpeech.stop()
                }
                return true
            }
            R.id.action_clear -> {
                requestInput.text?.clear()
                pods.clear()
                podsAdapter.notifyDataSetChanged()
                Log.d(TAG, "actions_clear")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun initWolframEngine() {
        waEngine = WAEngine().apply {
            appID = "3PVV7G-4Q5T6U8RRP";
            addFormat("plainText")
        }
    }

    fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
            .apply {
                setAction(android.R.string.ok) {
                    dismiss()
                }
                show()
            }
    }

    fun askWolfram(request: String) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val query = waEngine.createQuery().apply {
                input = request
            }
            runCatching {
                waEngine.performQuery(query)
            }.onSuccess { result ->
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    //Обработать запрос - ответ
                    if (result.isError) {
                        showSnackbar(result.errorMessage)
                        return@withContext
                    }
                    if (!result.isSuccess) {
                        requestInput.error = getString(R.string.error_do_not_understand)
                        return@withContext
                    }
                    for (pod in result.pods) {
                        if (pod.isError) continue
                        val content = StringBuilder()
                        for (subpod in pod.subpods) {
                            for (element in subpod.contents) {
                                if (element is WAPlainText) {
                                    content.append(element.text)
                                }
                            }
                        }
                        pods.add(0, HashMap<String, String>().apply {
                            Log.d(TAG, "Yyyyyyeeeees, exactly")
                            put("Title", pod.title)
                            put("Content", content.toString())
                        })
                    }
                    podsAdapter.notifyDataSetChanged()
                }
            }.onFailure { t ->
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    //обработка ошибки
                    showSnackbar(t.message ?: getString(R.string.error_something_went_wrong))
                }
            }
        }
    }
    fun initTts(){
        textToSpeech = TextToSpeech(this) { code ->
            if (code != TextToSpeech.SUCCESS)
            {
                Log.e(TAG, "TTs error code: $code")
                showSnackbar(getString(R.string.error_tts_is_not_ready))
            }
            else
            {
                isTtsReady = true
            }
            textToSpeech.language = Locale.US
        }
    }
    fun showVoiceInputDialog()
    {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.request_hint))
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        }
        runCatching {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
        }.onFailure { t ->
            showSnackbar(t.message ?: getString(R.string.error_voice_recognition_unavailable))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
        {
            data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)?.let { question ->
                requestInput.setText(question)
                askWolfram(question)
            }
        }
    }
}