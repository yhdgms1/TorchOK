package dev.yhdgms1.torchok.client;

import eu.midnightdust.lib.config.MidnightConfig;

public class ClientConfig extends MidnightConfig {
    @Entry()
    public static boolean enabled = true;

    @Entry(min = 0, max = 15, isSlider = true)
    public static int threshold = 4;
}
