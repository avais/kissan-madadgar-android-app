package pk.kissanmadadgar.mobile.data.local

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * App-wide singleton for spoken narration. Every screen previously created its own
 * TextToSpeech engine (7 separate instances across the app) with duplicated voice-selection
 * and language-init logic — wasteful, and any tuning (like the voice/pitch fix below) had to be
 * copy-pasted into each one and could drift out of sync. This is the one instance the whole app
 * shares; call initialize() once per screen (idempotent — a no-op after the first real call) and
 * use speak()/stop()/activeUtteranceId from anywhere.
 */
object NarrationManager {
    private var tts: TextToSpeech? = null
    private var isInitializing = false

    private val _activeUtteranceId = MutableStateFlow<String?>(null)
    val activeUtteranceId: StateFlow<String?> = _activeUtteranceId.asStateFlow()

    fun initialize(context: Context) {
        if (tts != null || isInitializing) return
        isInitializing = true
        val instance = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureVoice(tts)
            }
            isInitializing = false
        }
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _activeUtteranceId.value = utteranceId
            }
            override fun onDone(utteranceId: String?) {
                if (_activeUtteranceId.value == utteranceId) _activeUtteranceId.value = null
            }
            override fun onError(utteranceId: String?) {
                if (_activeUtteranceId.value == utteranceId) _activeUtteranceId.value = null
            }
        })
        tts = instance
    }

    private fun configureVoice(instance: TextToSpeech?) {
        val urLocale = Locale("ur", "PK")
        instance?.language = urLocale

        val voices = instance?.voices
        if (voices != null) {
            val urduVoices = voices.filter { it.locale.language == "ur" }
            // Rank by (male-name match, quality): a voice explicitly named male wins first,
            // but among candidates with the same male-match status the highest Voice.quality
            // (Android's own naturalness tier, e.g. VERY_HIGH network voices vs. low-quality
            // embedded ones) wins. Pitch is left at the engine's natural default — artificially
            // shifting pitch on a synthesized voice is what made narration sound robotic before.
            val selectedVoice = urduVoices
                .sortedWith(
                    compareByDescending<Voice> { voice ->
                        val name = voice.name.lowercase()
                        if (name.contains("urm") || name.contains("male") || name.contains("-m-")) 1 else 0
                    }.thenByDescending { it.quality }
                )
                .firstOrNull()
            if (selectedVoice != null) {
                instance.voice = selectedVoice
            }
        }
        instance?.setSpeechRate(0.95f)
        instance?.setPitch(1.0f)
    }

    fun speak(text: String, utteranceId: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _activeUtteranceId.value = null
    }

    fun isSpeaking(utteranceId: String): Boolean = _activeUtteranceId.value == utteranceId
}
