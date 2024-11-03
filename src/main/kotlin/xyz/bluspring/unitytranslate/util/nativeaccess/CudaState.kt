package xyz.bluspring.unitytranslate.util.nativeaccess

enum class CudaState(val message: String? = null) {
    AVAILABLE,
    LIBRARY_UNAVAILABLE("Failed to locate CUDA library (not using NVIDIA GPU?)"),
    EXCEPTION_THROWN("An error occurred while trying to enumerate devices"),
    FUNCTION_UNAVAILABLE("Failed to locate one or more CUDA functions"),
    CUDA_FAILED("A CUDA error occurred in one or more positions"),
    NO_CUDA_DEVICES("No CUDA-supported devices were found")
}