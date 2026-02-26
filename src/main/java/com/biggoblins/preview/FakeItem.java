package com.biggoblins.preview;

import java.awt.Color;

/**
 * Represents a fake OSRS item for the preview inventory.
 */
public class FakeItem
{
    public enum Type { POTION, EFFECT, GEAR, FOOD, OTHER }

    public final int id;
    public final String name;
    public final Type type;
    public final Color color;       // icon placeholder colour
    public final String shortName;  // 3-char icon label

    // Potion/food effect fields
    public final int hpRestore;
    public final int prayerRestore;
    public final int specRestore;

    // Effect fields
    public final String effectName;
    public final int effectTicks;

    private FakeItem(Builder b)
    {
        this.id            = b.id;
        this.name          = b.name;
        this.type          = b.type;
        this.color         = b.color;
        this.shortName     = b.shortName;
        this.hpRestore     = b.hpRestore;
        this.prayerRestore = b.prayerRestore;
        this.specRestore   = b.specRestore;
        this.effectName    = b.effectName;
        this.effectTicks   = b.effectTicks;
    }

    public static Builder builder(int id, String name, Type type)
    {
        return new Builder(id, name, type);
    }

    public static class Builder
    {
        int id; String name; Type type;
        Color color = new Color(120, 120, 180);
        String shortName = "ITM";
        int hpRestore, prayerRestore, specRestore;
        String effectName; int effectTicks;

        Builder(int id, String name, Type type) { this.id = id; this.name = name; this.type = type; }
        public Builder color(Color c)           { this.color = c; return this; }
        public Builder shortName(String s)      { this.shortName = s; return this; }
        public Builder hp(int v)                { this.hpRestore = v; return this; }
        public Builder prayer(int v)            { this.prayerRestore = v; return this; }
        public Builder spec(int v)              { this.specRestore = v; return this; }
        public Builder effect(String n, int t)  { this.effectName = n; this.effectTicks = t; return this; }
        public FakeItem build()                 { return new FakeItem(this); }
    }
}
