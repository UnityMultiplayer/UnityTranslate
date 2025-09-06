package xyz.bluspring.unitytranslate.gui.transcriber.browser

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.openqa.selenium.os.ExternalProcess
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.service.DriverCommandExecutor
import org.openqa.selenium.remote.service.DriverService

object BrowserNativeHandles {
    private fun getProcess(driver: RemoteWebDriver): Process? {
        val serviceField = DriverCommandExecutor::class.java.getDeclaredField("service")
        serviceField.isAccessible = true

        val service = serviceField.get(driver.commandExecutor) as DriverService

        val externalProcessField = DriverService::class.java.getDeclaredField("process")
        externalProcessField.isAccessible = true

        val externalProcess = externalProcessField.get(service) as ExternalProcess

        val processField = ExternalProcess::class.java.getDeclaredField("process")
        processField.isAccessible = true

        return processField.get(externalProcess) as Process?
    }

    fun makeGameParentWindows(driver: RemoteWebDriver) {
        val process = getProcess(driver) ?: return
        val javaProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, true, Kernel32.INSTANCE.GetCurrentProcessId())
        val webProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, true, process.pid().toInt())

        User32.INSTANCE.SetParent(WinDef.HWND(webProcess.pointer), WinDef.HWND(javaProcess.pointer))
    }

    fun makeGameParentOsx(driver: RemoteWebDriver) {

    }

    fun makeGameParentLinux(driver: RemoteWebDriver) {

    }

    fun killOlderDriverWindows() {
        ProcessHandle.allProcesses().filter { it.info().command().orElse("").endsWith("chromedriver.exe") }
            .forEach {
                it.destroyForcibly()
            }
    }
}