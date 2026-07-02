package net.minecraft.locale;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public abstract class Language {
    public static Language getInstance() {
        throw new IllegalStateException();
    }

    public abstract String getOrDefault(String id);
    public abstract String getOrDefault(String id, String fallback);
    public abstract FormattedCharSequence getVisualOrder(FormattedText text);
}
