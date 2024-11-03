package xyz.bluspring.unitytranslate.util.nativeaccess

object NativeAccess {
    fun isCudaSupported(): CudaState {
        LwjglLoader.tryLoadLwjgl()

        return CudaAccess.cudaState
    }
}