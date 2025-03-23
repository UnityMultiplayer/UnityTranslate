package xyz.bluspring.unitytranslate.transcriber.sapi5

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType

class WindowsSpeechApiTranscriber(instance: UnityTranslate, language: Language) : SpeechTranscriber(TranscriberType.WINDOWS_SAPI, instance, language) {
    init {
    }

    // TODO: need to figure out how to use the Speech Recognition API without using JNI.
    //       or how to use it in general.

    override fun stop() {

    }

    companion object {
        private var isLibraryLoaded = false

        init {
            loadLibrary()
        }

        fun loadLibrary() {
        }

        fun isSupported(): Boolean {
            return isLibraryLoaded
        }
    }

    /*companion object {
        lateinit var library: SharedLibrary

        private var hasTriedLoading = false

        fun isLibraryLoaded(): Boolean {
            return Companion::library.isInitialized
        }

        fun isSupported(): Boolean {
            if (Util.getPlatform() != Util.OS.WINDOWS)
                return false

            //tryLoadingLibrary()
            //return isLibraryLoaded()
            return false
        }

        private fun tryLoadingLibrary() {
            if (!hasTriedLoading && !isLibraryLoaded()) {
                try {
                    library = APIUtil.apiCreateLibrary("sapi.dll")
                } catch (e: Throwable) {
                    UnityTranslate.logger.error("Failed to load Windows Speech API!")
                    e.printStackTrace()
                }

                hasTriedLoading = true
            }
        }


    }*/
}