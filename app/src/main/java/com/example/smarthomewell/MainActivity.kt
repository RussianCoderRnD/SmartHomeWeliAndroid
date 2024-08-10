package com.example.smarthomewell // Определение пакета, к которому принадлежит класс

// 20.07.2024 Lugovik O.V. // Дата и автор комментария

import androidx.activity.enableEdgeToEdge // Импорт функции для активации режима без краёв
import androidx.core.view.ViewCompat // Импорт класса для работы с совместимостью представлений
import androidx.core.view.WindowInsetsCompat // Импорт класса для работы с отступами окон
import android.widget.Button // Импорт класса Button для создания кнопок
import okhttp3.* // Импорт всего пакета okhttp3 для работы с HTTP-запросами
import okhttp3.MediaType.Companion.toMediaTypeOrNull // Импорт метода расширения для преобразования строки в MediaType
import okhttp3.RequestBody.Companion.toRequestBody // Импорт метода расширения для преобразования строки в RequestBody
import java.util.concurrent.TimeUnit // Импорт класса для работы с единицами времени
import kotlin.random.Random // Импорт класса для генерации случайных чисел
import android.graphics.Bitmap // Импорт класса Bitmap для работы с изображениями
import android.graphics.BitmapFactory // Импорт класса для создания Bitmap из различных источников
import android.os.Bundle // Импорт класса Bundle для передачи данных между компонентами Android
import android.widget.ImageButton // Импорт класса ImageButton для создания кнопок с изображениями
import android.widget.ImageView // Импорт класса ImageView для отображения изображений
import android.widget.TextView // Импорт класса TextView для отображения текста
import androidx.appcompat.app.AppCompatActivity // Импорт класса AppCompatActivity для создания активности с поддержкой старых версий Android
import kotlinx.coroutines.CoroutineScope // Импорт класса CoroutineScope для запуска корутин
import kotlinx.coroutines.Dispatchers // Импорт класса Dispatchers для указания контекста корутины
import kotlinx.coroutines.launch // Импорт функции launch для запуска корутин
import okhttp3.OkHttpClient // Импорт класса OkHttpClient для выполнения HTTP-запросов
import okhttp3.Request // Импорт класса Request для представления HTTP-запросов
import okhttp3.Response // Импорт класса Response для представления HTTP-ответов
import org.json.JSONObject // Импорт класса JSONObject для работы с JSON-данными
import android.content.pm.ActivityInfo // Импорт класса ActivityInfo для информации о конфигурации активности
import android.os.Handler // Импорт класса Handler для обработки сообщений и задач
import android.os.Looper // Импорт класса Looper для работы с циклами сообщений
import android.view.View // Импорт класса View для базовых визуальных элементов в Android
import java.io.IOException // Импорт класса IOException для обработки ошибок ввода-вывода
import android.content.Intent // Импорт класса Intent для запуска новых активностей или служб
import android.widget.Toast // Импорт класса Toast для отображения всплывающих сообщений
import android.graphics.Color // Импорт класса Color для работы с цветами
import android.util.Log // Импорт класса Log для ведения журнала
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

var jsonString = "" // Глобальная переменная для хранения JSON строки
var count = 0 // Глобальная переменная для хранения температуры
var memHysteresis = 0 // Глобальная переменная для хранения гистерезиса
var menPressure = 0 // Глобальная переменная для хранения давления
var counPower = 0 // Глобальная переменная для хранения мощности

class MainActivity : AppCompatActivity() { // Определение класса MainActivity, который наследует от AppCompatActivity

    // Инициализация переменных для значений JSON объекта
    private var countHys: Double = 0.0 // Переменная для хранения значения гистерезиса
    private var countPres: Double = 0.0 // Переменная для хранения значения давления

    // Создаем клиент OkHttp
    private val client = OkHttpClient() // Инициализация клиента для выполнения HTTP-запросов

    companion object {
        // Статические URL для вашего ESP8266
        const val espUrlExo = "http://192.168.4.10/" // URL для основного ресурса
        const val espUrlOnOff = "http://192.168.4.10/onoff" // URL для включения/выключения
        const val espUrlRes = "http://192.168.4.10/res" // URL для получения данных
        const val espUrlCount = "http://192.168.4.10/count" // URL для работы с параметром count
        const val espUrlHysteresis = "http://192.168.4.10/hysteresis" // URL для работы с параметром hysteresis

        private var PR: Double = 0.0 // Переменная для значения PR
        private var COUNT: Double = 0.0 // Переменная для значения COUNT
        private var onoff: Boolean = false // Переменная для значения включено/выключено
        private var HYSTERESIS: Double = 0.0 // Переменная для значения гистерезиса
        private var RelayOnOff: Boolean = false // Переменная для значения реле включено/выключено
        private var DS18B20_sensor_status: Boolean = false // Переменная для статуса датчика DS18B20
        private var DS18B20_temperature: Double = 0.0 // Переменная для температуры датчика DS18B20
        private var ONOFF: Boolean = false // Переменная для значения включено/выключено
    }

    private lateinit var textViewVarHys: TextView // Переменная для TextView гистерезиса
    private lateinit var textViewVarPres: TextView // Переменная для TextView давления
    private lateinit var textViewHysPomp: TextView // Переменная для TextView гистерезиса насоса
    private lateinit var textViewPressPomp: TextView // Переменная для TextView давления насоса
    private lateinit var textViewPresure: TextView // Переменная для TextView давления
    private lateinit var textViewCount: TextView // Переменная для TextView счетчика
    private lateinit var textViewOnOff: TextView // Переменная для TextView включено/выключено
    private lateinit var textViewHysteresis: TextView // Переменная для TextView гистерезиса
    private lateinit var textViewTemperature: TextView // Переменная для TextView температуры
    private lateinit var textViewSensorStatus: TextView // Переменная для TextView статуса датчика
    private lateinit var textViewRalyOnOff: TextView // Переменная для TextView включено/выключено реле
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем ориентацию на портретную необходимо импортировать --- import android.content.pm.ActivityInfo
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_main)

        // Проверяем, нужно ли закрыть приложение
        if (intent.getBooleanExtra("EXIT", false)) {
            finish()
        }

        // Скрыть статусную строку
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        enableEdgeToEdge()

        // функция закрывает приложение по нажатию определённой картинки
        val closeButton: ImageView = findViewById(R.id.imageButtonExit)
        closeButton.setOnClickListener {
            intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Удаляет все предыдущие активности из стека
            intent.putExtra("EXIT", true)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация TextView
        textViewVarHys = findViewById(R.id.textViewVarHys)
        textViewVarPres = findViewById(R.id.textViewVarPres)
        textViewOnOff = findViewById(R.id.textViewOnOff)

        textViewPresure = findViewById(R.id.textViewPresure)
        textViewOnOff = findViewById(R.id.textViewOnOff)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        textViewSensorStatus = findViewById(R.id.textViewSensorStatus)
        textViewRalyOnOff = findViewById(R.id.textViewRalyOnOff)

        imageView4 = findViewById(R.id.imageView4)
        imageView3 = findViewById(R.id.imageView3)
        imageView2 = findViewById(R.id.imageView2)

        findViewById<ImageButton>(R.id.imageButtonSave).setOnClickListener {
            print("imageButtonSave ")
            fetchDataFromServer()
        }

        // Обновите setOnClickListener для кнопки
        findViewById<ImageButton>(R.id.imageButtonPowerOnOff).setOnClickListener {
            ONOFF = !ONOFF // Меняем значение на противоположное
            // Установка значения переменной в TextView
          //  textViewVarHys.text = if (ONOFF) "1" else "0"
            println("ONOFF: $ONOFF")
            ONOFF(ONOFF) // Отправляем новое значение на сервер
        }



        findViewById<ImageButton>(R.id.imageButtonHysteresisDown).setOnClickListener {
            HYSTERESIS=HYSTERESIS-0.01
            if (HYSTERESIS <= 0.0) HYSTERESIS = 0.0
            // Установка значения переменной в TextView
            textViewVarHys.text = "$HYSTERESIS"
            print("HYSTERESIS ")
            println(HYSTERESIS)
            sendDataToServer("hysteresis", "hysteresis", HYSTERESIS.toString())
        }

        findViewById<ImageButton>(R.id.imageButtonHysteresisUp).setOnClickListener {
            HYSTERESIS=HYSTERESIS+0.01
            // Установка значения переменной в TextView
            textViewVarHys.text = "$HYSTERESIS"
            print("HYSTERESIS ")
            println(HYSTERESIS)
            sendDataToServer("hysteresis", "hysteresis", HYSTERESIS.toString())
        }

        findViewById<ImageButton>(R.id.imageButtonPressureDown).setOnClickListener {

            COUNT=COUNT-0.01
            if (COUNT <= 0.0) COUNT = 0.0
            // Установка значения переменной в TextView
            textViewVarPres.text = "$COUNT"
            print("COUNT ")
            println(COUNT)
            sendDataToServer("count", "count", COUNT.toString())
        }

        findViewById<ImageButton>(R.id.imageButtonPressureUp).setOnClickListener {
            COUNT= COUNT+0.01
            // Установка значения переменной в TextView
            textViewVarPres.text = "$COUNT"
            print("COUNT ")
            println(COUNT)
            sendDataToServer("count", "count", COUNT.toString())
        }
    }

    /************************************/
    // Функция для запроса данных с сервера
    private fun fetchDataFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = get("http://192.168.4.10/res")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        runOnUiThread {
                            parseJsonAndAssignValues(json)
                            showToast("Данные успешно получены с сервера")
                        }
                    }
                } else {
                    runOnUiThread {
                        showToast("Ошибка от сервера: ${response.code}")
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    showToast("Ошибка ввода-вывода: ${e.message}")
                }
            }
        }
    }

    /*************************/
    // Функция для парсинга JSON и присвоения значений переменным
    private fun parseJsonAndAssignValues(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            PR = jsonObject.getDouble("PR")
            COUNT = jsonObject.getDouble("COUNT")
            onoff = jsonObject.getBoolean("ONOFF") //ONOFF = if (jsonObject.getBoolean("ONOFF")) 1 else 0
           HYSTERESIS = jsonObject.getDouble("HYSTERESIS")
           RelayOnOff = jsonObject.getBoolean("RelayOnOff")
            DS18B20_sensor_status = jsonObject.getBoolean("DS18B20_sensor_status")
            DS18B20_temperature = jsonObject.getDouble("DS18B20_temperature")

            textViewPresure.text = "$PR"
            textViewVarPres.text = "$COUNT"
            textViewOnOff.text = "$onoff"
          /* textViewVarHys.text = "$HYSTERESIS"*/
            textViewRalyOnOff.text = "$RelayOnOff"
          /*  textViewSensorStatus.text = "$DS18B20_sensor_status"*/
            textViewTemperature.text = "$DS18B20_temperature"


            // Выводим каждое значение в новой строке для улучшения читаемости
            println("""
            PR: $PR
            COUNT: $COUNT
            ONOFF: $onoff
            HYSTERESIS: $HYSTERESIS
            RelayOnOff: $RelayOnOff
            DS18B20_sensor_status: $DS18B20_sensor_status
            DS18B20_temperature: $DS18B20_temperature
        """.trimIndent())
                    if (DS18B20_sensor_status) {imageView4.setImageResource(R.drawable.lampon)} else {imageView4.setImageResource(R.drawable.lampoff)}
                    if (RelayOnOff) {imageView3.setImageResource(R.drawable.lampon)} else {imageView3.setImageResource(R.drawable.lampoff)}
                    if (onoff) {imageView2.setImageResource(R.drawable.lampon)} else {imageView2.setImageResource(R.drawable.lampoff)}
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception occurred while parsing JSON: ${e.message}")
        }
    }


    // Функция для отображения всплывающего сообщения
    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }


    // Функция для отправки текущего значения счетчика на сервер
    private fun ONOFF(onoff: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "http://192.168.4.10/onoff?onoff=${if (onoff) 1 else 0}"
                val response = get(url)
                if (response.isSuccessful) {
                    println("ONOFF sent successfully: $onoff")
                } else {
                    println("Failed to send ONOFF: ${response.code} ${response.message}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                println("IOException occurred: ${e.message}")
            }
        }
    }


    // Функция для выполнения GET запроса
    @Throws(IOException::class)
    private fun get(url: String): Response {
        // Функция выполняет GET-запрос по указанному URL и возвращает объект Response
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0") // Устанавливаем заголовок User-Agent для имитации браузера
            .build()
        println("Sending GET request to $url") // Выводим в консоль информацию о том, что отправляем GET-запрос
        return client.newCall(request).execute() // Выполняем запрос и возвращаем результат в виде объекта Response
    }

    private fun sendDataToServer(endpoint: String, paramName: String, paramValue: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "http://192.168.4.10/$endpoint?$paramName=$paramValue"
                val response = get(url)
                if (response.isSuccessful) {
                    println("$paramName sent successfully: $paramValue")
                } else {
                    println("Failed to send $paramName: ${response.code} ${response.message}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                println("IOException occurred: ${e.message}")
            }
        }
    }

}