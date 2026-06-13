package com.encraft.dz.util;

import com.gtnewhorizon.gtnhlib.color.ColorResource;

public class ColorUtils {

    private static final ColorResource.Factory color = new ColorResource.Factory("ifu");

    public static final ColorResource
    // spotless:off
        title             = color.rgb("title",             "0x404040"),
        displayText       = color.rgb("displayText",       "0x404040"),
        blocklistWarning  = color.rgb("blocklistWarning",  "0x404040"),
        searchAvailable   = color.rgb("searchAvailable",   "0x404040"),
        searchUnavailable = color.rgb("searchUnavailable", "0xAA0000");
    // spotless:on
}
