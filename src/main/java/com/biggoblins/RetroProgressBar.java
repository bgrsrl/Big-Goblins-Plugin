package com.biggoblins;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * A custom retro-styled progress bar with pixel aesthetics,
 * inner glow, and text overlay.
 */
public class RetroProgressBar extends JComponent
{
	private static final Color TRACK_BG = new Color(15, 15, 15);
	private static final Color TRACK_BORDER_OUTER = new Color(10, 10, 10);
	private static final Color TRACK_BORDER_INNER = new Color(50, 50, 50);
	private static final Color TEXT_SHADOW = new Color(0, 0, 0, 200);

	private int value;
	private int maxValue;
	private Color barColorStart;
	private Color barColorEnd;
	private String label;
	private Font textFont;
	private boolean percentMode;

	public RetroProgressBar(String label, Color barColorStart, Color barColorEnd)
	{
		this.label = label;
		this.barColorStart = barColorStart;
		this.barColorEnd = barColorEnd;
		this.value = 0;
		this.maxValue = 1;
		this.textFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
		setPreferredSize(new Dimension(0, 24));
		setMinimumSize(new Dimension(50, 24));
	}

	public void update(int value, int maxValue)
	{
		this.value = value;
		this.maxValue = Math.max(maxValue, 1);
		repaint();
	}

	public void setPercentMode(boolean percentMode)
	{
		this.percentMode = percentMode;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g.create();
		int w = getWidth();
		int h = getHeight();

		// Outer border (dark inset)
		g2.setColor(TRACK_BORDER_OUTER);
		g2.fillRect(0, 0, w, h);

		// Inner border highlight (subtle top-left light)
		g2.setColor(TRACK_BORDER_INNER);
		g2.drawRect(1, 1, w - 3, h - 3);

		// Track background
		g2.setColor(TRACK_BG);
		g2.fillRect(2, 2, w - 4, h - 4);

		// Calculate fill
		float percent = Math.min(1.0f, (float) value / maxValue);
		int fillWidth = (int) ((w - 4) * percent);

		if (fillWidth > 0)
		{
			// Gradient bar fill
			GradientPaint gradient = new GradientPaint(
				2, 2, barColorStart,
				2, h - 4, barColorEnd
			);
			g2.setPaint(gradient);
			g2.fillRect(2, 2, fillWidth, h - 4);

			// Top highlight shine (inner glow)
			Color shine = new Color(255, 255, 255, 60);
			g2.setColor(shine);
			g2.fillRect(2, 2, fillWidth, (h - 4) / 3);

			// Bottom edge darkening
			Color bottomShade = new Color(0, 0, 0, 50);
			g2.setColor(bottomShade);
			g2.fillRect(2, h - 4, fillWidth, 2);

			// Pixel notches every 10% for retro feel
			g2.setColor(new Color(0, 0, 0, 40));
			for (int i = 1; i < 10; i++)
			{
				int notchX = 2 + (int) ((w - 4) * (i / 10.0));
				if (notchX < 2 + fillWidth)
				{
					g2.drawLine(notchX, 2, notchX, h - 3);
				}
			}
		}

		// Text overlay: "value / max"
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g2.setFont(textFont);
		String text;
		if (percentMode)
		{
			int pct = maxValue > 0 ? Math.round((float) value / maxValue * 100) : 0;
			text = pct + "%";
		}
		else
		{
			text = value + " / " + maxValue;
		}
		FontMetrics fm = g2.getFontMetrics();
		int textX = (w - fm.stringWidth(text)) / 2;
		int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

		// Text shadow
		g2.setColor(TEXT_SHADOW);
		g2.drawString(text, textX + 1, textY + 1);

		// Text foreground
		g2.setColor(Color.WHITE);
		g2.drawString(text, textX, textY);

		g2.dispose();
	}
}
