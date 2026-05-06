package xyz.bluspring.unitytranslate.shared

import java.net.URI

object OperatingSystem {
    enum class Type(val formatted: String) {
        WINDOWS("Windows") {
            override fun getOpenUriArguments(uri: URI): Array<String> {
                return arrayOf("rundll32", "url.dll,FileProtocolHandler", uri.toString())
            }
        },
        MAC("macOS") {
            override fun getOpenUriArguments(uri: URI): Array<String> {
                return arrayOf("open", uri.toString())
            }
        },
        LINUX("Linux"), OTHER("(unknown)");

        protected open fun getOpenUriArguments(uri: URI): Array<String> {
            return arrayOf(
                "xdg-open",
                if (uri.scheme == "file")
                    uri.toString().replace("file:", "file://")
                else
                    uri.toString()
            )
        }

        fun openUri(uri: URI) {
            val process = Runtime.getRuntime().exec(this.getOpenUriArguments(uri))
            process.inputStream.close()
            process.errorStream.close()
            process.outputStream.close()
        }
    }

    val type: Type = System.getProperty("os.name", "generic").lowercase().run {
        if (indexOf("mac") >= 0 || indexOf("darwin") >= 0)
            Type.MAC
        else if (indexOf("win") >= 0)
            Type.WINDOWS
        else if (indexOf("nux") >= 0)
            Type.LINUX
        else
            Type.OTHER
    }
}