package com.queueshub.ui

import android.Manifest
import android.R.attr.path
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import android.media.ExifInterface

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.Task
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.queueshub.data.api.interceptors.GlobalNavigationHandler
import com.queueshub.data.api.interceptors.GlobalNavigator
import com.queueshub.ui.navigation.NavigationContainer
import com.queueshub.ui.navigation.Router
import com.queueshub.ui.navigation.RouterImpl
import com.queueshub.ui.navigation.Screen
import com.queueshub.ui.theme.BasicStateTheme
import com.queueshub.utils.*
import com.queueshub.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


val TAG: String = "Main Activity"
val FOLDER_IMAGES: String = "QueuesHub"
private lateinit var cameraExecutor: ExecutorService
lateinit var imageCapture: ImageCapture
lateinit var outputFileOptions: ImageCapture.OutputFileOptions


@AndroidEntryPoint
class MainActivity : ComponentActivity(), GlobalNavigationHandler {
    private var backPressed = 0L
    val viewModel: MainActivityViewModel by viewModels()


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextWrapper(newBase.setLocale(Locale("ar"))))
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        val viewModel =
            // Update the uiState
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.onEach {
                        uiState = it
                    }.collect()
                }
            }

        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }
        setContent {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                Log.e("token is", it)
            }
            askNotificationPermission()
            BasicStateTheme {
                // A surface container using the 'background' color from the theme
                if (uiState is MainActivityUiState.Success) if ((uiState as MainActivityUiState.Success).logged) {
                    Log.e("topic  is", "user_${(uiState as MainActivityUiState.Success).userId}")

                    FirebaseMessaging.getInstance()
                        .subscribeToTopic("user_${(uiState as MainActivityUiState.Success).userId}")
                    myApp(finish = finish, Screen.Orders.route)
                } else {
                    myApp(finish = finish, Screen.Login.route)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        GlobalNavigator.registerHandler(this)
    }

    override fun onStop() {
        super.onStop()
        GlobalNavigator.unregisterHandler()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(this, "لن نتمكن من التواصل معك عن طريق الاشعارات", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val finish: () -> Unit = {
        if (backPressed + 3000 > System.currentTimeMillis()) {
            finishAndRemoveTask()
        } else {
            Toast.makeText(
                this, "اضغط مرة اخري للخروج", Toast.LENGTH_SHORT
            ).show()
        }
        backPressed = System.currentTimeMillis()
    }

    override fun logout() {
        viewModel.logout()
        val intent: Intent = intent
        finish()
        startActivity(intent)
    }
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
private fun myApp(finish: () -> Unit, startDestination: String) {
    val scaffoldState = rememberScaffoldState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val viewModel: AppViewModel = hiltViewModel()
    val userLogged by viewModel.userLogged.collectAsState()
    val isLogged = userLogged.first
    val route = navBackStackEntry?.destination?.route ?: startDestination
    val router: Router = remember { RouterImpl(navController, route) }

    if (route == Screen.Home.route || route == Screen.Login.route) {
        BackHandler {
            finish()
        }
    }
    Scaffold(modifier = Modifier
        .semantics {
            testTagsAsResourceId = true
        }
        .background(Color(0xFFE9E9E9)),
        scaffoldState = scaffoldState,
        content = { innerPadding ->
            NavigationContainer(
                navController = navController,
                scaffoldState = scaffoldState,
                paddingValues = innerPadding,
                router = router,
                startDestination = startDestination
            )
        })
}

fun makeImageName(currentTime: Long): String {
    return "Image $currentTime.jpg"
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraBarcodePreview(
    onUpdateSerial: (String) -> Unit, onUpdateIMEI: (String) -> Unit, onCloseScanner: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }
    var enabledTorch by remember { mutableStateOf<Boolean>(false) }
    var imei by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var count = 0

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { AndroidViewContext ->
            PreviewView(AndroidViewContext).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

            }
        }, modifier = Modifier.fillMaxSize(), update = { previewView ->
            val cameraSelector: CameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val barcodeAnalyser = BarcodeAnalyzer { barcodes ->
                    val barcode = barcodes.first()
                    barcode.rawValue?.let { barcodeValue ->
                        if (barcodeValue.isDigitsOnly() && barcodeValue.length == 15) {
                            imei = barcodeValue
                            count++
                            onUpdateIMEI(imei)
                        } else {
                            count++
                            serial = barcodeValue
                            onUpdateSerial(serial)
                        }

                        if (imei != "" && serial != "") onCloseScanner()
                    }

                }
                val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()

                    .setTargetResolution(Size(previewView.width, previewView.height))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                        it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                    }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalysis
                    )

                    // Getting the CameraControl instance from the camera
                    val cameraControl = camera.cameraControl
                    cameraControl.enableTorch(enabledTorch)
                    previewView.setOnTouchListener(View.OnTouchListener { view: View, motionEvent: MotionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> return@OnTouchListener true
                            MotionEvent.ACTION_UP -> {
                                // Get the MeteringPointFactory from PreviewView
                                val factory = previewView.getMeteringPointFactory()

                                // Create a MeteringPoint from the tap coordinates
                                val point = factory.createPoint(motionEvent.x, motionEvent.y)

                                // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                                val action = FocusMeteringAction.Builder(point).build()

                                // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                                // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                                cameraControl.startFocusAndMetering(action)

                                return@OnTouchListener true
                            }

                            else -> return@OnTouchListener false
                        }
                    })
                } catch (e: Exception) {
                    Log.d("TAG", "CameraPreview: ${e.localizedMessage}")
                }
            }, ContextCompat.getMainExecutor(context))
        })

        IconButton(modifier = Modifier.padding(bottom = 20.dp), onClick = {
            onCloseScanner()

        }, content = {
            Icon(
                imageVector = Icons.Sharp.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .padding(1.dp)
                    .border(1.dp, Color.White, CircleShape)
            )
        })

        Icon(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clickable { enabledTorch = !enabledTorch },
            imageVector = if (enabledTorch) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
            tint = Color.White,
            contentDescription = ""
        )
        if (imei.length > 1 || serial.length > 1) {
            count = 1
        }
        Text(
            text = "$count/2",
            modifier = Modifier.align(Alignment.TopCenter),
            fontSize = 35.sp,
            color = Color.White
        )
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraSIMBarcodePreview(
    onUpdateGsm: (String) -> Unit, onUpdateIMEI: (String) -> Unit, onCloseScanner: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabledTorch by remember { mutableStateOf<Boolean>(false) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imei by remember { mutableStateOf("") }
    var gsm by remember { mutableStateOf("") }
    var count by remember { mutableStateOf(0) }

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { AndroidViewContext ->
            PreviewView(AndroidViewContext).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

            }
        }, modifier = Modifier.fillMaxSize(), update = { previewView ->
            val cameraSelector: CameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val barcodeAnalyser = BarcodeAnalyzer { barcodes ->
                    val barcode = barcodes.first()
                    barcode.rawValue?.let { barcodeValue ->
                        if (barcodeValue.isDigitsOnly() && barcodeValue.length > 12 && imei == "") {
                            imei = barcodeValue
                            count++
                            onUpdateIMEI(imei)
                        } else if ((barcodeValue.length == 11 && barcodeValue.startsWith("01")) || (barcodeValue.length == 12 && barcodeValue.startsWith(
                                "201"
                            )) && gsm == ""
                        ) {
                            count++
                            gsm = barcodeValue
                            onUpdateGsm(gsm)
                        }

                        if (imei != "" && gsm != "") {
                            onCloseScanner()
                        }
                    }

                }
                val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(previewView.width, previewView.height))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                        it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                    }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalysis
                    )

                    // Getting the CameraControl instance from the camera
                    val cameraControl = camera.cameraControl
                    cameraControl.enableTorch(enabledTorch)
                    previewView.setOnTouchListener(View.OnTouchListener { view: View, motionEvent: MotionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> return@OnTouchListener true
                            MotionEvent.ACTION_UP -> {
                                // Get the MeteringPointFactory from PreviewView
                                val factory = previewView.getMeteringPointFactory()

                                // Create a MeteringPoint from the tap coordinates
                                val point = factory.createPoint(motionEvent.x, motionEvent.y)

                                // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                                val action = FocusMeteringAction.Builder(point).build()

                                // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                                // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                                cameraControl.startFocusAndMetering(action)

                                return@OnTouchListener true
                            }

                            else -> return@OnTouchListener false
                        }
                    })
                } catch (e: Exception) {
                    Log.d("TAG", "CameraPreview: ${e.localizedMessage}")
                }
            }, ContextCompat.getMainExecutor(context))
        })

        IconButton(modifier = Modifier.padding(bottom = 20.dp), onClick = {
            onCloseScanner()

        }, content = {
            Icon(
                imageVector = Icons.Sharp.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .padding(1.dp)
                    .border(1.dp, Color.White, CircleShape)
            )
        })

        Icon(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clickable { enabledTorch = !enabledTorch },
            imageVector = if (enabledTorch) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
            tint = Color.White,
            contentDescription = ""
        )
        if (imei.length > 1 || gsm.length > 1) {
            count = 1
        }
        Text(
            text = "$count/2",
            modifier = Modifier.align(Alignment.TopCenter),
            fontSize = 35.sp,
            color = Color.White
        )
    }
}

@ExperimentalGetImage
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraType: CameraType,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onImageCaptured: () -> Unit,
    onTryAgain: () -> Unit = {},
    onAnalyzerFinished: (String, File) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var enabledTorch by remember { mutableStateOf<Boolean>(false) }
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        }
    }

    LaunchedEffect(enabledTorch) {

        imageCapture = ImageCapture.Builder().setTargetResolution(Size(270, 480)).build()
        val mDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        val file = File(context.cacheDir, mDateFormat.format(Date()) + ".png")
        outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, imageCapture
        )
        camera.cameraControl.enableTorch(enabledTorch)
    }
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView(modifier = modifier, factory = { previewView })

        Icon(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clickable { enabledTorch = !enabledTorch },
            imageVector = if (enabledTorch) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
            tint = Color.White,
            contentDescription = ""
        )
        IconButton(modifier = Modifier.padding(bottom = 20.dp), onClick = {
            onImageCaptured()
            onClick(context, cameraType, onAnalyzerFinished) {
                onTryAgain()
            }
        }, content = {
            Icon(
                imageVector = Icons.Sharp.Lens,
                contentDescription = "Take picture",
                tint = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .padding(1.dp)
                    .border(1.dp, Color.White, CircleShape)
            )
        })
    }
}

var lastImage: ImageProxy? = null

@androidx.camera.core.ExperimentalGetImage
fun onClick(
    context: Context,
    type: CameraType,
    onAnalyzerFinished: (String, File) -> Unit,
    onTryAgain: () -> Unit
) {
    lastImage?.close()

    imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            lastImage = image

            Log.i("DDDD", "onCaptureSuccess: ${image.imageInfo.rotationDegrees}")

            val byteArray = com.queueshub.utils.ImageUtil.jpegImageToJpegByteArray(image)

            val file = File(
                context.cacheDir,
                String.format("%s.png", UUID.randomUUID().toString().replace("-", ""))
            )
            file.createNewFile()
            file.writeBytes(byteArray)

            when (type) {
                CameraType.DEVICE -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }

                CameraType.SENSOR -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }


                CameraType.SHASIS -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }

                CameraType.DEVICE_CAPTURE -> {
                    CarLicenseAnalyzer(object : OnAnalyzerFinished {
                        override fun onAnalyzerDone(x: ArrayList<String>) {
                            onAnalyzerFinished(
                                x.joinToString(","), file
                            )
                        }

                        override fun onTryAgain() {
                            onTryAgain()
                        }
                    }).analyze(image)
                }

                CameraType.CAR_PLATE -> {
                    try {
                        val firebaseVisionImage =
                            FirebaseVisionImage.fromFilePath(context, file.toUri())

                        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                            .setLanguageHints(listOf("ar")).build()
                        val recognizer =
                            FirebaseVision.getInstance().getCloudTextRecognizer(options)
                        val result = recognizer.processImage(firebaseVisionImage)
                            .addOnSuccessListener { firebaseVisionText ->
                                onAnalyzerFinished(
                                    firebaseVisionText.textBlocks.joinToString { it.text }, file
                                )
//                                val x = firebaseVisionText.text.lines().toString().replace(
//                                    """^[\u0621-\u064A\u0660-\u0669 ]+${'$'}""".toRegex(),
//                                    ""
//                                )

                            }.addOnFailureListener { e ->
                                onAnalyzerFinished(
                                    "", file
                                )
//                                "17x35"
                                // Task failed with an exception
                                // ...
                            }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        onAnalyzerFinished(
                            "", file
                        )
                    }
//                        CarPlateAnalyzer(object : OnPlateAnalyzerFinished {
//                            override fun onAnalyzerDone(x: List<TextBlock>) {
//
//                            }
//                        }).analyze(image)
                }

                CameraType.CAR_LICENSE -> {

                    val image: FirebaseVisionImage
                    try {
                        image = FirebaseVisionImage.fromFilePath(context, file.toUri())

                        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                            .setLanguageHints(listOf("ar")).build()
                        val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)
                        val result = detector.processImage(image)
                            .addOnSuccessListener { firebaseVisionText ->
                                onAnalyzerFinished(
                                    firebaseVisionText.textBlocks.joinToString { it.text }, file
                                )
                            }.addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
                                onAnalyzerFinished(
                                    "", file
                                )
                            }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        onAnalyzerFinished(
                            "", file
                        )
                    }
                }

                CameraType.CAR_LICENSE2 -> {

                    val image: FirebaseVisionImage
                    try {
                        image = FirebaseVisionImage.fromFilePath(context, file.toUri())

                        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                            .setLanguageHints(listOf("ar")).build()
                        val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)
                        val result = detector.processImage(image)
                            .addOnSuccessListener { firebaseVisionText ->
                                onAnalyzerFinished(
                                    firebaseVisionText.textBlocks.joinToString { it.text }, file
                                )
                                for (item in firebaseVisionText.text.lines()) {
                                    Log.d("MyDebugData2", "$item")
                                }
                            }.addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
                                onAnalyzerFinished(
                                    "", file
                                )
                                Log.d("MyDebugData2", "fail")
                            }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        onAnalyzerFinished(
                            "", file
                        )
                        Log.d("MyDebugData2", "catch")
                    }
                }

                CameraType.SIM -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }

                CameraType.SIM_CAPTURE -> {
                    SIMAnalyzer(object : OnSIMAnalyzerFinished {
                        override fun onAnalyzerDone(serial: String?, gsm: String?) {
                            if (!serial.isNullOrEmpty() || !gsm.isNullOrEmpty()) onAnalyzerFinished(
                                "$serial,$gsm", file
                            )
                            else {
                                val image: FirebaseVisionImage
                                try {
                                    image = FirebaseVisionImage.fromFilePath(context, file.toUri())

                                    val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                                        .setLanguageHints(listOf("en")).build()
                                    val detector =
                                        FirebaseVision.getInstance().getCloudTextRecognizer(options)
                                    val result = detector.processImage(image)
                                        .addOnSuccessListener { firebaseVisionText ->
                                            var serial = ""
                                            firebaseVisionText.textBlocks.forEach {
                                                val text =
                                                    it.text.replace("\n", "").replace(" ", "")
                                                var numbers = text.filter { it.isDigit() }
                                                if (numbers.length > 11) serial = numbers
                                            }
                                            onAnalyzerFinished(
                                                "$serial,", file
                                            )
                                        }.addOnFailureListener { e ->
                                            // Task failed with an exception
                                            // ...
                                            onAnalyzerFinished(
                                                "", file
                                            )
                                        }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    onAnalyzerFinished(
                                        "", file
                                    )
                                }
                            }
                        }

                        override fun onTryAgain() {
                            onTryAgain()
                        }
                    }).analyze(image)
                }

                CameraType.MAINTENANCE -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }

                else -> {}
            }
            Logger.d("took image")
//
            super.onCaptureSuccess(image)
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
        }
    })

}

private fun annotateImage(requestJson: String): Task<JsonElement> {
    return Firebase.functions.getHttpsCallable("annotateImage").call(requestJson)
        .continueWith { task ->
            // This continuation runs on either success or failure, but if the task
            // has failed then result will throw an Exception which will be
            // propagated down.
            val result = task.result?.data
            JsonParser.parseString(Gson().toJson(result))
        }
}


//private fun processTextBlock(text: FirebaseVisionText) {
//    val blocks: List<FirebaseVisionText.TextBlock> = text.getTextBlocks()
//    if (blocks.isEmpty()) {
//        return
//    }
//    for (i in blocks.indices) {
//        Log.i("TAGTAGTAG", "processTextBlock ss: ${blocks[i].text}")
//        val lines = blocks[i].lines
//        for (j in lines.indices) {
//            val elements = lines[j].elements
//            for (k in elements.indices) {
//                Log.i("TAGTAGTAG", "processTextBlock: ${elements[k].text}")
//            }
//        }
//    }
//}

private fun processTextBlock(text: FirebaseVisionText): List<String> {
    val list:MutableList<String> = mutableListOf()
    val blocks: List<FirebaseVisionText.TextBlock> = text.textBlocks
    if (blocks.isEmpty()) {
        return list
    }
    for (i in blocks.indices) {
        val lines = blocks[i].lines
        for (j in lines.indices) {
         list.add(lines[j].text)
        }
    }
    return list
}

//private fun processTextBlock(text: FirebaseVisionDocumentText?) {
//    // Task completed successfully
//    if (text == null) {
//        return
//    }
//    val blocks = text.blocks
//    for (i in blocks.indices) {
//        val paragraphs = blocks[i].paragraphs
//        for (j in paragraphs.indices) {
//            val words = paragraphs[j].words
//            for (l in words.indices) {
//                Log.i("TAGTAGTAG", "processTextBlock: ${words[l].text}")
//            }
//        }
//    }
//}
//
//
@Throws(IOException::class)
fun rotateImage(context: Context, bitmap: Bitmap, image: (Bitmap) -> Unit) {
    var rotate = 0
    val exif = ExifInterface(bitmap.toFile(context).path)
    val orientation: Int = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
    }
    Log.i("TAGTAGTAG", "rotateImage: $rotate")

    val matrix = Matrix()
    matrix.postRotate(rotate.toFloat())
    val newBitmap = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width,
        bitmap.height, matrix, true
    )
    image(newBitmap)
}
//
//@Throws(IOException::class)
//fun rotateImage(context: Context,bitmap: Bitmap):Bitmap {
//    var rotate = 0
//    val exif = ExifInterface(bitmap.toFile(context).path)
//    val orientation: Int = exif.getAttributeInt(
//        ExifInterface.TAG_ORIENTATION,
//        ExifInterface.ORIENTATION_NORMAL
//    )
//
//    when (orientation) {
//        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
//        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
//        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
//    }
//    Log.i("TAGTAGTAG", "rotateImage: $rotate")
//
//    val matrix = Matrix()
//    matrix.postRotate(rotate.toFloat())
//    return  Bitmap.createBitmap(
//        bitmap, 0, 0, bitmap.width,
//        bitmap.height, matrix, true
//    )
//}


