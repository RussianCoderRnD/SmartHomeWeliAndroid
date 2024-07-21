package com.example.smarthomewell

// 20.07.2024 Lugovik O.V.
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle // Импортируем класс Bundle, который используется для передачи данных между компонентами Android
import android.widget.ImageButton // Импортируем класс ImageButton, который используется для создания кнопок с изображениями
import android.widget.ImageView // Импортируем класс ImageView, который используется для отображения изображений
import android.widget.TextView // Импортируем класс TextView, который используется для отображения текста
import androidx.appcompat.app.AppCompatActivity // Импортируем класс AppCompatActivity, который является базовым классом для активностей, использующих современные компоненты Android
import kotlinx.coroutines.CoroutineScope // Импортируем класс CoroutineScope, который используется для запуска корутин
import kotlinx.coroutines.Dispatchers // Импортируем класс Dispatchers, который предоставляет диспетчеры для корутин
import kotlinx.coroutines.launch // Импортируем функцию launch, которая запускает новую корутину
import okhttp3.OkHttpClient // Импортируем класс OkHttpClient, который используется для выполнения HTTP-запросов
import okhttp3.Request // Импортируем класс Request, который представляет HTTP-запрос
import okhttp3.Response // Импортируем класс Response, который представляет HTTP-ответ
import org.json.JSONObject // Импортируем класс JSONObject, который используется для работы с JSON-данными
import android.content.pm.ActivityInfo // Импортируем класс ActivityInfo, который содержит информацию о конфигурации активности
import android.os.Handler // Импортируем класс Handler, который позволяет отправлять и обрабатывать сообщения и выполняемые объекты, связанные с потоком
import android.os.Looper // Импортируем класс Looper, который предоставляет цикл событий для обработки сообщений
import android.view.View // Импортируем класс View, который является базовым классом для всех визуальных элементов в Android
import java.io.IOException // Импортируем класс IOException, который используется для обработки ошибок ввода-вывода
import android.content.Intent // Импортируем класс Intent, который используется для запуска новых активностей или служб и передачи данных между компонентами
import okhttp3.MediaType.Companion.toMediaTypeOrNull // Импортируем метод расширения toMediaTypeOrNull, который преобразует строку в объект MediaType или возвращает null, если преобразование не удалось
import okhttp3.RequestBody.Companion.toRequestBody // Импортируем метод расширения toRequestBody, который преобразует строку или массив байтов в объект RequestBody
import android.widget.Toast
import android.graphics.Color
import android.util.Log

var jsonString = "" // Глобальная переменная для хранения JSON строки
var count = 0 // установленная температура
var memHysteresis = 0
var menPressure = 0
var counPower = 0

class MainActivity : AppCompatActivity() {

    // Инициализация переменных для значений JSON объекта
    private var countHys: Double = 0.0
    private var countPres: Double = 0.0

    // Создаем клиент OkHttp
    private val client = OkHttpClient()

    companion object {
        // Статические URL для вашего ESP8266
        const val espUrlExo = "http://192.168.4.10/"
        const val espUrlOnOff = "http://192.168.4.10/onoff"
        const val espUrlRes = "http://192.168.4.10/res"
        const val espUrlCount = "http://192.168.4.10/count"
        const val espUrlHysteresis = "http://192.168.4.10/hysteresis"

        // Объявляем и инициализируем переменные для хранения значений параметров
        private var PR: Double = 0.0
        private var COUNT: Double = 0.0
        private var onoff: Boolean = false
        private var HYSTERESIS: Double = 0.0
        private var RelayOnOff: Boolean = false
        private var DS18B20_sensor_status: Boolean = false
        private var DS18B20_temperature: Double = 0.0

        private var ONOFF: Boolean = false
    }

    private lateinit var textViewVarHys: TextView // Объявление переменной для TextView
    private lateinit var textViewVarPres: TextView // Объявление переменной для TextView
    private lateinit var textViewHysPomp: TextView // Объявление переменной для TextView
    private lateinit var textViewPressPomp: TextView // Объявление переменной для TextView

    private lateinit var textViewPresure: TextView // Объявление переменной для TextView
    private lateinit var textViewCount: TextView // Объявление переменной для TextView
    private lateinit var textViewOnOff: TextView // Объявление переменной для TextView
    private lateinit var textViewHysteresis: TextView // Объявление переменной для TextView
    private lateinit var textViewTemperature: TextView // Объявление переменной для TextView
    private lateinit var textViewSensorStatus: TextView // Объявление переменной для TextView
    private lateinit var textViewRalyOnOff: TextView // Объявление переменной для TextView



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
        setContentView(R.layout.activity_main)
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
        textViewCount = findViewById(R.id.textViewCount)
        textViewOnOff = findViewById(R.id.textViewOnOff)
        textViewHysteresis = findViewById(R.id.textViewHysteresis)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        textViewSensorStatus = findViewById(R.id.textViewSensorStatus)
        textViewRalyOnOff = findViewById(R.id.textViewRalyOnOff)



        findViewById<ImageButton>(R.id.imageButtonSave).setOnClickListener {
            print("imageButtonSave ")
            fetchDataFromServer()
        }

        // Обновите setOnClickListener для кнопки
        findViewById<ImageButton>(R.id.imageButtonPowerOnOff).setOnClickListener {
            ONOFF = !ONOFF // Меняем значение на противоположное
            // Установка значения переменной в TextView
            textViewVarHys.text = if (ONOFF) "1" else "0"
            println("ONOFF: $ONOFF")
            ONOFF(ONOFF) // Отправляем новое значение на сервер
        }

        findViewById<ImageButton>(R.id.imageButtonHysteresisDown).setOnClickListener {
            countHys=countHys-0.01
            if (countHys <= 0.0) countHys = 0.0
            // Установка значения переменной в TextView
            textViewVarHys.text = "$countHys"
            print("countHys ")
            println(countHys)
            countHys()
        }

        findViewById<ImageButton>(R.id.imageButtonHysteresisUp).setOnClickListener {
            countHys=countHys+0.01
            // Установка значения переменной в TextView
            textViewVarHys.text = "$countHys"
            print("countHys ")
            println(countHys)
            countHys()
        }

        findViewById<ImageButton>(R.id.imageButtonPressureDown).setOnClickListener {

            countPres=countPres-0.01
            if (countPres <= 0.0) countPres = 0.0
            // Установка значения переменной в TextView
            textViewVarPres.text = "$countPres"
            print("countPres ")
            println(countPres)
            countPres()
        }

        findViewById<ImageButton>(R.id.imageButtonPressureUp).setOnClickListener {
            countPres=countPres+0.01
            // Установка значения переменной в TextView
            textViewVarPres.text = "$countPres"
            print("countPres ")
            println(countPres)
            countPres()

        }
    }

    /************************************/
    // Функция для запроса данных с сервера
    private fun fetchDataFromServer() {
        // Выполняем сетевой запрос в корутине
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Выполняем запрос к серверу
                val response = get("http://192.168.4.10/res")

                // Проверяем успешность ответа
                if (response.isSuccessful) {
                    // Получаем тело ответа в виде строки
                    val responseBody = response.body?.string()

                    // Парсим JSON и обновляем UI в основном потоке
                    responseBody?.let { json ->
                        runOnUiThread {
                            parseJsonAndAssignValues(json)
                            // Добавляем сообщение или уведомление об успешном получении ответа
                            //showToast("Данные успешно получены с сервера")
                        }
                    }
                } else {
                    // Показываем сообщение об ошибке, если сервер вернул неудачный ответ
                    runOnUiThread {
                        println("Server returned error: ${response.code}")
                        showToast("Server returned error: ${response.code}")
                    }
                }
            } catch (e: IOException) {
                // Показываем сообщение об ошибке в случае исключения
                runOnUiThread {
                    showToast("IOException occurred: ${e.message}")
                    println("IOException occurred: ${e.message}")
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
            textViewCount.text = "$COUNT"
            textViewOnOff.text = "$onoff"
            textViewHysteresis.text = "$HYSTERESIS"
            textViewRalyOnOff.text = "$RelayOnOff"
            textViewSensorStatus.text = "$DS18B20_sensor_status"
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
    private fun countPres() {
        // Определяем функцию COUNT
        CoroutineScope(Dispatchers.IO).launch {
            // Запускаем корутину в диспетчере IO
            try {
                // Оборачиваем код в блок try для обработки возможных исключений
                val url = "http://192.168.4.10/count?count=$countPres"
                // Формируем URL для отправки запроса на сервер с параметром count
                val response = get(url)
                // Выполняем HTTP-запрос по указанному URL и получаем ответ
                if (response.isSuccessful) {
                    // Проверяем, успешен ли ответ
                    println("Count sent successfully: $countPres")
                    // Выводим в консоль сообщение о успешной отправке count
                } else {
                    // Если ответ не успешен, выводим код и сообщение об ошибке в консоль
                    println("Failed to send count: ${response.code} ${response.message}")
                }
            } catch (e: IOException) {
                // Обрабатываем исключения IOException
                e.printStackTrace()
                println("IOException occurred: ${e.message}")
            }
        }
    }

    // Функция для отправки текущего значения счетчика на сервер
    private fun countHys() {
        // Определяем функцию COUNT
        CoroutineScope(Dispatchers.IO).launch {
            // Запускаем корутину в диспетчере IO
            try {
                // Оборачиваем код в блок try для обработки возможных исключений
                val url = "http://192.168.4.10/hysteresis?hysteresis=$countHys"
                // Формируем URL для отправки запроса на сервер с параметром count
                val response = get(url)
                // Выполняем HTTP-запрос по указанному URL и получаем ответ
                if (response.isSuccessful) {
                    // Проверяем, успешен ли ответ
                    println("Count sent successfully: $countHys")
                    // Выводим в консоль сообщение о успешной отправке count
                } else {
                    // Если ответ не успешен, выводим код и сообщение об ошибке в консоль
                    println("Failed to send count: ${response.code} ${response.message}")
                }
            } catch (e: IOException) {
                // Обрабатываем исключения IOException
                e.printStackTrace()
                println("IOException occurred: ${e.message}")
            }
        }
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

}