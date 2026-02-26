package com.biggoblins;

import com.biggoblins.preview.FakeInventory;
import com.biggoblins.preview.FakeItem;
import com.biggoblins.preview.Loadouts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standalone preview launcher — fake OSRS game + BigGoblinsPanel side-by-side.
 *
 * HOW TO RUN IN INTELLIJ:
 *   Right-click PanelPreview.java → Run 'PanelPreview.main()'
 */
public class PanelPreview
{
    // ── Simulated player state ────────────────────────────────────
    private static int hp      = 87;
    private static int maxHp   = 99;
    private static int prayer  = 43;
    private static int maxPray = 77;
    private static int spec    = 75;

    private static final Map<String, int[]> activeEffects = new LinkedHashMap<>();
    // activeEffects: name → [itemId, ticks]

    private static BigGoblinsPanel panel;
    private static JLabel statusLabel;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            panel = new BigGoblinsPanel(PanelPreview::stubItemImage);

            // Initial state
            panel.updateMemberCount(12);
            refreshVitals();
            panel.updateCombatStats(99, 99, 99, 99, 99, 99, 99, 99, 99, 99);
            panel.updateOpponent("Zulrah", 180, 255);
            panel.updateGameMessage("Click an item to use it!");
            panel.updateEffects(activeEffects);

            // ── Build the window ──────────────────────────────────────
            JFrame frame = new JFrame("Big Goblins — Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(new Color(30, 27, 22));
            frame.setLayout(new BorderLayout(8, 0));

            // Left: fake inventory area
            JPanel leftPanel = buildInventoryPanel();
            frame.add(leftPanel, BorderLayout.WEST);

            // Right: the actual plugin panel
            JScrollPane scroll = new JScrollPane(panel);
            scroll.setPreferredSize(new Dimension(242, 720));
            scroll.setBorder(null);
            scroll.setBackground(new Color(40, 40, 40));
            scroll.getVerticalScrollBar().setUnitIncrement(8);
            frame.add(scroll, BorderLayout.CENTER);

            // Bottom: status bar
            statusLabel = new JLabel("  Ready — select a loadout and click items to test");
            statusLabel.setForeground(new Color(180, 180, 180));
            statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
            statusLabel.setBackground(new Color(20, 18, 14));
            statusLabel.setOpaque(true);
            frame.add(statusLabel, BorderLayout.SOUTH);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // ── Inventory + loadout selector ─────────────────────────────
    private static JPanel buildInventoryPanel()
    {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(new Color(30, 27, 22));
        wrapper.setBorder(new EmptyBorder(8, 8, 8, 4));

        // Loadout selector buttons
        JPanel loadoutBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        loadoutBar.setBackground(new Color(30, 27, 22));

        FakeInventory inventory = new FakeInventory(PanelPreview::onItemUsed);

        JButton pvpBtn  = loadoutButton("PvP",     () -> inventory.loadItems(Loadouts.pvp()));
        JButton pvmBtn  = loadoutButton("PvM",     () -> inventory.loadItems(Loadouts.pvm()));
        JButton skilBtn = loadoutButton("Skilling",() -> inventory.loadItems(Loadouts.skilling()));
        JButton clrBtn  = loadoutButton("Clear",   () ->
        {
            activeEffects.clear();
            panel.updateEffects(activeEffects);
            hp = 99; prayer = 77; spec = 100;
            refreshVitals();
            status("Stats reset.");
        });

        loadoutBar.add(pvpBtn);
        loadoutBar.add(pvmBtn);
        loadoutBar.add(skilBtn);
        loadoutBar.add(clrBtn);

        // Title
        JLabel title = new JLabel("INVENTORY", SwingConstants.CENTER);
        title.setForeground(new Color(200, 180, 120));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        title.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(30, 27, 22));
        top.add(title, BorderLayout.NORTH);
        top.add(loadoutBar, BorderLayout.CENTER);

        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(inventory, BorderLayout.CENTER);

        // Load PvM by default
        inventory.loadItems(Loadouts.pvm());

        return wrapper;
    }

    private static JButton loadoutButton(String label, Runnable action)
    {
        JButton btn = new JButton(label);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        btn.setForeground(new Color(220, 200, 140));
        btn.setBackground(new Color(60, 53, 42));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 90, 60), 1),
            new EmptyBorder(3, 8, 3, 8)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ── Item use handler ─────────────────────────────────────────
    private static void onItemUsed(FakeItem item)
    {
        StringBuilder msg = new StringBuilder("Used: ").append(item.name);

        // Restore HP
        if (item.hpRestore > 0)
        {
            hp = Math.min(maxHp, hp + item.hpRestore);
            msg.append("  +").append(item.hpRestore).append(" HP");
        }

        // Restore Prayer
        if (item.prayerRestore > 0)
        {
            prayer = Math.min(maxPray, prayer + item.prayerRestore);
            msg.append("  +").append(item.prayerRestore).append(" Prayer");
        }

        // Restore Spec
        if (item.specRestore > 0)
        {
            spec = Math.min(100, spec + item.specRestore);
            msg.append("  +").append(item.specRestore).append("% Spec");
        }

        // Add effect
        if (item.effectName != null)
        {
            activeEffects.put(item.effectName, new int[]{item.id, item.effectTicks});
            panel.updateEffects(activeEffects);
            msg.append("  → ").append(item.effectName).append(" active");
        }

        refreshVitals();
        panel.updateGameMessage(msg.toString());
        status(msg.toString());
    }

    private static void refreshVitals()
    {
        panel.updateHitpoints(hp, maxHp);
        panel.updatePrayer(prayer, maxPray);
        panel.updateSpecial(spec);
    }

    private static void status(String text)
    {
        if (statusLabel != null)
            statusLabel.setText("  " + text);
    }

    /** Coloured placeholder for item icons (no RuneLite needed). */
    private static BufferedImage stubItemImage(int itemId)
    {
        BufferedImage img = new BufferedImage(24, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        int hue = (itemId * 47) % 360;
        g.setColor(Color.getHSBColor(hue / 360f, 0.6f, 0.7f));
        g.fillRoundRect(2, 2, 20, 18, 4, 4);
        g.setColor(new Color(255, 255, 255, 160));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 6));
        g.drawString("ITM", 4, 14);
        g.dispose();
        return img;
    }
}
