package pk.kissanmadadgar.mobile.presentation.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pk.kissanmadadgar.mobile.R
import pk.kissanmadadgar.mobile.domain.model.Machinery
import pk.kissanmadadgar.mobile.presentation.MainViewModel
import pk.kissanmadadgar.mobile.core.theme.AgriGreenPrimary
import pk.kissanmadadgar.mobile.core.theme.AgriGreenLight
import pk.kissanmadadgar.mobile.core.UrduNarrations
import pk.kissanmadadgar.mobile.core.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Voice Booking State Machine
enum class VoiceBookingState {
    IDLE,
    SPEAKING_MACHINE_INFO,
    SPEAKING_INTRO,
    LISTENING_DATE,
    SPEAKING_DATE_CONFIRM,
    LISTENING_ACRES,
    SPEAKING_ACRES_CONFIRM,
    LISTENING_HOURS,
    SPEAKING_HOURS_CONFIRM,
    DONE
}

// Helper: Parse a spoken date string into millis
private fun parseSpokenDate(spoken: String): Long? {
    val text = spoken.trim().lowercase()
        .replace("کی", "").replace(" ki", "").replace(".", "").trim()

    val urduNumberWords = mapOf(
        "ek" to 1, "do" to 2, "teen" to 3, "char" to 4, "paanch" to 5,
        "chhe" to 6, "saat" to 7, "aath" to 8, "nau" to 9, "das" to 10,
        "gyarah" to 11, "baarah" to 12, "terah" to 13, "chaudah" to 14,
        "pandrah" to 15, "solah" to 16, "satrah" to 17, "athaarah" to 18,
        "unees" to 19, "bees" to 20, "ikkees" to 21, "baaees" to 22,
        "teyees" to 23, "chaubees" to 24, "pachchees" to 25,
        "chabbees" to 26, "sataaees" to 27, "athaaees" to 28,
        "untees" to 29, "tees" to 30, "ikatees" to 31,
        "ایک" to 1, "دو" to 2, "تین" to 3, "چار" to 4, "پانچ" to 5,
        "چھے" to 6, "سات" to 7, "آٹھ" to 8, "نو" to 9, "دس" to 10,
        "گیارہ" to 11, "بارہ" to 12, "تیرہ" to 13, "چودہ" to 14,
        "پندرہ" to 15, "سولہ" to 16, "سترہ" to 17, "اٹھارہ" to 18,
        "انیس" to 19, "بیس" to 20, "اکیس" to 21, "بائیس" to 22,
        "تئیس" to 23, "چوبیس" to 24, "پچیس" to 25,
        "چھبیس" to 26, "ستائیس" to 27, "اٹھائیس" to 28,
        "انتیس" to 29, "تیس" to 30, "اکتیس" to 31
    )

    val monthMap = mapOf(
        "january" to 0, "jan" to 0, "جنوری" to 0,
        "february" to 1, "feb" to 1, "فروری" to 1,
        "march" to 2, "mar" to 2, "مارچ" to 2,
        "april" to 3, "apr" to 3, "اپریل" to 3,
        "may" to 4, "مئی" to 4,
        "june" to 5, "jun" to 5, "جون" to 5,
        "july" to 6, "jul" to 6, "جولائی" to 6,
        "august" to 7, "aug" to 7, "اگست" to 7,
        "september" to 8, "sep" to 8, "sept" to 8, "ستمبر" to 8,
        "october" to 9, "oct" to 9, "اکتوبر" to 9,
        "november" to 10, "nov" to 10, "نومبر" to 10,
        "december" to 11, "dec" to 11, "دسمبر" to 11
    )

    val tokens = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
    var day: Int? = null
    var month: Int? = null
    var year: Int? = null

    for (token in tokens) {
        if (day == null) {
            val d = token.toIntOrNull() ?: urduNumberWords[token]
            if (d != null && d in 1..31) { day = d; continue }
        }
        if (month == null) {
            val m = monthMap[token]
            if (m != null) { month = m; continue }
        }
        if (year == null) {
            val y = token.toIntOrNull()
            if (y != null && y in 2020..2099) { year = y; continue }
        }
    }

    if (day == null || month == null) return null
    if (year == null) {
        year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }

    return try {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.set(year, month, day, 0, 0, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    } catch (e: Exception) { null }
}

// Helper: Parse spoken hours string into Int
private fun parseSpokenHours(spoken: String): Int? {
    val text = spoken.trim().lowercase()
        .replace("گھنٹے", "").replace("گھنٹہ", "")
        .replace("ghantay", "").replace("ghanta", "")
        .replace("ghante", "").replace("hours", "")
        .replace("hour", "").replace("kay liye", "")
        .replace("kay liyay", "").replace("کے لیے", "")
        .replace("کے لئے", "")
        .trim()

    val urduNumberWords = mapOf(
        "ek" to 1, "do" to 2, "teen" to 3, "char" to 4, "paanch" to 5,
        "chhe" to 6, "saat" to 7, "aath" to 8, "nau" to 9, "das" to 10,
        "ایک" to 1, "دو" to 2, "تین" to 3, "چار" to 4, "پانچ" to 5,
        "چھے" to 6, "سات" to 7, "آٹھ" to 8, "نو" to 9, "دس" to 10,
        "1" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5,
        "6" to 6, "7" to 7, "8" to 8, "9" to 9, "10" to 10
    )

    val tokens = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
    for (token in tokens) {
        val n = token.toIntOrNull() ?: urduNumberWords[token]
        if (n != null && n in 1..24) return n
    }
    return null
}

// Helper: Parse spoken acres (رقبہ) string into a Double, range 0.1 - 100
private fun parseSpokenAcres(spoken: String): Double? {
    val text = spoken.trim().lowercase()
        .replace("ایکڑ", "").replace("acres", "").replace("acre", "")
        .replace("رقبہ", "").replace("raqba", "")
        .trim()

    // Speech-to-text usually returns digits directly (e.g. "5" or "5.5") — try that first
    val directMatch = Regex("\\d+(\\.\\d+)?").find(text)
    if (directMatch != null) {
        val value = directMatch.value.toDoubleOrNull()
        if (value != null && value in 0.1..100.0) return value
    }

    val urduNumberWords = mapOf(
        "ek" to 1, "do" to 2, "teen" to 3, "char" to 4, "paanch" to 5,
        "chhe" to 6, "saat" to 7, "aath" to 8, "nau" to 9, "das" to 10,
        "ایک" to 1, "دو" to 2, "تین" to 3, "چار" to 4, "پانچ" to 5,
        "چھے" to 6, "سات" to 7, "آٹھ" to 8, "نو" to 9, "دس" to 10,
        "گیارہ" to 11, "بارہ" to 12, "تیرہ" to 13, "چودہ" to 14,
        "پندرہ" to 15, "بیس" to 20, "تیس" to 30, "چالیس" to 40,
        "پچاس" to 50, "ساٹھ" to 60, "ستر" to 70, "اسی" to 80,
        "نوے" to 90, "سو" to 100
    )

    val tokens = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
    for (token in tokens) {
        val n = urduNumberWords[token]
        if (n != null && n in 1..100) return n.toDouble()
    }
    return null
}

@Composable
fun StepIndicator(step: Int, isActive: Boolean, isCompleted: Boolean, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted || isActive) AgriGreenPrimary else Color.LightGray
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = step.toString(), 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = if (isActive) AgriGreenPrimary else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    machineryId: String,
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var machinery by remember { mutableStateOf<Machinery?>(null) }
    
    // Form States
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var acres by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf(4) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val user by viewModel.currentUser.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequesterAcres = remember { FocusRequester() }
    var showErrors by remember { mutableStateOf(false) }

    // --- Voice Booking States ---
    val context = LocalContext.current
    var voiceState by remember { mutableStateOf(VoiceBookingState.IDLE) }
    var voiceStatusText by remember { mutableStateOf("") }
    var machineStatusText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Pulse animation for mic button
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScaleAnim"
    )

    // Success audio narration speaking state
    var isSuccessSpeaking by remember { mutableStateOf(false) }

    // Success button animations
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha1"
    )
    
    val rippleScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 750),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val rippleAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 750),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha2"
    )

    val successButtonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSuccessSpeaking) 1.0f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "successButtonScale"
    )

    // TTS engine
    val tts = remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    // SpeechRecognizer
    val speechRecognizer = remember { mutableStateOf<android.speech.SpeechRecognizer?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Start voice flow after permission granted
            val item = machinery
            if (item != null) {
                voiceState = VoiceBookingState.SPEAKING_INTRO
                val userName = user?.fullName ?: context.getString(R.string.user_guest_name)
                val machineName = item.nameUr
                val ownerName = item.providerName
                val introText = UrduNarrations.getBookingIntro(context, userName, machineName, ownerName)
                voiceStatusText = context.getString(R.string.voice_status_listening)
                tts.value?.speak(introText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "intro")
            }
        } else {
            android.widget.Toast.makeText(context, context.getString(R.string.toast_mic_permission), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Helper: Start listening with SpeechRecognizer
    fun startListening() {
        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ur-PK")
            putExtra(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        try {
            speechRecognizer.value?.startListening(intent)
        } catch (e: Exception) {
            voiceStatusText = context.getString(R.string.voice_status_recording_failed)
            voiceState = VoiceBookingState.IDLE
        }
    }

    // Initialize TTS & SpeechRecognizer
    DisposableEffect(Unit) {
        var ttsInstance: android.speech.tts.TextToSpeech? = null
        ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                val urLocale = java.util.Locale("ur", "PK")
                ttsInstance?.language = urLocale

                // Find a high-quality Urdu male voice. Rank by (male-name match, quality) —
                // a male-named voice wins first, but among same-match candidates the highest
                // Voice.quality (Android's own naturalness tier) wins. Pitch is left at the
                // engine's natural default below — artificially shifting pitch on a synthesized
                // voice is what made narration sound robotic, not a genuinely deeper voice.
                val voices = ttsInstance?.voices
                if (voices != null) {
                    val urduVoices = voices.filter { it.locale.language == "ur" }
                    val selectedVoice = urduVoices
                        .sortedWith(
                            compareByDescending<android.speech.tts.Voice> { voice ->
                                val name = voice.name.lowercase()
                                if (name.contains("urm") || name.contains("male") || name.contains("-m-")) 1 else 0
                            }.thenByDescending { it.quality }
                        )
                        .firstOrNull()

                    if (selectedVoice != null) {
                        ttsInstance?.voice = selectedVoice
                    }
                }

                ttsInstance?.setSpeechRate(0.95f)
                ttsInstance?.setPitch(1.0f)
            }
        }
        tts.value = ttsInstance

        // Set TTS utterance listener to chain the voice flow
        ttsInstance.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (utteranceId == "success_booking") {
                    isSuccessSpeaking = true
                }
            }
            override fun onDone(utteranceId: String?) {
                if (utteranceId == "success_booking") {
                    isSuccessSpeaking = false
                }
                when (utteranceId) {
                    "machine_info" -> {
                        voiceState = VoiceBookingState.DONE
                        machineStatusText = context.getString(R.string.voice_status_done)
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(2000)
                            voiceState = VoiceBookingState.IDLE
                            machineStatusText = ""
                        }
                    }
                    "intro" -> {
                        voiceState = VoiceBookingState.LISTENING_DATE
                        voiceStatusText = context.getString(R.string.voice_status_speak_date)
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(300)
                            startListening()
                        }
                    }
                    "date_confirm" -> {
                        voiceState = VoiceBookingState.LISTENING_ACRES
                        voiceStatusText = context.getString(R.string.voice_status_speak_acres)
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(300)
                            startListening()
                        }
                    }
                    "acres_confirm" -> {
                        voiceState = VoiceBookingState.LISTENING_HOURS
                        voiceStatusText = context.getString(R.string.voice_status_speak_hours)
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(300)
                            startListening()
                        }
                    }
                    "date_error_past" -> {
                        voiceState = VoiceBookingState.LISTENING_DATE
                        voiceStatusText = context.getString(R.string.voice_status_speak_date)
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(300)
                            startListening()
                        }
                    }
                    "hours_confirm" -> {
                        voiceState = VoiceBookingState.DONE
                        voiceStatusText = context.getString(R.string.voice_status_done)
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(2000)
                            voiceState = VoiceBookingState.IDLE
                            voiceStatusText = ""
                        }
                    }
                }
            }
            @Deprecated("Deprecated in API")
            override fun onError(utteranceId: String?) {
                if (utteranceId == "success_booking") {
                    isSuccessSpeaking = false
                }
                voiceState = VoiceBookingState.IDLE
                voiceStatusText = "⚠️ " + context.getString(R.string.voice_status_recording_failed)
            }
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                if (utteranceId == "success_booking") {
                    isSuccessSpeaking = false
                }
            }
        })

        // Initialize SpeechRecognizer
        if (android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
            val sr = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
            sr.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    voiceStatusText = when (voiceState) {
                        VoiceBookingState.LISTENING_DATE -> context.getString(R.string.voice_status_speak_date)
                        VoiceBookingState.LISTENING_ACRES -> context.getString(R.string.voice_status_speak_acres)
                        VoiceBookingState.LISTENING_HOURS -> context.getString(R.string.voice_status_speak_hours)
                        else -> context.getString(R.string.voice_status_ready)
                    }
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    voiceStatusText = context.getString(R.string.voice_status_speak_try)
                }
                override fun onError(error: Int) {
                    val errorMsg = when (error) {
                        android.speech.SpeechRecognizer.ERROR_NO_MATCH -> context.getString(R.string.voice_error_no_match)
                        android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> context.getString(R.string.voice_error_timeout)
                        android.speech.SpeechRecognizer.ERROR_AUDIO -> context.getString(R.string.voice_error_audio)
                        android.speech.SpeechRecognizer.ERROR_NETWORK -> context.getString(R.string.voice_error_network)
                        else -> context.getString(R.string.voice_error_prefix, error.toString())
                    }
                    voiceStatusText = context.getString(R.string.voice_error_prefix, errorMsg)
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(2500)
                        voiceState = VoiceBookingState.IDLE
                        voiceStatusText = ""
                    }
                }
                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""

                    when (voiceState) {
                        VoiceBookingState.LISTENING_DATE -> {
                            val dateMillis = parseSpokenDate(spokenText)
                            if (dateMillis != null) {
                                val todayStart = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                                    set(java.util.Calendar.MINUTE, 0)
                                    set(java.util.Calendar.SECOND, 0)
                                    set(java.util.Calendar.MILLISECOND, 0)
                                }.timeInMillis

                                if (dateMillis < todayStart) {
                                    voiceState = VoiceBookingState.SPEAKING_DATE_CONFIRM
                                    voiceStatusText = context.getString(R.string.voice_status_past_date_error)
                                    val errorNarration = context.getString(R.string.narrate_past_date_error)
                                    tts.value?.speak(errorNarration, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "date_error_past")
                                } else {
                                    selectedDateMillis = dateMillis
                                    val displayDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
                                    val displayDateUr = SimpleDateFormat("d MMMM yyyy", java.util.Locale("ur", "PK")).format(Date(dateMillis))
                                    voiceState = VoiceBookingState.SPEAKING_DATE_CONFIRM
                                    voiceStatusText = context.getString(R.string.voice_status_date_selected, displayDate)
                                    val confirmText = UrduNarrations.getBookingDateConfirmation(context, displayDateUr)
                                    tts.value?.speak(confirmText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "date_confirm")
                                }
                            } else {
                                voiceStatusText = context.getString(R.string.voice_status_date_not_understood)
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1500)
                                    voiceStatusText = context.getString(R.string.voice_status_speak_date)
                                    startListening()
                                }
                            }
                        }
                        VoiceBookingState.LISTENING_ACRES -> {
                            val parsedAcres = parseSpokenAcres(spokenText)
                            if (parsedAcres != null) {
                                val formattedAcres = if (parsedAcres == parsedAcres.toLong().toDouble()) {
                                    parsedAcres.toLong().toString()
                                } else {
                                    parsedAcres.toString()
                                }
                                acres = formattedAcres
                                voiceState = VoiceBookingState.SPEAKING_ACRES_CONFIRM
                                voiceStatusText = context.getString(R.string.voice_status_acres_selected, formattedAcres)
                                val confirmText = UrduNarrations.getBookingAcresConfirmation(context, formattedAcres)
                                tts.value?.speak(confirmText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "acres_confirm")
                            } else {
                                voiceStatusText = context.getString(R.string.voice_status_acres_not_understood)
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1500)
                                    voiceStatusText = context.getString(R.string.voice_status_speak_acres)
                                    startListening()
                                }
                            }
                        }
                        VoiceBookingState.LISTENING_HOURS -> {
                            val parsedHours = parseSpokenHours(spokenText)
                            if (parsedHours != null) {
                                hours = parsedHours
                                voiceState = VoiceBookingState.SPEAKING_HOURS_CONFIRM
                                voiceStatusText = context.getString(R.string.voice_status_hours_selected, parsedHours.toString())
                                val confirmText = UrduNarrations.getBookingHoursConfirmation(context, parsedHours)
                                tts.value?.speak(confirmText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "hours_confirm")
                            } else {
                                voiceStatusText = context.getString(R.string.voice_status_hours_not_understood)
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1500)
                                    voiceStatusText = context.getString(R.string.voice_status_speak_hours)
                                    startListening()
                                }
                            }
                        }
                        else -> {}
                    }
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
            speechRecognizer.value = sr
        }

        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
            speechRecognizer.value?.stopListening()
            speechRecognizer.value?.destroy()
        }
    }

    LaunchedEffect(machineryId) {
        machinery = viewModel.availableMachinery.value.find { it.id == machineryId }
    }

    if (showSuccess) {
        val displayDate = selectedDateMillis?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
        } ?: stringResource(id = R.string.label_unknown)
        val displayDateUr = selectedDateMillis?.let {
            SimpleDateFormat("d MMMM yyyy", java.util.Locale("ur", "PK")).format(Date(it))
        } ?: displayDate
        val acresValue = acres.toDoubleOrNull() ?: 0.0
        val ownerName = machinery?.providerName ?: ""
        val successNarrationText = UrduNarrations.getBookingSuccessNarration(acres, ownerName, displayDateUr)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FA))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle, 
                            contentDescription = stringResource(id = R.string.content_description_success), 
                            tint = AgriGreenPrimary, 
                            modifier = Modifier.size(80.dp)
                        )
                        
                        Text(
                            text = stringResource(id = R.string.booking_confirm_success_msg),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = stringResource(id = R.string.booking_success_desc),
                            fontSize = 15.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        // Audio Narration Button (same style and icon as welcome assistant)
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Ripples (glowing waves when idle)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = rippleScale1,
                                        scaleY = rippleScale1,
                                        alpha = rippleAlpha1
                                    )
                                    .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = rippleScale2,
                                        scaleY = rippleScale2,
                                        alpha = rippleAlpha2
                                    )
                                    .background(Color(0xFFFF6D00).copy(alpha = 0.4f), CircleShape)
                            )

                            // Main Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer(
                                        scaleX = successButtonScale,
                                        scaleY = successButtonScale
                                    )
                                    .shadow(elevation = 6.dp, shape = CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (isSuccessSpeaking) {
                                                listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                            } else {
                                                listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                            }
                                        ),
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable {
                                        if (isSuccessSpeaking) {
                                            tts.value?.stop()
                                            isSuccessSpeaking = false
                                        } else {
                                            tts.value?.speak(successNarrationText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "success_booking")
                                            isSuccessSpeaking = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSuccessSpeaking) {
                                    // Active sound wave equalizer animation
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 4) {
                                            val barHeightScale by infiniteTransition.animateFloat(
                                                initialValue = 0.2f,
                                                targetValue = 1.0f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(durationMillis = 300 + (i * 100)),
                                                    repeatMode = RepeatMode.Reverse
                                                ),
                                                label = "bar_success_$i"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(16.dp * barHeightScale)
                                                    .background(Color.White, RoundedCornerShape(1.5.dp))
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.SupportAgent,
                                        contentDescription = "آڈیو سنیں",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                        SummaryCard(
                            items = listOf(
                                stringResource(id = R.string.summary_label_date) to displayDate,
                                stringResource(id = R.string.summary_label_acres) to if (acresValue > 0.0) acres else "-"
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                PrimaryButton(
                    text = stringResource(id = R.string.btn_ok),
                    onClick = { onSuccess() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            AgriDetailHeader(
                title = stringResource(id = R.string.booking_confirm_title),
                onBackClick = { onBack() }
            )
        }
    ) { padding ->
        if (machinery == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriGreenPrimary)
            }
        } else {
            val item = machinery!!
            
            KeyboardAwareContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- Machine Info Card with Voice Mic Button ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Title row with machine info mic button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.label_machine_info),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )

                                // Machine Info Narration Button
                                val isMachineInfoSpeaking = voiceState == VoiceBookingState.SPEAKING_MACHINE_INFO
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isMachineInfoSpeaking) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(scaleX = micScale, scaleY = micScale)
                                                .background(Color(0xFFFFB300).copy(alpha = 0.4f), CircleShape)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .graphicsLayer(scaleX = if (isMachineInfoSpeaking) micScale else 1f, scaleY = if (isMachineInfoSpeaking) micScale else 1f)
                                            .shadow(if (isMachineInfoSpeaking) 4.dp else 2.dp, CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = if (isMachineInfoSpeaking) {
                                                        listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                    } else {
                                                        listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                    }
                                                ),
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .clickable {
                                                if (isMachineInfoSpeaking) {
                                                    tts.value?.stop()
                                                    voiceState = VoiceBookingState.IDLE
                                                    machineStatusText = ""
                                                } else if (voiceState == VoiceBookingState.IDLE || voiceState == VoiceBookingState.DONE) {
                                                    val machineItem = machinery
                                                    if (machineItem != null) {
                                                        voiceState = VoiceBookingState.SPEAKING_MACHINE_INFO
                                                        machineStatusText = context.getString(R.string.voice_status_machine_details)
                                                        val userName = user?.fullName ?: context.getString(R.string.user_guest_name)
                                                        val machineName = machineItem.nameUr
                                                        val ownerName = machineItem.providerName
                                                        val distance = machineItem.distanceText ?: ("1.2 " + context.getString(R.string.distance_km_format).replace("%1\$s", "").trim())
                                                        val projectName = machineItem.projectName ?: context.getString(R.string.pcap_fallback)
                                                        val subsidyInfo = machineItem.subsidyText ?: context.getString(R.string.subsidy_fallback)

                                                        val machineInfoText = UrduNarrations.getMachineInfoNarration(
                                                            context,
                                                            userName,
                                                            machineName,
                                                            ownerName,
                                                            distance,
                                                            projectName,
                                                            subsidyInfo
                                                        )
                                                        tts.value?.speak(machineInfoText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "machine_info")
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isMachineInfoSpeaking) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (i in 0 until 3) {
                                                    val barHeightScale by infiniteTransition.animateFloat(
                                                        initialValue = 0.2f,
                                                        targetValue = 1.0f,
                                                        animationSpec = infiniteRepeatable(
                                                            animation = tween(400 + i * 150),
                                                            repeatMode = RepeatMode.Reverse
                                                        ),
                                                        label = "bar_mach_info_$i"
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 2.5.dp, height = 12.dp)
                                                            .graphicsLayer(scaleY = barHeightScale)
                                                            .background(Color.White, RoundedCornerShape(1.dp))
                                                    )
                                                }
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.SupportAgent,
                                                contentDescription = stringResource(id = R.string.content_description_listen_audio),
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            val localContext = LocalContext.current
                            val isAuthorized = user != null
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFECF7F2))
                                            .border(BorderStroke(1.dp, Color(0xFF0B5D34).copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.logo_pcap),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(id = R.string.pcap_fallback),
                                                color = Color(0xFF0B5D34),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // Full card width, not squeezed against the subsidy badge below —
                                // that badge claims a fixed chunk of the row it's in regardless of
                                // available space, so anything sharing a row with it needs its own
                                // full-width row instead to avoid truncating.
                                Text(
                                    text = item.providerName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AgriGreenPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Same full-width treatment as the provider name above — this used
                                // to live inside the weight(1f) column squeezed against the subsidy
                                // badge, which is why "سُپَر سِیڈر" was clipping to "سُپَر سِ...".
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_super_seeder),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.nameUr,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // Same treatment for the phone number, which also gets its own
                                // TextOverflow.Ellipsis now — it previously had none, so instead of
                                // truncating visibly it hard-clipped mid-digit with no "..." to show
                                // anything was missing.
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val clipboardManager = localContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clipData = android.content.ClipData.newPlainText("phone", item.providerPhone)
                                            clipboardManager.setPrimaryClip(clipData)
                                            android.widget.Toast.makeText(localContext, context.getString(R.string.toast_phone_copied), android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_phone_round),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val displayPhone = if (isAuthorized) item.providerPhone else item.providerPhone.take(4) + "-*******"
                                    Text(
                                        text = displayPhone,
                                        color = Color.DarkGray,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val rawDistanceText = item.distanceText ?: "1.2"
                                        // Only wrap plain numeric distances (e.g. "1.2") with the "X کلومیٹر دور"
                                        // template. The backend can also send an already human-readable phrase
                                        // (e.g. "کچھ قدم دور" for very short distances) — appending the km
                                        // suffix to that produced garbled text like "کچھ قدم دور کلومیٹر دور".
                                        val displayDistanceText = if (rawDistanceText.trim().toDoubleOrNull() != null) {
                                            stringResource(id = R.string.distance_km_format, rawDistanceText)
                                        } else {
                                            rawDistanceText
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_location_round),
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = displayDistanceText, color = Color.DarkGray, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp))
                                            .background(Color(0xFFECF7F2))
                                            .border(BorderStroke(1.dp, Color(0xFF0B5D34).copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = Color(0xFF0B5D34),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = stringResource(id = R.string.label_subsidy_scheme),
                                                color = Color(0xFF0B5D34),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stringResource(id = R.string.subsidy_fallback),
                                                color = Color(0xFF0B5D34),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val todayStart = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val dateError = if (selectedDateMillis == null) {
                        if (showErrors) stringResource(id = R.string.error_select_date) else null
                    } else if (selectedDateMillis!! < todayStart) {
                        stringResource(id = R.string.error_past_date)
                    } else null



                    // --- Booking Card with Voice Mic Button ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Title row with mic button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.booking_details_title),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )

                                // Voice Mic / Booking Assistant Button
                                val isVoiceActive = voiceState == VoiceBookingState.SPEAKING_INTRO ||
                                        voiceState == VoiceBookingState.LISTENING_DATE ||
                                        voiceState == VoiceBookingState.SPEAKING_DATE_CONFIRM ||
                                        voiceState == VoiceBookingState.LISTENING_ACRES ||
                                        voiceState == VoiceBookingState.SPEAKING_ACRES_CONFIRM ||
                                        voiceState == VoiceBookingState.LISTENING_HOURS ||
                                        voiceState == VoiceBookingState.SPEAKING_HOURS_CONFIRM
                                val isListening = voiceState == VoiceBookingState.LISTENING_DATE ||
                                        voiceState == VoiceBookingState.LISTENING_ACRES ||
                                        voiceState == VoiceBookingState.LISTENING_HOURS
                                val isSpeaking = isVoiceActive && !isListening

                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isVoiceActive) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(scaleX = micScale, scaleY = micScale)
                                                .background((if (isListening) Color(0xFFD32F2F) else Color(0xFFFFB300)).copy(alpha = 0.4f), CircleShape)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .graphicsLayer(scaleX = if (isVoiceActive) micScale else 1f, scaleY = if (isVoiceActive) micScale else 1f)
                                            .shadow(if (isVoiceActive) 4.dp else 2.dp, CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = when {
                                                        isListening -> listOf(Color(0xFFD32F2F), Color(0xFFFF1744))
                                                        isSpeaking -> listOf(Color(0xFFE65100), Color(0xFFFF3D00))
                                                        else -> listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                                    }
                                                ),
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .clickable {
                                                if (voiceState == VoiceBookingState.IDLE || voiceState == VoiceBookingState.DONE) {
                                                    // Check permission and start
                                                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                                                            context, android.Manifest.permission.RECORD_AUDIO
                                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                                    ) {
                                                        val machineItem = machinery
                                                        if (machineItem != null) {
                                                            voiceState = VoiceBookingState.SPEAKING_INTRO
                                                            val userName = user?.fullName ?: context.getString(R.string.user_guest_name)
                                                            val machineName = machineItem.nameUr
                                                            val ownerName = machineItem.providerName
                                                            val introText = UrduNarrations.getBookingIntro(context, userName, machineName, ownerName)
                                                            voiceStatusText = context.getString(R.string.voice_status_listening)
                                                            tts.value?.speak(introText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "intro")
                                                        }
                                                    } else {
                                                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                                    }
                                                } else {
                                                    // Cancel current voice flow
                                                    tts.value?.stop()
                                                    speechRecognizer.value?.stopListening()
                                                    voiceState = VoiceBookingState.IDLE
                                                    voiceStatusText = ""
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isListening) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = stringResource(id = R.string.content_description_mic),
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (isSpeaking) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (i in 0 until 3) {
                                                    val barHeightScale by infiniteTransition.animateFloat(
                                                        initialValue = 0.2f,
                                                        targetValue = 1.0f,
                                                        animationSpec = infiniteRepeatable(
                                                            animation = tween(400 + i * 150),
                                                            repeatMode = RepeatMode.Reverse
                                                        ),
                                                        label = "bar_booking_flow_$i"
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 2.5.dp, height = 12.dp)
                                                            .graphicsLayer(scaleY = barHeightScale)
                                                            .background(Color.White, RoundedCornerShape(1.dp))
                                                    )
                                                }
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.SupportAgent,
                                                contentDescription = stringResource(id = R.string.content_description_listen_audio),
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Voice Status Banner
                            AnimatedVisibility(
                                visible = voiceState != VoiceBookingState.SPEAKING_MACHINE_INFO && voiceStatusText.isNotEmpty(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                val statusBgColor = when {
                                    voiceState == VoiceBookingState.LISTENING_DATE ||
                                    voiceState == VoiceBookingState.LISTENING_ACRES ||
                                    voiceState == VoiceBookingState.LISTENING_HOURS -> Color(0xFFFFF3E0) // Warm orange bg
                                    voiceState == VoiceBookingState.SPEAKING_INTRO ||
                                    voiceState == VoiceBookingState.SPEAKING_DATE_CONFIRM ||
                                    voiceState == VoiceBookingState.SPEAKING_ACRES_CONFIRM ||
                                    voiceState == VoiceBookingState.SPEAKING_HOURS_CONFIRM -> Color(0xFFE8F5E9) // Green bg
                                    voiceState == VoiceBookingState.DONE -> Color(0xFFE8F5E9)
                                    else -> Color(0xFFFFF8E1)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(statusBgColor)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = voiceStatusText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF333333)
                                        )
                                    }
                                }
                            }

                            DatePickerField(
                                selectedDateMillis = selectedDateMillis,
                                onDateSelected = { selectedDateMillis = it },
                                isError = dateError != null
                            )
                            dateError?.let { err ->
                                Spacer(modifier = Modifier.height(4.dp))
                                ErrorMessage(message = err)
                            }

                            // Acres Input moved above Matlooba Ghantay (Hours)
                            val acresDouble = acres.toDoubleOrNull()
                            val acresError = if (acres.isEmpty()) {
                                if (showErrors) stringResource(id = R.string.error_acres_required) else null
                            } else if (acresDouble == null || acresDouble < 0.1 || acresDouble > 100.0) {
                                stringResource(id = R.string.error_acres_invalid)
                            } else null

                            Column(modifier = Modifier.fillMaxWidth()) {
                                NumberInputField(
                                    value = acres,
                                    onValueChange = { acres = it },
                                    label = stringResource(id = R.string.label_acres_required),
                                    helperText = stringResource(id = R.string.helper_acres_range),
                                    textFieldModifier = Modifier.focusRequester(focusRequesterAcres),
                                    isError = acresError != null,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                    ),
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    )
                                )
                                acresError?.let { err ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ErrorMessage(message = err)
                                }
                            }
                        }
                    }

                    val acresValue = acres.toDoubleOrNull() ?: 0.0
                    val isAcresValid = acres.toDoubleOrNull() != null && acres.toDoubleOrNull()!! in 0.1..100.0
                    val isDateValid = selectedDateMillis != null && selectedDateMillis!! >= todayStart
                    val isFormValid = isDateValid && isAcresValid

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LoadingButton(
                            text = stringResource(id = R.string.btn_finalize_booking),
                            isLoading = isSubmitting,
                            enabled = !isSubmitting,
                            containerColor = if (isFormValid) AgriGreenPrimary else Color(0xFF89C2A5),
                            onClick = {
                                if (isFormValid) {
                                    isSubmitting = true
                                    viewModel.createBooking(
                                        item.id, selectedDateMillis!!, 0, item.hourlyRate, acresValue,
                                        onSuccess = {
                                            isSubmitting = false
                                            showSuccess = true
                                        },
                                        onError = {
                                            // The failure message itself is already shown via a
                                            // Toast in MainViewModel.createBooking — this just
                                            // has to stop the button from spinning forever.
                                            isSubmitting = false
                                        }
                                    )
                                } else {
                                    showErrors = true
                                    if (!isAcresValid) {
                                        focusRequesterAcres.requestFocus()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        SecondaryButton(
                            text = stringResource(id = R.string.btn_back),
                            onClick = { onBack() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
