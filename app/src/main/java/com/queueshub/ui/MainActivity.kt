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


// Log tag for MainActivity
val TAG: String = "Main Activity"

// Folder name for storing captured images
val FOLDER_IMAGES: String = "QueuesHub"

// Executor service for camera operations to run on background thread
private lateinit var cameraExecutor: ExecutorService

// ImageCapture use case for taking photos
lateinit var imageCapture: ImageCapture

// Output file options for saving captured images
lateinit var outputFileOptions: ImageCapture.OutputFileOptions


// Dagger Hilt entry point for dependency injection
@AndroidEntryPoint
class MainActivity : ComponentActivity(), GlobalNavigationHandler {
    // Timestamp for tracking back button presses to implement double-tap to exit
    private var backPressed = 0L

    // Main activity view model injected by Hilt
    val viewModel: MainActivityViewModel by viewModels()


    // Override base context to set Arabic locale for the entire app
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextWrapper(newBase.setLocale(Locale("ar"))))
    }

    // Activity lifecycle method - called when activity is first created
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen that shows while app is loading
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Initialize single thread executor for camera operations
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Mutable state to track UI loading state
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        val viewModel =
            // Launch coroutine to observe UI state changes from ViewModel
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.onEach {
                        uiState = it
                    }.collect()
                }
            }

        // Keep splash screen visible while app is in loading state
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }
        // Set up Compose UI content
        setContent {
            // Get Firebase messaging token for push notifications
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                Log.e("token is", it)
            }
            // Request notification permissions from user
            askNotificationPermission()
            // Apply app theme
            BasicStateTheme {
                // Check if user is logged in and navigate accordingly
                if (uiState is MainActivityUiState.Success) if ((uiState as MainActivityUiState.Success).logged) {
                    Log.e("topic  is", "user_${(uiState as MainActivityUiState.Success).userId}")
                    // Subscribe to user-specific Firebase topic for notifications
                    FirebaseMessaging.getInstance()
                        .subscribeToTopic("user_${(uiState as MainActivityUiState.Success).userId}")
                    // Navigate to Orders screen for logged in users
                    myApp(finish = finish, Screen.Orders.route)
                } else {
                    // Navigate to Login screen for non-logged in users
                    myApp(finish = finish, Screen.Login.route)
                }
            }
        }
    }

    // Activity lifecycle method - called when activity becomes visible
    override fun onStart() {
        super.onStart()
        // Register this activity as global navigation handler
        GlobalNavigator.registerHandler(this)
    }

    // Activity lifecycle method - called when activity is no longer visible
    override fun onStop() {
        super.onStop()
        // Unregister global navigation handler to prevent memory leaks
        GlobalNavigator.unregisterHandler()
    }

    // Activity lifecycle method - called when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
    }

    // Activity result launcher for requesting notification permission
    // Handles the result of POST_NOTIFICATIONS permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted - FCM SDK can post notifications
        } else {
            // Permission denied - show Arabic message to user
            Toast.makeText(this, "لن نتمكن من التواصل معك عن طريق الاشعارات", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Request notification permission from user (Android 13+ only)
    private fun askNotificationPermission() {
        // POST_NOTIFICATIONS permission is only required for API level 33 (TIRAMISU) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted - FCM can post notifications
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Show educational UI explaining why notification permission is needed
                // Should provide "OK" and "No thanks" buttons for user choice
                // If "OK" selected, request permission; if "No thanks", continue without notifications
            } else {
                // Request permission directly using the launcher
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Function to handle app exit with double-tap confirmation
    private val finish: () -> Unit = {
        // If back was pressed within last 3 seconds, exit app
        if (backPressed + 3000 > System.currentTimeMillis()) {
            finishAndRemoveTask()
        } else {
            // Show Arabic message asking user to press again to exit
            Toast.makeText(
                this, "اضغط مرة اخري للخروج", Toast.LENGTH_SHORT
            ).show()
        }
        // Record current time for next back press check
        backPressed = System.currentTimeMillis()
    }

    // Implementation of GlobalNavigationHandler interface
    // Handles user logout by clearing data and restarting activity
    override fun logout() {
        viewModel.logout()
        val intent: Intent = intent
        finish()
        startActivity(intent)
    }
}

// Main Compose function that sets up app navigation and UI structure
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
private fun myApp(finish: () -> Unit, startDestination: String) {
    // Remember scaffold state for managing UI components like drawer, snackbar
    val scaffoldState = rememberScaffoldState()
    // Navigation controller for managing app navigation
    val navController = rememberNavController()
    // Current navigation destination state
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // App-level ViewModel injected by Hilt
    val viewModel: AppViewModel = hiltViewModel()
    // Observe user login state
    val userLogged by viewModel.userLogged.collectAsState()
    val isLogged = userLogged.first
    // Get current route or use start destination as fallback
    val route = navBackStackEntry?.destination?.route ?: startDestination
    // Router implementation for navigation logic
    val router: Router = remember { RouterImpl(navController, route) }

    // Handle back button press on main screens (Home/Login) to exit app
    if (route == Screen.Home.route || route == Screen.Login.route) {
        BackHandler {
            finish()
        }
    }
    // Main scaffold container with navigation setup
    Scaffold(modifier = Modifier
        .semantics {
            // Enable test tags as resource IDs for UI testing
            testTagsAsResourceId = true
        }
        .background(Color(0xFFE9E9E9)), // Light gray background
        scaffoldState = scaffoldState,
        content = { innerPadding ->
            // Main navigation container with all app screens
            NavigationContainer(
                navController = navController,
                scaffoldState = scaffoldState,
                paddingValues = innerPadding,
                router = router,
                startDestination = startDestination
            )
        })
}

// Utility function to generate image filename with timestamp
fun makeImageName(currentTime: Long): String {
    return "Image $currentTime.jpg"
}

// Camera preview composable for scanning device serial numbers and IMEI codes
// Scans barcodes and automatically detects IMEI (15 digits) vs serial numbers
@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraBarcodePreview(
    onUpdateSerial: (String) -> Unit, onUpdateIMEI: (String) -> Unit, onCloseScanner: () -> Unit
) {
    // Get current context and lifecycle owner for camera operations
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Camera preview state
    var preview by remember { mutableStateOf<Preview?>(null) }
    // Flashlight toggle state
    var enabledTorch by remember { mutableStateOf<Boolean>(false) }
    // Scanned IMEI and serial number states
    var imei by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    // Counter for scanning progress display
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

// Camera preview composable for scanning SIM card barcodes
// Specifically designed to scan GSM numbers and IMEI codes from SIM cards
@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraSIMBarcodePreview(
    onUpdateGsm: (String) -> Unit, onUpdateIMEI: (String) -> Unit, onCloseScanner: () -> Unit
) {
    // Context and lifecycle for camera operations
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Flashlight toggle state
    var enabledTorch by remember { mutableStateOf<Boolean>(false) }
    // Camera preview state
    var preview by remember { mutableStateOf<Preview?>(null) }
    // Scanned IMEI and GSM number states
    var imei by remember { mutableStateOf("") }
    var gsm by remember { mutableStateOf("") }
    // Counter for tracking scan progress
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

                    Logger.d("barcode: 1 $barcodes")
                    Logger.d("barcode: 2 $barcode")
                    Logger.d("barcode: 3 ${barcode.rawValue}")

                    barcode.rawValue?.let { barcodeValue ->
                        if (barcodeValue.isDigitsOnly() && barcodeValue.length > 12 && imei == "") {
                            imei = barcodeValue
                            count++
                            onUpdateIMEI(imei)
                        } else if ((barcodeValue.length == 11 && barcodeValue.startsWith("01"))
                            || (barcodeValue.length == 12 && barcodeValue.startsWith("201"))
                            && gsm == ""
                        ) {
                            count++
                            gsm = barcodeValue
                            onUpdateGsm(gsm)
                        }

                        if (imei != "" && gsm != "") {
                            onCloseScanner()
                        }


                        Logger.d("barcode: 4 $barcodeValue")
                        Logger.d("barcode: 5 $imei")
                        Logger.d("barcode: 6 $gsm")
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

// Generic camera preview composable for capturing and analyzing images
// Supports different camera types like device photos, car plates, licenses, etc.
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
    // Get context, coroutine scope, and lifecycle owner for camera operations
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Flashlight toggle state
    var enabledTorch by remember { mutableStateOf<Boolean>(false) }
    // PreviewView for camera display with full screen layout
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

// Global variable to hold the last captured image for cleanup
var lastImage: ImageProxy? = null

// Main image capture and processing function
// Handles different camera types and applies appropriate image analysis
@androidx.camera.core.ExperimentalGetImage
fun onClick(
    context: Context,
    type: CameraType,
    onAnalyzerFinished: (String, File) -> Unit,
    onTryAgain: () -> Unit
) {
    // Close previous image to free memory
    lastImage?.close()

    // Capture image using camera executor
    imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            // Store reference to current image
            lastImage = image

            Log.i("DDDD", "onCaptureSuccess: ${image.imageInfo.rotationDegrees}")

            // Convert image to JPEG byte array
            val byteArray = com.queueshub.utils.ImageUtil.jpegImageToJpegByteArray(image)

            // Create temporary file for the captured image
            val file = File(
                context.cacheDir,
                String.format("%s.png", UUID.randomUUID().toString().replace("-", ""))
            )
            file.createNewFile()
            file.writeBytes(byteArray)

            // Process image based on camera type
            when (type) {
                // Basic device photo capture - no text analysis
                CameraType.DEVICE -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }

                // Sensor photo capture - no text analysis
                CameraType.SENSOR -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }

                // Chassis photo capture - no text analysis
                CameraType.SHASIS -> {
                    onAnalyzerFinished(
                        "", file
                    )
                }

                // Device capture with car license analysis
                CameraType.DEVICE_CAPTURE -> {
                    CarLicenseAnalyzer(object : OnAnalyzerFinished {
                        override fun onAnalyzerDone(x: ArrayList<String>) {
                            // Join detected strings and return result
                            onAnalyzerFinished(
                                x.joinToString(","), file
                            )
                        }

                        override fun onTryAgain() {
                            onTryAgain()
                        }
                    }).analyze(image)
                }

                // Car license plate text recognition using Firebase ML
                CameraType.CAR_PLATE -> {
                    try {
                        // Create Firebase Vision image from captured file
                        val firebaseVisionImage =
                            FirebaseVisionImage.fromFilePath(context, file.toUri())

                        // Configure text recognizer for Arabic language
                        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                            .setLanguageHints(listOf("ar")).build()
                        val recognizer =
                            FirebaseVision.getInstance().getCloudTextRecognizer(options)
                        val result = recognizer.processImage(firebaseVisionImage)
                            .addOnSuccessListener { firebaseVisionText ->
                                // Extract and join all detected text blocks
                                onAnalyzerFinished(
                                    firebaseVisionText.textBlocks.joinToString { it.text }, file
                                )
                            }.addOnFailureListener { e ->
                                // Return empty result on failure
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
//                        CarPlateAnalyzer(object : OnPlateAnalyzerFinished {
//                            override fun onAnalyzerDone(x: List<TextBlock>) {
//
//                            }
//                        }).analyze(image)
                }

                // Car license document text recognition
                CameraType.CAR_LICENSE -> {
                    val image: FirebaseVisionImage
                    try {
                        // Create Firebase Vision image from file
                        image = FirebaseVisionImage.fromFilePath(context, file.toUri())

                        // Configure for Arabic text recognition
                        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                            .setLanguageHints(listOf("ar")).build()
                        val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)
                        val result = detector.processImage(image)
                            .addOnSuccessListener { firebaseVisionText ->
                                // Extract and return detected text
                                onAnalyzerFinished(
                                    firebaseVisionText.textBlocks.joinToString { it.text }, file
                                )
                            }.addOnFailureListener { e ->
                                // Handle recognition failure
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
                            Logger.d("serail $serial")
                            Logger.d("gsm $gsm")
                            if (!serial.isNullOrEmpty() || !gsm.isNullOrEmpty())
                                onAnalyzerFinished("$serial,$gsm", file)
                            else {
                                val image: FirebaseVisionImage
                                try {
                                    image = FirebaseVisionImage.fromFilePath(context, file.toUri())

                                    // val options = FirebaseVisionCloudTextRecognizerOptions.Builder().setLanguageHints(listOf("en")).build()
                                    val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                                        .setLanguageHints(listOf("en", "ar")) // "en",
                                        .build()
                                    val detector =
                                        FirebaseVision.getInstance().getCloudTextRecognizer(options)
                                    val result = detector.processImage(image)
                                        .addOnSuccessListener { firebaseVisionText ->

                                            Logger.d("OnSIMAnalyzerFinished 1 $firebaseVisionText")
                                            Logger.d("OnSIMAnalyzerFinished 2 ${firebaseVisionText.textBlocks}")
                                            Logger.d("OnSIMAnalyzerFinished 3 ${firebaseVisionText.textBlocks.joinToString { it.text }}")
                                            Logger.d("OnSIMAnalyzerFinished 4 ${firebaseVisionText.text.lines()}")

//                                            var serial = ""
//                                            firebaseVisionText.textBlocks.forEach {
//                                                val text =
//                                                    it.text.replace("\n", "").replace(" ", "")
//                                                var numbers = text.filter { it.isDigit() }
//                                                if (numbers.length > 11) serial = numbers
//                                            }
//                                            onAnalyzerFinished(
//                                                "$serial,", file
//                                            )
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

// Firebase function to annotate images using Google Cloud Vision API
// Currently unused but available for advanced image analysis
private fun annotateImage(requestJson: String): Task<JsonElement> {
    return Firebase.functions.getHttpsCallable("annotateImage").call(requestJson)
        .continueWith { task ->
            // Process the result regardless of success or failure
            // If failed, an exception will be thrown and propagated
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

// Process Firebase Vision text blocks and extract individual lines
// Returns a list of text strings from all detected text blocks
private fun processTextBlock(text: FirebaseVisionText): List<String> {
    val list: MutableList<String> = mutableListOf()
    val blocks: List<FirebaseVisionText.TextBlock> = text.textBlocks
    if (blocks.isEmpty()) {
        return list
    }
    // Iterate through all text blocks and extract line text
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
// Rotate bitmap image based on EXIF orientation data
// Fixes orientation issues when images are captured in different orientations
@Throws(IOException::class)
fun rotateImage(context: Context, bitmap: Bitmap, image: (Bitmap) -> Unit) {
    var rotate = 0
    // Read EXIF data to determine original orientation
    val exif = ExifInterface(bitmap.toFile(context).path)
    val orientation: Int = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    // Determine rotation angle based on EXIF orientation
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
    }
    Log.i("TAGTAGTAG", "rotateImage: $rotate")

    // Apply rotation transformation to bitmap
    val matrix = Matrix()
    matrix.postRotate(rotate.toFloat())
    val newBitmap = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width,
        bitmap.height, matrix, true
    )
    // Return rotated bitmap via callback
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


