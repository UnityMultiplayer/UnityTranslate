package xyz.bluspring.unitytranslate.common.network

interface PacketDefinitions {
    val packets: Set<PacketBuilder<*>>
}