package com.biggoblins.preview;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fake OSRS-style inventory grid (4 cols x 7 rows = 28 slots).
 * Clicking an item fires the onItemUsed consumer.
 */
public class FakeInventory extends JPanel
{
    private static final int COLS      = 4;
    private static final int ROWS      = 7;
    private static final int SLOT_SIZE = 50;
    private static final Color BG      = new Color(60, 53, 42);       // OSRS inventory brown
    private static final Color SLOT_BG = new Color(75, 67, 53);
    private static final Color HOVER   = new Color(100, 90, 70);
    private static final Color SELECT  = new Color(200, 170, 80, 120);
    private static final Color BORDER  = new Color(40, 35, 28);

    private final SlotPanel[] slots = new SlotPanel[COLS * ROWS];
    private final Consumer<FakeItem> onItemUsed;
    private int selectedSlot = -1;

    public FakeInventory(Consumer<FakeItem> onItemUsed)
    {
        this.onItemUsed = onItemUsed;
        setBackground(BG);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 25, 18), 2),
            new EmptyBorder(4, 4, 4, 4)
        ));
        setLayout(new GridLayout(ROWS, COLS, 2, 2));

        for (int i = 0; i < COLS * ROWS; i++)
        {
            SlotPanel slot = new SlotPanel(i);
            slots[i] = slot;
            add(slot);
        }
    }

    public void loadItems(List<FakeItem> items)
    {
        selectedSlot = -1;
        for (int i = 0; i < COLS * ROWS; i++)
        {
            FakeItem item = (i < items.size()) ? items.get(i) : null;
            slots[i].setItem(item);
        }
        repaint();
    }

    // ── Slot panel ────────────────────────────────────────────────
    private class SlotPanel extends JPanel
    {
        private final int index;
        private FakeItem item;
        private boolean hovered;

        SlotPanel(int index)
        {
            this.index = index;
            setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
            setBackground(SLOT_BG);
            setOpaque(false);

            addMouseListener(new MouseAdapter()
            {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e)
                {
                    if (item == null) return;
                    selectedSlot = index;
                    repaint();
                    onItemUsed.accept(item);

                    // Flash selection off after 300ms
                    Timer t = new Timer(300, ev -> { selectedSlot = -1; repaint(); });
                    t.setRepeats(false);
                    t.start();
                }
            });
        }

        void setItem(FakeItem i) { this.item = i; repaint(); }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Slot background
            g2.setColor(hovered && item != null ? HOVER : SLOT_BG);
            g2.fillRect(0, 0, w, h);

            // Slot border
            g2.setColor(BORDER);
            g2.drawRect(0, 0, w - 1, h - 1);

            if (item != null)
            {
                // Draw coloured item icon
                drawItemIcon(g2, item, w, h);

                // Selection highlight
                if (selectedSlot == index)
                {
                    g2.setColor(SELECT);
                    g2.fillRect(0, 0, w, h);
                }

                // Item quantity dots on corner (visual flair)
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(w - 8, 2, 5, 5);
            }

            g2.dispose();
        }

        private void drawItemIcon(Graphics2D g2, FakeItem it, int w, int h)
        {
            // Draw a stylised item placeholder icon
            int iconW = 36, iconH = 32;
            int x = (w - iconW) / 2;
            int y = (h - iconH) / 2 - 2;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(x + 2, y + 2, iconW, iconH, 6, 6);

            // Main colour block
            g2.setColor(it.color);
            g2.fillRoundRect(x, y, iconW, iconH, 6, 6);

            // Highlight shine
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(x + 3, y + 2, iconW - 6, iconH / 2, 4, 4);

            // Short name text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (iconW - fm.stringWidth(it.shortName)) / 2;
            int ty = y + (iconH + fm.getAscent()) / 2 - 2;
            // shadow
            g2.setColor(new Color(0, 0, 0, 180));
            g2.drawString(it.shortName, tx + 1, ty + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(it.shortName, tx, ty);

            // Type indicator pip
            Color pipColor;
            switch (it.type)
            {
                case POTION: pipColor = new Color(100, 220, 100); break;
                case EFFECT: pipColor = new Color(220, 100, 100); break;
                case FOOD:   pipColor = new Color(220, 180, 80);  break;
                default:     pipColor = new Color(150, 150, 150); break;
            }
            g2.setColor(pipColor);
            g2.fillOval(x + 2, y + iconH - 8, 6, 6);
        }
    }
}
