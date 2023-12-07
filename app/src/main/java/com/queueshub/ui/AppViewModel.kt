package com.queueshub.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queueshub.domain.model.Maintenance
import com.queueshub.domain.model.Order
import com.queueshub.domain.model.Sensor
import com.queueshub.domain.model.User
import com.queueshub.domain.usecases.CreateOrder
import com.queueshub.domain.usecases.GetOrderStarted
import com.queueshub.domain.usecases.GetUserLogged
import com.queueshub.ui.models.Event
import com.queueshub.ui.viewStates.CreateOrderViewState
import com.queueshub.utils.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    userLogged: GetUserLogged,
    orderStarted: GetOrderStarted,
    val createOrder: CreateOrder
) : ViewModel() {
    var goingBack: Boolean= false
    private lateinit var finalOrderType: java.util.ArrayList<String>
    var selectedOrder: Order? = null
    var showedInfo: Boolean = false
    var orderType: OrderType = OrderType.UNDEFINED

    var plateInfoAuto = false
    var licenseAuto = false
    var onUpdate = mutableStateOf(0)

    fun updateUI() {
        onUpdate.value = (0..1_000_000).random()
    }


    var selectedSensors = mutableListOf<Sensor>()
    var selectedMaintenances = mutableListOf<Maintenance>()
    val listOfColors = listOf(
        "أبيض",
        "ابيض",
        "احمر",
        "أحمر",
        "ازرق",
        "أزرق",
        "أصفر",
        "اصفر",
        "ذهبي",
        "ذهبى",
        "رمادي",
        "رمادى",
        "رصاصي",
        "رصاصى",
        "فضي",
        "فضى",
        "نبيتي",
        "نبيتى",
        "أسود",
        "اسود",
        "برتقالي",
        "برتقالى",
        "بني",
        "بنى",
        "اخضر",
        "أخضر",
        "كحلي",
        "كحلى",
        "نحاسي",
        "نحاسى",
        "برونزي",
        "برونزى",
    )
    val listOfCarModels = listOf(
        "شيفروليه",
        "شيفرولية",
        "كيا",
        "بيجو",
        "هيونداي",
        "هيونداى",
        "سكودا",
        "فيات",
        "دى اس",
        "دي اس",
        "اوبل",
        "أوبل",
        "بى واى دى",
        "بي واي دي",
        "رينو",
        "تويوتا",
        "هوندا",
        "لاند روفر",
        "جيب",
        "مازيراتي",
        "مازيراتى",
        "جيتور",
        "جيلي",
        "جيلى",
        "شيرى",
        "شيري",
        "لادا",
        "نصر",
        "بيستون",
        "ميتسوبيشي",
        "ميتسوبيشى",
        "جاك",
        "سوزوكى",
        "سوزوكي",
        "استون مارتن",
        "أستون مارتن",
        "بروتون",
        "كاي ي",
        "مازدا",
        "مينى",
        "ميني",
        "فولفو",
        "بايك",
        "فورد",
        "سيات",
        "كوبرا",
        "نيسان",
        "ام جى",
        "ام جي",
        "فولكس فاغن",
        "فولكس واجن",
        "فولكس فاجن",
        "دونج فنج",
        "بريليانس",
        "الفاروميو",
        "فورثينج",
        "بورش",
        "دفسك",
        "سانج يونج",
        "اودي",
        "أودي",
        "أودى",
        "اودى",
        "شانجان",
        "سوبارو",
        "شانجى",
        "بي ام دبليو",
        "بي أم دبليو",
        "بى ام دبليو",
        "بى أم دبليو",
        "سيتروين",
        "لكزس",
        "جاكوار",
        "ساوايست",
        "مرسيدس",
        "هافال"
    )

    fun extractDataFromPlate() {
        var pp = plateText.replace("-", "")
        val linesInPlat = pp.split("\n").toMutableList()
        linesInPlat.remove(linesInPlat.find { it.contains("EGYPT") })
        linesInPlat.remove(linesInPlat.find { it.contains("مصر") })
        plateNum = linesInPlat.find { it -> it.count { it == ' ' } >= 1 } ?: ""
        plateNum = plateNum.replace("""[\p{P}\p{S}&&[^.]]+""".toRegex(), "")
    }

    fun extractDataFromLicense() {
        licenseText2.replace("شاسته", "شاسيه")
        licenseText2.replace("شنسيه", "شاسيه")
        licenseText2.replace("شنسية", "شاسيه")
        licenseText2.replace('ة', 'ه')
        try {
            val x = licenseText2.substring(licenseText2.indexOf("شاسيه") + 6, licenseText2.length)

            shaseh = x.substring(0, x.indexOfFirst { !it.isDigit() })
        } catch (_: Exception) {
        }
        try {
            val x3 = licenseText2.substring(licenseText2.indexOf("موتور") + 6, licenseText2.length)
            motor = x3.substring(0, x3.indexOfFirst { !it.isDigit() })
        } catch (_: Exception) {
        }
        try {
            color = licenseText2.split("\n")
                .find { listOfColors.contains(it.replace("[, ـ]".toRegex(), "")) } ?: ""
        } catch (_: Exception) {
        }
        try {
            carModel = licenseText2.split("\n")
                .find { listOfCarModels.contains(it.replace("[, ـ]".toRegex(), "")) }
                ?.replace("[, ـ]".toRegex(), "") ?: ""
        } catch (_: Exception) {
        }
        try {
            year = "\\b(19|20)\\d{2}\\b".toRegex().find(licenseText2)?.value ?: ""
        } catch (_: Exception) {
        }
    }

    fun processSimImage() {
        if (simText.length > 1) simGSM = try {
            val simSplitters = simText.split(",")
            simSerial = simSplitters[0]
            simSplitters[1]
        } catch (e: Exception) {
            simSerial = ""
            ""
        }
    }

    var plateText: String = ""

    //        "gitell\n" + ", مصر\n" + "ن ط ب ٣٩٥\n" + ", EGYPT\n" + ", 395\n" + ", B T N\n"
    var simText: String = ""
    var licenseText2: String = ""

    //        "Gr\n" + ", ۹۰۰۰۰۰۰۱۳٠٠٦٠٧٢\n" + ", نصر\n" + "شاسيه 9101788\n" + "موتور 0257043\n" + "4 سلندر بنزین\n" + "مجمعة التامين الاجباري للمركبات تاريخ الفحص ٢٠٢٥\n" + ", 127\n" + ", 1920\n" + ", غير مشترك عن سائق\n" + "م الكتروني\n" + ", لمسوريا\n" + ", **90880\n" + "أسود\n" + ", اساس\n" + "-\n" + ", SO\n"
    var shaseh: String = ""
    var motor: String = ""
    var carStatus: String = "working"
    var removeDeviceStatus: String = "working"
    var removeDeviceWith: String = "customer"
    var isSupplied: Int = 0
    var isSimSupplied: Int = 0
    var color: String = ""
    var year: String = ""
    var carModel: String = ""
    var plateNum: String = ""
    var simSerial: String = ""
    var simGSM: String = ""
    var orderId: Long = 0
    var imei: String = ""
    var oldImei: String = ""
    var oldPlate: String = ""
    var oldSim: String = ""
    var serial: String = ""
    var licenseImage: File? = null
    var plateImage: File? = null
    var licenseImage2: File? = null
    var deviceImage: File? = null
    var chasisImage: File? = null
    var simImage: File? = null
    var maintenanceFile: File? = null
    var licenseText: String = ""

    //        "جمهورية مصر العربية\n" + "وزارة الداخلية\n" + "ادارة مرور الفيـوم\n" + ", ايمن جرجس فرج عيد\n" + ", الكعابي الجديدة سنورس\n" + "نهاية الترخيص ۲۸-۰۹-۲۰۲۳\n" + "تاريخ التحرير ۲۸-۰۹-۲۰۲۰\n" + ", وحده مرور سنورس\n" + ", S\n" + ", ۸\n" + ", ۲۰۹۱۵\n" + ", رخصة تسير ملاكى\n" + "مصری\n" + ", عبدا ما عدتر\n"
    private val _userLogged: MutableStateFlow<Pair<Boolean, Long?>> =
        MutableStateFlow(Pair(false, null))
    val userLogged: StateFlow<Pair<Boolean, Long?>> = _userLogged.asStateFlow()
    private val _orderStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val orderStarted: StateFlow<Boolean> = _orderStarted.asStateFlow()

    init {
        viewModelScope.launch {
            _userLogged.update {
                userLogged()
            }
            _orderStarted.update {
                orderStarted()
            }
        }
    }

    private val _state = MutableStateFlow(CreateOrderViewState())
    val state: StateFlow<CreateOrderViewState> = _state.asStateFlow()

    fun createOrder(notes: String) {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _state.update {
                    try {
                        createOrder(
                            carModel,
                            year,
                            color,
                            shaseh,
                            motor,
                            plateNum,
                            oldPlate,
                            carStatus,
                            orderId,
                            isSupplied.toString(),
                            oldImei,
                            imei,
                            "",
                            serial,
                            if (simSerial.startsWith("89200220")) "vodafone" else if (simSerial.startsWith(
                                    "89200305"
                                )
                            ) "etisalat" else if (simSerial.startsWith(
                                    "8920018"
                                )
                            ) "orange" else "we",
                            simSerial,
                            simGSM,
                            oldSim,
                            isSimSupplied.toString(),
                            finalOrderType,
                            removeDeviceStatus,
                            removeDeviceWith,
                            selectedSensors as ArrayList<Sensor>,
                            selectedMaintenances as ArrayList<Maintenance>,
                            maintenanceFile,
                            simImage,
                            deviceImage,
                            plateImage,
                            licenseImage,
                            licenseImage2,
                            chasisImage,
                            notes
                        )
                        it.copy(loading = false, success = true)
                    } catch (e: IOException) {
                        it.copy(loading = false, failure = Event(e))
                    }
                }
            }
        }
    }

    fun setStateToIdle() {
        _state.update { it.copy(success = false) }
    }

    fun isNextCarInfoAvailable(plateAvailable: Int, licenseAvailable: Int): Boolean {
        return getPlateDone(plateAvailable, licenseAvailable)
    }

    private fun getPlateDone(plateAvailable: Int, licenseAvailable: Int): Boolean =
        plateAvailable == 0 || (plateAvailable == 1 && plateImage != null && licenseAvailable == 0) || (plateAvailable == 1 && plateImage != null && licenseAvailable == 1 && licenseImage != null && licenseImage2 != null)

    private fun getLicenseDone(licenseAvailable: Int): Boolean =
        licenseAvailable == 0 || (licenseAvailable == 1 && licenseImage != null && licenseImage2 != null)

    fun clearData() {

        plateText = ""
        simText = ""
        licenseText2 = ""

        shaseh = ""
        motor = ""
        carStatus = "working"
        removeDeviceStatus = "working"
        removeDeviceWith = "customer"
        isSupplied = 0
        color = ""
        year = ""
        carModel = ""
        plateNum = ""
        simSerial = ""
        simGSM = ""
        orderId = 0
        imei = ""
        oldImei = ""
        oldPlate = ""
        oldSim = ""
        serial = ""
        licenseImage = null
        plateImage = null
        licenseImage2 = null
        deviceImage = null
        chasisImage = null
        simImage = null
        maintenanceFile = null
        licenseText = ""

        plateInfoAuto = false
        licenseAuto = false
        selectedSensors = mutableListOf<Sensor>()
        selectedMaintenances = mutableListOf<Maintenance>()
        orderType = OrderType.UNDEFINED
    }

    fun saveOrderType(arrayListOf: java.util.ArrayList<String>) {
        this.finalOrderType = arrayListOf
    }


}

