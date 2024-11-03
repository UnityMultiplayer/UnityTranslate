package xyz.bluspring.unitytranslate.util.nativeaccess

import net.minecraft.Util
import org.jetbrains.annotations.ApiStatus.Internal
import org.lwjgl.system.APIUtil
import org.lwjgl.system.JNI
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.SharedLibrary
import xyz.bluspring.unitytranslate.UnityTranslate

/**
 * This class should NEVER be accessed by normal means, only by using the NativeAccess or the LwjglLoader objects.
 */
@Internal
internal object CudaAccess {
    private var isLibraryLoaded = false
    private lateinit var library: SharedLibrary
    private var PFN_cuInit: Long = 0L
    private var PFN_cuDeviceGetCount: Long = 0L
    private var PFN_cuDeviceComputeCapability: Long = 0L

    private var PFN_cuGetErrorName: Long = 0L
    private var PFN_cuGetErrorString: Long = 0L

    private fun logCudaError(code: Int, at: String) {
        if (code == 0)
            return

        // TODO: these return ??? for some reason.
        //       can we figure out why?

        val errorCode = if (PFN_cuGetErrorName != MemoryUtil.NULL) {
            val ptr = MemoryUtil.nmemAlloc(255)
            JNI.callPP(code, ptr, PFN_cuGetErrorName)
            MemoryUtil.memUTF16(ptr).apply {
                MemoryUtil.nmemFree(ptr)
            }
        } else "[CUDA ERROR NAME NOT FOUND]"

        val errorDesc = if (PFN_cuGetErrorString != MemoryUtil.NULL) {
            val ptr = MemoryUtil.nmemAlloc(255)
            JNI.callPP(code, ptr, PFN_cuGetErrorString)
            MemoryUtil.memUTF16(ptr).apply {
                MemoryUtil.nmemFree(ptr)
            }
        } else "[CUDA ERROR DESC NOT FOUND]"

        UnityTranslate.logger.error("CUDA error at $at: $code $errorCode ($errorDesc)")
    }

    @JvmStatic
    val cudaState: CudaState = isCudaSupported()

    private fun isCudaSupported(): CudaState {
        if (!isLibraryLoaded) {
            try {
                library = if (Util.getPlatform() == Util.OS.WINDOWS) {
                    APIUtil.apiCreateLibrary("nvcuda.dll")
                } else if (Util.getPlatform() == Util.OS.LINUX) {
                    APIUtil.apiCreateLibrary("libcuda.so")
                } else {
                    return CudaState.LIBRARY_UNAVAILABLE
                }

                PFN_cuInit = library.getFunctionAddress("cuInit")
                PFN_cuDeviceGetCount = library.getFunctionAddress("cuDeviceGetCount")
                PFN_cuDeviceComputeCapability = library.getFunctionAddress("cuDeviceComputeCapability")
                PFN_cuGetErrorName = library.getFunctionAddress("cuGetErrorName")
                PFN_cuGetErrorString = library.getFunctionAddress("cuGetErrorString")

                if (PFN_cuInit == MemoryUtil.NULL || PFN_cuDeviceGetCount == MemoryUtil.NULL || PFN_cuDeviceComputeCapability == MemoryUtil.NULL) {
                    //UnityTranslate.logger.info("CUDA results: $PFN_cuInit $PFN_cuDeviceGetCount $PFN_cuDeviceComputeCapability")
                    return CudaState.FUNCTION_UNAVAILABLE
                }
            } catch (_: UnsatisfiedLinkError) {
                UnityTranslate.logger.warn("CUDA library failed to load! Not attempting to initialize CUDA functions.")
                return CudaState.LIBRARY_UNAVAILABLE
            } catch (e: Throwable) {
                UnityTranslate.logger.warn("An error occurred while searching for CUDA devices! You don't have to report this, don't worry.")
                e.printStackTrace()
                return CudaState.EXCEPTION_THROWN
            }

            isLibraryLoaded = true
        }

        val success = 0

        if (JNI.callI(0, PFN_cuInit).apply {
                logCudaError(this, "init")
            } != success) {
            return CudaState.CUDA_FAILED
        }

        val totalPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())
        if (JNI.callPI(totalPtr, PFN_cuDeviceGetCount).apply {
                logCudaError(this, "get device count")
            } != success) {
            return CudaState.CUDA_FAILED
        }

        val totalCudaDevices = MemoryUtil.memGetInt(totalPtr)
        UnityTranslate.logger.info("Total CUDA devices: $totalCudaDevices")
        if (totalCudaDevices <= 0) {
            return CudaState.NO_CUDA_DEVICES
        }

        MemoryUtil.nmemFree(totalPtr)

        for (i in 0 until totalCudaDevices) {
            val minorPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())
            val majorPtr = MemoryUtil.nmemAlloc(Int.SIZE_BYTES.toLong())

            if (JNI.callPPI(majorPtr, minorPtr, i, PFN_cuDeviceComputeCapability).apply {
                    logCudaError(this, "get device compute capability $i")
                } != success) {
                continue
            }

            val majorVersion = MemoryUtil.memGetInt(majorPtr)
            val minorVersion = MemoryUtil.memGetInt(minorPtr)

            MemoryUtil.nmemFree(majorPtr)
            MemoryUtil.nmemFree(minorPtr)

            UnityTranslate.logger.info("Found device with CUDA compute capability major $majorVersion minor $minorVersion.")

            return CudaState.AVAILABLE
        }

        return CudaState.NO_CUDA_DEVICES
    }
}