package xyz.bluspring.unitytranslate.common.util.nativeaccess

import com.kenai.jffi.Platform
import jnr.ffi.LibraryLoader
import jnr.ffi.Runtime
import jnr.ffi.byref.IntByReference
import jnr.ffi.byref.PointerByReference
import xyz.bluspring.unitytranslate.common.UnityTranslate

object CudaHelper {
    private const val CUDA_SUCCESS = 0

    private var hasInit = false
    private var isLibraryLoaded = false

    private lateinit var cudaLibrary: CudaLibrary

    val isCudaSupported = getCudaSupport().apply {
        if (this.status)
            UnityTranslate.logger.info("CUDA is supported, using GPU for translations.")
        else
            UnityTranslate.logger.info("CUDA is not supported, using CPU for translations. (Code: ${this.name})")
    }.status

    fun loadLibrary() {
        if (hasInit)
            return

        hasInit = true

        val cudaLoader = LibraryLoader.create(CudaLibrary::class.java)
            .failImmediately()

        val platform = Platform.getPlatform()

        val cudaLibrary = try {
            if (platform.os == Platform.OS.WINDOWS)
                cudaLoader.load("nvcuda.dll")
            else if (platform.os == Platform.OS.LINUX)
                cudaLoader.load("libcuda.so")
            else null
        } catch (_: UnsatisfiedLinkError) {
            UnityTranslate.logger.warn("CUDA library failed to load! Not attempting to initialize CUDA functions.")
            null
        } catch (e: Throwable) {
            UnityTranslate.logger.error("An error occurred while searching for CUDA devices!")
            e.printStackTrace()
            null
        }

        if (cudaLibrary != null) {
            isLibraryLoaded = true
        } else return

        this.cudaLibrary = cudaLibrary
    }

    private fun getCudaSupport(): CudaSupport {
        loadLibrary()
        if (!isLibraryLoaded)
            return CudaSupport.NO_LIBRARY

        if (!checkSuccess(cudaLibrary.cuInit(0), "init"))
            return CudaSupport.FUNCTION_FAILED

        val totalPtr = IntByReference()
        if (!checkSuccess(cudaLibrary.cuDeviceGetCount(totalPtr), "get device count"))
            return CudaSupport.FUNCTION_FAILED

        val totalCudaDevices = totalPtr.value
        UnityTranslate.logger.info("Total CUDA devices: $totalCudaDevices")
        if (totalCudaDevices <= 0)
            return CudaSupport.NO_DEVICES

        for (i in 0 until totalCudaDevices) {
            val majorPtr = IntByReference()
            val minorPtr = IntByReference()

            if (!checkSuccess(cudaLibrary.cuDeviceComputeCapability(majorPtr, minorPtr, i), "get device compute capability $i"))
                continue

            val majorVersion = majorPtr.value
            val minorVersion = minorPtr.value

            UnityTranslate.logger.info("Found device with CUDA compute capability major $majorVersion minor $minorVersion.")
            return CudaSupport.SUPPORTED
        }

        return CudaSupport.NO_DEVICES
    }

    private fun checkSuccess(code: Int, name: String): Boolean {
        if (code != CUDA_SUCCESS) {
            logCudaError(code, name)
            return false
        }

        return true
    }

    private fun logCudaError(code: Int, at: String) {
        if (code == CUDA_SUCCESS)
            return

        if (!isLibraryLoaded)
            return

        val runtime = Runtime.getRuntime(cudaLibrary)

        val errorCodePtr = runtime.memoryManager.allocateDirect(255)
        val errorDescPtr = runtime.memoryManager.allocateDirect(255)

        val errorCode = if (cudaLibrary.cuGetErrorName(code, PointerByReference(errorCodePtr)) == CUDA_SUCCESS)
            errorCodePtr.getString(0, 255, Charsets.UTF_16)
         else "[CUDA ERROR NAME NOT FOUND]"

        val errorDesc = if (cudaLibrary.cuGetErrorString(code, PointerByReference(errorDescPtr)) == CUDA_SUCCESS)
            errorDescPtr.getString(0, 255, Charsets.UTF_16)
        else "[CUDA ERROR DESC NOT FOUND]"

        UnityTranslate.logger.error("CUDA error at $at: $code $errorCode ($errorDesc)")
    }

    enum class CudaSupport(val status: Boolean) {
        SUPPORTED(true),
        NO_LIBRARY(false),
        FUNCTION_FAILED(false),
        NO_DEVICES(false)
    }
}