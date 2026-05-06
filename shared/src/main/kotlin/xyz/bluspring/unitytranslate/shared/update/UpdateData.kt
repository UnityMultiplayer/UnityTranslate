package xyz.bluspring.unitytranslate.shared.update

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.download.URLProvider
import java.net.URI
import java.net.URL
import java.util.*

@JvmRecord
data class UpdateData(
    val version: String,
    val downloads: Map<String, List<DownloadInfo>>,
    val publishTime: Long,

    // UnityTranslate Standalone data
    val standaloneVersion: Optional<String>,
    val launcherVersion: Optional<String>,
) {
    @JvmRecord
    data class DownloadInfo(
        override val url: URL,
        val sha1: Optional<String>
    ) : URLProvider {
        companion object {
            val URL_CODEC: Codec<URL> = Codec.STRING.xmap(URI::create, URI::toString).xmap(URI::toURL, URL::toURI)
            val FULL_CODEC: Codec<DownloadInfo> = RecordCodecBuilder.create { instance ->
                instance.group(
                    URL_CODEC
                        .fieldOf("url")
                        .forGetter(DownloadInfo::url),
                    Codec.STRING.optionalFieldOf("sha1")
                        .forGetter(DownloadInfo::sha1)
                )
                    .apply(instance, ::DownloadInfo)
            }

            val CODEC: Codec<DownloadInfo> = Codec.withAlternative(
                FULL_CODEC,
                URL_CODEC.xmap({ DownloadInfo(it, Optional.empty()) }, DownloadInfo::url)
            )
        }
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<UpdateData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("version")
                    .forGetter(UpdateData::version),
                Codec.unboundedMap(Codec.STRING, DownloadInfo.CODEC.listOf())
                    .optionalFieldOf("downloads", emptyMap())
                    .forGetter(UpdateData::downloads),
                Codec.LONG.fieldOf("publish_time")
                    .forGetter(UpdateData::publishTime),
                Codec.STRING.optionalFieldOf("standalone_version")
                    .forGetter(UpdateData::standaloneVersion),
                Codec.STRING.optionalFieldOf("launcher_version")
                    .forGetter(UpdateData::launcherVersion),
            )
                .apply(instance, ::UpdateData)
        }
    }
}
