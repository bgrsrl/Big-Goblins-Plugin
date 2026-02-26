package com.biggoblins.preview;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/**
 * Pre-built item loadouts for the preview inventory.
 */
public class Loadouts
{
    public static List<FakeItem> pvp()
    {
        return Arrays.asList(
            FakeItem.builder(2436, "Super Attack(4)",  FakeItem.Type.POTION).color(new Color(200, 80,  80 )).shortName("SAT").hp(0).prayer(0).spec(0).effect("Atk Boost", 300).build(),
            FakeItem.builder(2440, "Super Strength(4)",FakeItem.Type.POTION).color(new Color(220, 120, 40 )).shortName("SST").hp(0).prayer(0).spec(0).effect("Str Boost", 300).build(),
            FakeItem.builder(2442, "Super Defence(4)", FakeItem.Type.POTION).color(new Color(80,  120, 220)).shortName("SDF").hp(0).prayer(0).spec(0).effect("Def Boost", 300).build(),
            FakeItem.builder(2444, "Prayer Potion(4)", FakeItem.Type.POTION).color(new Color(80,  200, 220)).shortName("PRY").hp(0).prayer(32).spec(0).build(),
            FakeItem.builder(3024, "Super Restore(4)", FakeItem.Type.POTION).color(new Color(160, 200, 255)).shortName("RST").hp(0).prayer(38).spec(0).build(),
            FakeItem.builder(2434, "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).prayer(0).spec(0).build(),
            FakeItem.builder(385,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).prayer(0).spec(0).build(),
            FakeItem.builder(386,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).prayer(0).spec(0).build(),
            FakeItem.builder(3025, "Saradomin Brew(4)",FakeItem.Type.POTION).color(new Color(220, 180, 60 )).shortName("BRW").hp(16).prayer(0).spec(0).build(),
            FakeItem.builder(3026, "Saradomin Brew(4)",FakeItem.Type.POTION).color(new Color(220, 180, 60 )).shortName("BRW").hp(16).prayer(0).spec(0).build(),
            FakeItem.builder(3027, "Saradomin Brew(4)",FakeItem.Type.POTION).color(new Color(220, 180, 60 )).shortName("BRW").hp(16).prayer(0).spec(0).build(),
            FakeItem.builder(12695,"Ranging Potion(4)",FakeItem.Type.POTION).color(new Color(80,  180, 80 )).shortName("RNG").hp(0).prayer(0).spec(0).effect("Rng Boost", 300).build(),
            FakeItem.builder(5952, "Stamina Potion(4)",FakeItem.Type.POTION).color(new Color(255, 160, 80 )).shortName("STA").hp(0).prayer(0).spec(20).build(),
            FakeItem.builder(5953, "Stamina Potion(4)",FakeItem.Type.POTION).color(new Color(255, 160, 80 )).shortName("STA").hp(0).prayer(0).spec(20).build(),
            FakeItem.builder(9999, "Spec Restore",     FakeItem.Type.POTION).color(new Color(255, 220, 40 )).shortName("SPC").hp(0).prayer(0).spec(25).build(),
            FakeItem.builder(9998, "Spec Restore",     FakeItem.Type.POTION).color(new Color(255, 220, 40 )).shortName("SPC").hp(0).prayer(0).spec(25).build(),
            null, null, null, null, null, null, null, null  // empty slots
        );
    }

    public static List<FakeItem> pvm()
    {
        return Arrays.asList(
            FakeItem.builder(20996,"Overload(4)",      FakeItem.Type.EFFECT).color(new Color(200, 50,  50 )).shortName("OVL").effect("Overload", 500).build(),
            FakeItem.builder(20997,"Overload(4)",      FakeItem.Type.EFFECT).color(new Color(200, 50,  50 )).shortName("OVL").effect("Overload", 500).build(),
            FakeItem.builder(2444, "Prayer Potion(4)", FakeItem.Type.POTION).color(new Color(80,  200, 220)).shortName("PRY").prayer(32).build(),
            FakeItem.builder(2445, "Prayer Potion(4)", FakeItem.Type.POTION).color(new Color(80,  200, 220)).shortName("PRY").prayer(32).build(),
            FakeItem.builder(2446, "Prayer Potion(4)", FakeItem.Type.POTION).color(new Color(80,  200, 220)).shortName("PRY").prayer(32).build(),
            FakeItem.builder(2447, "Prayer Potion(4)", FakeItem.Type.POTION).color(new Color(80,  200, 220)).shortName("PRY").prayer(32).build(),
            FakeItem.builder(2436, "Super Attack(4)",  FakeItem.Type.EFFECT).color(new Color(200, 80,  80 )).shortName("SAT").effect("Atk Boost", 300).build(),
            FakeItem.builder(2440, "Super Strength(4)",FakeItem.Type.EFFECT).color(new Color(220, 120, 40 )).shortName("SST").effect("Str Boost", 300).build(),
            FakeItem.builder(12695,"Antifire(4)",      FakeItem.Type.EFFECT).color(new Color(255, 140, 0  )).shortName("AFR").effect("Antifire", 500).build(),
            FakeItem.builder(2434, "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(385,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(386,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(387,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(388,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(3024, "Super Restore(4)", FakeItem.Type.POTION).color(new Color(160, 200, 255)).shortName("RST").prayer(38).build(),
            FakeItem.builder(3025, "Super Restore(4)", FakeItem.Type.POTION).color(new Color(160, 200, 255)).shortName("RST").prayer(38).build(),
            FakeItem.builder(5952, "Stamina Potion(4)",FakeItem.Type.POTION).color(new Color(255, 160, 80 )).shortName("STA").spec(20).build(),
            FakeItem.builder(2454, "Antipoison(4)",    FakeItem.Type.EFFECT).color(new Color(80,  200, 80 )).shortName("APO").effect("Antipoison", 450).build(),
            null, null, null, null, null, null, null, null  // empty slots
        );
    }

    public static List<FakeItem> skilling()
    {
        return Arrays.asList(
            FakeItem.builder(2436, "Super Attack(4)",  FakeItem.Type.EFFECT).color(new Color(200, 80,  80 )).shortName("SAT").effect("Atk Boost", 300).build(),
            FakeItem.builder(2440, "Super Strength(4)",FakeItem.Type.EFFECT).color(new Color(220, 120, 40 )).shortName("SST").effect("Str Boost", 300).build(),
            FakeItem.builder(2442, "Super Defence(4)", FakeItem.Type.EFFECT).color(new Color(80,  120, 220)).shortName("SDF").effect("Def Boost", 300).build(),
            FakeItem.builder(2444, "Prayer Potion(4)", FakeItem.Type.POTION).color(new Color(80,  200, 220)).shortName("PRY").prayer(32).build(),
            FakeItem.builder(2434, "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(385,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(386,  "Shark",            FakeItem.Type.FOOD  ).color(new Color(80,  160, 220)).shortName("SHK").hp(20).build(),
            FakeItem.builder(5952, "Stamina Potion(4)",FakeItem.Type.POTION).color(new Color(255, 160, 80 )).shortName("STA").spec(20).build(),
            FakeItem.builder(5953, "Stamina Potion(4)",FakeItem.Type.POTION).color(new Color(255, 160, 80 )).shortName("STA").spec(20).build(),
            FakeItem.builder(12695,"Ranging Potion(4)",FakeItem.Type.POTION).color(new Color(80,  180, 80 )).shortName("RNG").effect("Rng Boost", 300).build(),
            FakeItem.builder(2454, "Antipoison(4)",    FakeItem.Type.EFFECT).color(new Color(80,  200, 80 )).shortName("APO").effect("Antipoison", 450).build(),
            FakeItem.builder(9999, "Spec Restore",     FakeItem.Type.POTION).color(new Color(255, 220, 40 )).shortName("SPC").spec(25).build(),
            null, null, null, null, null, null, null, null, null, null, null, null  // empty slots
        );
    }
}
