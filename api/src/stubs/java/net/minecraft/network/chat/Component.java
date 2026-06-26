package net.minecraft.network.chat;

public interface Component extends FormattedText {
    static MutableComponent literal(String text) {
        throw new IllegalStateException();
    }
}
