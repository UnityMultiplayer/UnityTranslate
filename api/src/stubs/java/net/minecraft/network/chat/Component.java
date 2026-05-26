package net.minecraft.network.chat;

public interface Component {
    static MutableComponent literal(String text) {
        throw new IllegalStateException();
    }
}
