package net.minecraft.locale;

import net.minecraft.network.chat.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;

public abstract class Language {
    public static Language getInstance() {
        throw new IllegalStateException();
    }

    public abstract FormattedCharSequence getVisualOrder(FormattedText text);
}
