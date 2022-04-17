package com.falsepattern.crashguard;

import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.5.0,);")
public class CrashGuard {
    public static boolean softCrash = false;
    public static String[] crashHint = null;
    public static final Logger LOG = LogManager.getLogger(Tags.MODID);
}
