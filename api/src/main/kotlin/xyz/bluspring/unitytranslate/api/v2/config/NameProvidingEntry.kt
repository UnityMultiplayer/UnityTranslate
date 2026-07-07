package xyz.bluspring.unitytranslate.api.v2.config

interface NameProvidingEntry : Comparable<NameProvidingEntry> {
    val serializedName: String

    override fun compareTo(other: NameProvidingEntry): Int {
        return this.serializedName.compareTo(other.serializedName)
    }
}
