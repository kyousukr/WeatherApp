package com.yourname.weather;

import java.awt.*;

/**
 * Central design system. All colors, fonts, and sizing constants live here.
 * Never hardcode visual values elsewhere — reference these tokens.
 */
public final class UITheme {

    // ── Background layers ────────────────────────────────────────────────────
    public static final Color BG_BASE        = new Color(0x080C12);
    public static final Color BG_SURFACE     = new Color(0x0F1520);
    public static final Color BG_CARD        = new Color(0x141C28);
    public static final Color BG_CARD_ALT    = new Color(0x192030);
    public static final Color BG_INPUT       = new Color(0x0D1421);
    public static final Color BG_STAT        = new Color(0x1A2436);
    public static final Color TITLEBAR_BG    = new Color(0x06090F);

    // ── Accent / interactive ─────────────────────────────────────────────────
    public static final Color ACCENT         = new Color(0x4D9EFF);
    public static final Color ACCENT_HOVER   = new Color(0x3080E8);
    public static final Color ACCENT_GLOW    = new Color(0x1B4A8A);
    public static final Color ACCENT_DIM     = new Color(0x1E3A5C);

    // ── Text ─────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(0xEEF4FF);
    public static final Color TEXT_SECONDARY = new Color(0x8DA4C0);
    public static final Color TEXT_MUTED     = new Color(0x4A5A70);
    public static final Color TEXT_ACCENT    = new Color(0x6CB8FF);

    // ── Borders / dividers ───────────────────────────────────────────────────
    public static final Color BORDER         = new Color(0x1E2D42);
    public static final Color BORDER_BRIGHT  = new Color(0x2A3F5A);

    // ── Status ───────────────────────────────────────────────────────────────
    public static final Color SUCCESS        = new Color(0x3DD68C);
    public static final Color ERROR          = new Color(0xFF5A5A);
    public static final Color WARNING        = new Color(0xF5A623);

    // ── Window controls ──────────────────────────────────────────────────────
    public static final Color BTN_MINIMIZE   = new Color(0x2A6ECC);
    public static final Color BTN_CLOSE      = new Color(0xCC3333);

    // ── Sizing constants ─────────────────────────────────────────────────────
    public static final int CORNER_RADIUS    = 22;
    public static final int CARD_RADIUS      = 18;
    public static final int INNER_RADIUS     = 12;
    public static final int SMALL_RADIUS     = 8;

    public static final int FIELD_HEIGHT     = 42;
    public static final int SEARCH_BTN_SIZE  = 42;
    public static final int WIN_BTN_SIZE     = 28;

    public static final int H_PAD            = 18;
    public static final int V_GAP            = 10;

    // ── Typography ───────────────────────────────────────────────────────────
    public static final Font FONT_WINDOW_TITLE = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font FONT_SECTION_HDR  = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_CITY        = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TEMP        = new Font("Segoe UI", Font.BOLD, 80);
    public static final Font FONT_TEMP_UNIT   = new Font("Segoe UI", Font.PLAIN, 32);
    public static final Font FONT_CONDITION   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_FEELS       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_VALUE  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_STAT_LABEL  = new Font("Segoe UI", Font.BOLD, 8);
    public static final Font FONT_DAY         = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_FORECAST_T  = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_STATUS      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_DATE        = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {}
}