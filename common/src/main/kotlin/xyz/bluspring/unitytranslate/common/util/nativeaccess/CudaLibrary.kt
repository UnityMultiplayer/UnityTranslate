package xyz.bluspring.unitytranslate.common.util.nativeaccess

import jnr.ffi.annotations.In
import jnr.ffi.byref.IntByReference
import jnr.ffi.byref.PointerByReference

interface CudaLibrary {
    fun cuInit(@In flags: Int): Int
    fun cuDeviceGetCount(count: IntByReference): Int
    fun cuDeviceComputeCapability(major: IntByReference, minor: IntByReference, @In device: Int): Int
    fun cuGetErrorName(@In error: Int, pStr: PointerByReference): Int
    fun cuGetErrorString(@In error: Int, pStr: PointerByReference): Int
}