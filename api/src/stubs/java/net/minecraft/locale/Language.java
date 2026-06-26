package net.minecraft.locale;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public abstract class Language {
    public static Language getInstance() {
        throw new IllegalStateException();
    }

    public abstract FormattedCharSequence getVisualOrder(FormattedText text);
}
