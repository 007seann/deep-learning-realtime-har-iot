package com.specknet.pdiotapp.live;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.chaquo.python.PyObject
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.activity.ActivityData
import com.specknet.pdiotapp.preprocessing.DataQueue
import com.specknet.pdiotapp.preprocessing.RawAccelGyroData
import com.specknet.pdiotapp.preprocessing.TestResponseData
import com.specknet.pdiotapp.user.UserData
import com.specknet.pdiotapp.user.UserSession
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.RandomDataGenerator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_model_prediction.activity_pred
import kotlinx.android.synthetic.main.activity_model_prediction.sub_activity_pred
import org.tensorflow.lite.Delegate
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.time.LocalDateTime

class LivePredictingActivity : AppCompatActivity() {


    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    val filter = IntentFilter()

    var time = 0f

    lateinit var allRespeckData: LineData


    lateinit var respeckChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)

    private lateinit var task1Model: Interpreter
    private lateinit var task2Model: Interpreter
    private lateinit var task3Model: Interpreter
    private lateinit var task3GyroModel: Interpreter

    private lateinit var respeckXDataQueue: DataQueue<Float>
    private lateinit var respeckYDataQueue: DataQueue<Float>
    private lateinit var respeckZDataQueue: DataQueue<Float>

    private lateinit var respeckRawAccelGyroDataQueue: DataQueue<RawAccelGyroData>

    private var usesGyro = true

    private lateinit var module: PyObject

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var toggleGraphButton: Button

    private var isGraphOn = false
    private var isModelRunning = false

    private var prevActivity = ""

    private val TAG = "LivePredictingActivity"

    private val newModelHandler = Handler(Looper.getMainLooper())
    private val newUpdatePredRunnable = object: Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            if (respeckXDataQueue.isFull()) {
                val input = preprocess()
                val rawInputWithGyro = if (usesGyro) {
                    preprocessWithGyro()
                } else {
                    emptyArray()
                }
                //val currActivity = runEnsembleModels(input, rawInputWithGyro)
                val currActivity = runNewModels(input, rawInputWithGyro)
                //val currActivity = runTask1Model(input)
                //val currActivity = runTask2Model(input)
                //val currActivity = runTask3Model(input)
                if (prevActivity != currActivity) {
                    val splitActivities = currActivity.split(", ")
                    activity_pred.text = splitActivities[0]
                    sub_activity_pred.text = splitActivities[1]
                    prevActivity = currActivity
                    UserSession.addActivity(ActivityData(LocalDateTime.now(), splitActivities[0], splitActivities[1]))
                }
            }
            newModelHandler.postDelayed(this, 1500)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        // Once the activity is destroyed, write the activity data to the database all at once
        writeActivities()
        UserSession.clearActivities()

        if (isModelRunning) newModelHandler.removeCallbacks(newUpdatePredRunnable)

        task1Model.close()
        //task2Model.close()
        if (usesGyro) {
            task3GyroModel.close()
        }
        else {
            task3Model.close()
        }

        unregisterReceiver(respeckLiveUpdateReceiver)
        looperRespeck.quit()

        super.onDestroy()
    }

    private fun loadModelFile(fileName:String): ByteBuffer {
        val fileDescriptor = assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadNewTask1Model() {
        val model = loadModelFile("task1_physical_model.tflite")
        val flexDelegate: Delegate = FlexDelegate()
        val options = Interpreter.Options().addDelegate(flexDelegate)
        this.task1Model = Interpreter(model, options)
//        Log.i(TAG, "Task 1 model loaded. Input Tensor Shape: " + this.task1Model.getInputTensor(0).shape().contentToString() +
//                " Output Tensor Shape: " + this.task1Model.getOutputTensor(0).shape().contentToString())
    }

    private fun loadNewTask2Model() {
        val model = loadModelFile("task2_model_single.tflite")
        val flexDelegate: Delegate = FlexDelegate()
        val options = Interpreter.Options().addDelegate(flexDelegate)
        this.task2Model = Interpreter(model, options)
//        Log.i(TAG, "Task 2 model loaded. Input Tensor Shape: " + this.task2Model.getInputTensor(0).shape().contentToString() +
//                " Output Tensor Shape: " + this.task2Model.getOutputTensor(0).shape().contentToString())
    }

    private fun loadNewTask3Model() {
        val model = loadModelFile("task3_model_weighted.tflite")
        val flexDelegate: Delegate = FlexDelegate()
        val options = Interpreter.Options().addDelegate(flexDelegate)
        this.task3Model = Interpreter(model, options)
//        Log.i(TAG, "Task 3 model loaded. Input Tensor Shape: " + this.task3Model.getInputTensor(0).shape().contentToString() +
//                " Output Tensor Shape: " + this.task3Model.getOutputTensor(0).shape().contentToString())
    }

    private fun loadTask3GyroModel() {
        val model = loadModelFile("task3_model_gyro_subactivity.tflite")
        val flexDelegate: Delegate = FlexDelegate()
        val options = Interpreter.Options().addDelegate(flexDelegate)
        this.task3GyroModel = Interpreter(model, options)
    }

    private fun runEnsembleModels(preprocessedInput: Array<FloatArray>, rawInputWithGyro: Array<FloatArray>): String {
        val task1Pred = runTask1Model(preprocessedInput)
        val activity = task1Pred.split(", ")[0]
        if (!Constants.NON_STATIONARY_ACTIVITIES.contains(activity)) { // Check whether the outcome was a stationary activity
            val yPreprocessed = FloatArray(20)
            val yGyro = FloatArray(20)
            task3Model.run(arrayOf(preprocessedInput), arrayOf(yPreprocessed))
            task3GyroModel.run(arrayOf(rawInputWithGyro), arrayOf(yGyro))
            val y = yPreprocessed.mapIndexed { index, value -> value + yGyro[index] }.toFloatArray()
            return "$activity, ${processTask3Output(arrayOf(y)).split(", ")[1]}"
        }
        return task1Pred
    }

    private fun runNewModels(preprocessedInput: Array<FloatArray>, rawInputWithGyro: Array<FloatArray>): String {
        val task1Pred = runTask1Model(preprocessedInput)
        val activity = task1Pred.split(", ")[0]
        if (!Constants.NON_STATIONARY_ACTIVITIES.contains(activity)) { // Check whether the outcome was a stationary activity
            if (usesGyro) {
                //Log.i(TAG, rawInputWithGyro.contentDeepToString())
                //Log.i(TAG, rawInputWithGyro.size.toString() + " " + rawInputWithGyro[0].size.toString())
                return "$activity, ${runTask3GyroModel(rawInputWithGyro)}"
            }
            return "$activity, ${runTask3Model(preprocessedInput).split(", ")[1]}"
        }
        return task1Pred
    }

    private fun runTask1Model(x: Array<FloatArray>): String {
        val y = arrayOf(FloatArray(12))
        task1Model.run(arrayOf(x), y)
        //Log.i(TAG, "Task 1 " + y.contentDeepToString())
        return processTask1Output(y)
    }

    private fun runTask2Model(x: Array<FloatArray>): String {
        val y = arrayOf(FloatArray(15))
        task2Model.run(arrayOf(x), y)
        return processTask2Output(y)
    }

    private fun runTask3Model(x: Array<FloatArray>): String {
        val y = arrayOf(FloatArray(20))
        task3Model.run(arrayOf(x), y)
        //Log.i(TAG, "Task 3: " + y.contentDeepToString())
        return processTask3Output(y)
    }

    private fun runTask3GyroModel(x: Array<FloatArray>): String {
        val y = arrayOf(FloatArray(4))
        task3GyroModel.run(arrayOf(x), y)
        Log.i(TAG, "Task 3 Gyro: " + y.contentDeepToString())
        return processTask3RespOutput(y)
    }

    private fun processTask1Output(array: Array<FloatArray>): String {
        val argmax: Int = array[0].indexOfFirst { it == array[0].maxOrNull() }
        val activities = Constants.TASK1_MAP[argmax]
        //Log.i(TAG, "Task 1 Model Running. Argmax: $argmax, Model output: $activities")
        return "${activities?.first}, ${activities?.second}"
    }

    private fun processTask2Output(array: Array<FloatArray>): String {
        val argmax: Int = array[0].indexOfFirst { it == array[0].maxOrNull() }
        val activities: Pair<String, String>? = Constants.TASK2_MAP[argmax]
        //Log.i(TAG, "Task 2 Model running. Argmax: $argmax, Model output: $activities")
        return "${activities?.first}, ${activities?.second}"
    }

    private fun processTask3Output(array: Array<FloatArray>): String {
        val argmax: Int = array[0].indexOfFirst { it == array[0].maxOrNull() }
        val activities = Constants.TASK3_MAP[argmax]
        //Log.i(TAG, "Task 3 Model Running. Argmax: $argmax, Model output: $activities")
        return "${activities?.first}, ${activities?.second}"
    }

    private fun processTask3RespOutput(array: Array<FloatArray>): String {
        val argmax: Int = array[0].indexOfFirst { it == array[0].maxOrNull() }
        val subActivity = Constants.RESPIRATORY_ACTIVITIES[argmax]
        return subActivity!!
    }

    private fun preprocess(): Array<FloatArray> {
        val rtn = module.callAttr("preprocess",
            respeckXDataQueue.arrayToString(), respeckYDataQueue.arrayToString(), respeckZDataQueue.arrayToString())
        val rawInput = Gson().fromJson(rtn.toString(), Array<TestResponseData>::class.java)
        return rawInput.map { it.toArray() }.toTypedArray()
    }

    private fun preprocessWithGyro(): Array<FloatArray> {
        return respeckRawAccelGyroDataQueue.map { it.toFloatArray() }.toTypedArray()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun populateMockData() {
        var activities = RandomDataGenerator.generateRandomActivityData(100).toMutableList()
        activities = activities.sortedBy { it.date }.toMutableList()
        UserSession.setActivities(activities)
        //Log.i(TAG, "Mock data generated.")
    }

    // For RealTimeDatabase
    @RequiresApi(Build.VERSION_CODES.O)
    private fun writeActivities() {
        val email = UserSession.getUserEmail()
        val hashedEmail = UserData.hashEmail(email!!)
        val activities = UserSession.getActivities()
        val userReference = UserSession.getRealDB().reference.child(hashedEmail)
        for (activityData: ActivityData in activities) {
            userReference.child(activityData.date.toString().substring(0, 19)).setValue(activityData.activity + ", " + activityData.subActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_prediction)

        startButton = findViewById(R.id.start_pred_button)
        stopButton = findViewById(R.id.stop_pred_button)
        toggleGraphButton = findViewById(R.id.toggle_graph_button)

        module = UserSession.getModule()

        // register a broadcast receiver for respeck status
        filter.addAction(Constants.ACTION_RESPECK_CONNECTED)
        filter.addAction(Constants.ACTION_RESPECK_DISCONNECTED)

        setupCharts()

        // set up Respeck data queue
        respeckXDataQueue = DataQueue()
        respeckYDataQueue = DataQueue()
        respeckZDataQueue = DataQueue()

        if (usesGyro) {
            respeckRawAccelGyroDataQueue = DataQueue()
        }

        setupBroadcastReceivers()

        setupClickListeners()

        loadNewTask1Model()
        //loadNewTask2Model()
        if (usesGyro) {
            loadTask3GyroModel()
            task3GyroModel.allocateTensors()
        }
        else {
            loadNewTask3Model()
            task3Model.allocateTensors()
        }
        task1Model.allocateTensors()
        //task2Model.allocateTensors()
    }

    private fun setupClickListeners() {
        startButton.setOnClickListener {
            //Log.i(TAG, "Start button clicked")
            if (!isModelRunning) {
                activity_pred.text = "Running..."
                sub_activity_pred.text = "Running..."
                newModelHandler.post(newUpdatePredRunnable)
                isModelRunning = true
            }
            startButton.setBackgroundColor(resources.getColor(R.color.highlighted_button))
            stopButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
        }
        stopButton.setOnClickListener {
            //Log.i(TAG, "Stop button clicked")
            if (isModelRunning) {
                newModelHandler.removeCallbacks(newUpdatePredRunnable)
                activity_pred.text = "N/A"
                sub_activity_pred.text = "N/A"
                prevActivity = ""
                isModelRunning = false
            }
            startButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            stopButton.setBackgroundColor(resources.getColor(R.color.highlighted_button))
        }
        toggleGraphButton.setOnClickListener {
            //Log.i(TAG, "Toggle graph button clicked")
            if (isGraphOn) {
                findViewById<LinearLayout>(R.id.respeck_graphs).visibility = LinearLayout.INVISIBLE
                findViewById<LineChart>(R.id.respeck_chart).visibility = LineChart.INVISIBLE
                isGraphOn = false
                toggleGraphButton.setBackgroundColor(resources.getColor(R.color.not_highlighted_button))
            }
            else {
                findViewById<LinearLayout>(R.id.respeck_graphs).visibility = LinearLayout.VISIBLE
                findViewById<LineChart>(R.id.respeck_chart).visibility = LineChart.VISIBLE
                isGraphOn = true
                toggleGraphButton.setBackgroundColor(resources.getColor(R.color.highlighted_button))
            }
        }
    }

    private fun setupRespeckBroadcastReceiver() {
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                //Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    //Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    respeckXDataQueue.addPop(x)
                    respeckYDataQueue.addPop(y)
                    respeckZDataQueue.addPop(z)
                    updateGraph("respeck", x, y, z)

                    if (usesGyro) { // Use Respeck gyro data
                        respeckRawAccelGyroDataQueue.addPop(RawAccelGyroData(x, y, z, liveData.gyro.x, liveData.gyro.y, liveData.gyro.z))
                    }

                    time += 1 // Every 0.04s
                }
            }
        }
        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)
    }

    private fun setupBroadcastReceivers() {
        setupRespeckBroadcastReceiver()
    }

    private fun setupCharts() {
        respeckChart = findViewById(R.id.respeck_chart)

        // Respeck

        time = 0f
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        dataSet_res_accel_x.setColor(
            ContextCompat.getColor(
                this,
                R.color.red
            )
        )
        dataSet_res_accel_y.setColor(
            ContextCompat.getColor(
                this,
                R.color.green
            )
        )
        dataSet_res_accel_z.setColor(
            ContextCompat.getColor(
                this,
                R.color.blue
            )
        )

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()
    }

    fun updateGraph(graph: String, x: Float, y: Float, z: Float) {
        // take the first element from the queue
        // and update the graph with it
        if (graph == "respeck") {
            dataSet_res_accel_x.addEntry(Entry(time, x))
            dataSet_res_accel_y.addEntry(Entry(time, y))
            dataSet_res_accel_z.addEntry(Entry(time, z))

            runOnUiThread {
                allRespeckData.notifyDataChanged()
                respeckChart.notifyDataSetChanged()
                respeckChart.invalidate()
                respeckChart.setVisibleXRangeMaximum(150f)
                respeckChart.moveViewToX(respeckChart.lowestVisibleX + 40)
            }
        }
    }
}
