package com.biggoblins;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

public class BigGoblinsPanel extends PluginPanel
{
	private static final Color TITLE_GREEN = new Color(0, 200, 0);
	private static final Color SHADOW_COLOR = new Color(0, 0, 0, 180);
	private static final Color CONTENT_BG = new Color(25, 25, 25);
	private static final Color CONTENT_BORDER = new Color(55, 55, 55);
	private static final Color SECTION_HEADER = new Color(180, 180, 180);
	private static final Color LABEL_COLOR = new Color(200, 200, 200);
	private static final Color BOOST_COLOR = new Color(80, 255, 80);
	private static final Color DRAIN_COLOR = new Color(255, 80, 80);
	private static final Color TIMER_COLOR = new Color(255, 255, 100);

	// HP bar colors
	private static final Color HP_START = new Color(220, 40, 40);
	private static final Color HP_END = new Color(160, 20, 20);

	// Prayer bar colors
	private static final Color PRAYER_START = new Color(50, 200, 220);
	private static final Color PRAYER_END = new Color(30, 140, 170);

	// Spec bar colors
	private static final Color SPEC_START = new Color(240, 200, 40);
	private static final Color SPEC_END = new Color(200, 160, 20);

	// Opponent bar colors
	private static final Color OPP_HP_START = new Color(200, 60, 60);
	private static final Color OPP_HP_END = new Color(140, 30, 30);

	// Warning threshold — effects with fewer ticks than this pulse red
	private static final int WARNING_TICKS = 25;

	// Combat stat colors (reused in constructor + updates)
	private static final Color STAT_ATK = new Color(200, 60, 60);
	private static final Color STAT_STR = new Color(60, 200, 60);
	private static final Color STAT_DEF = new Color(100, 140, 220);
	private static final Color STAT_RNG = new Color(80, 180, 80);
	private static final Color STAT_MAG = new Color(120, 120, 220);

	// Fonts reused in dynamic updates (avoid recreating every tick)
	private static final Font EFFECT_TIMER_FONT = new Font(Font.MONOSPACED, Font.BOLD, 9);
	private static final Font EMPTY_ITALIC_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 10);
	private static final Color DIM_GRAY = new Color(100, 100, 100);

	private final JLabel memberCountLabel;
	private final RetroProgressBar hpBar;
	private final RetroProgressBar prayerBar;
	private final RetroProgressBar specBar;
	private final JLabel attackLabel;
	private final JLabel strengthLabel;
	private final JLabel defenceLabel;
	private final JLabel rangedLabel;
	private final JLabel magicLabel;
	private final JLabel opponentNameLabel;
	private final RetroProgressBar opponentHpBar;
	private final JPanel opponentSection;
	private final JPanel effectsContainer;
	private final JPanel effectsSection;
	private final Font retroFont;
	private final ItemManager itemManager;
	private final Timer animationTimer;
	private final java.util.HashMap<Integer, BufferedImage> scaledImageCache = new java.util.HashMap<>();

	// Game message section
	private final GameMessagePanel gameMessagePanel;
	private final JLabel messageLabel;

	public BigGoblinsPanel(ItemManager itemManager)
	{
		super(false);
		this.itemManager = itemManager;
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		retroFont = loadRetroFont();
		Font sectionFont = new Font(Font.SANS_SERIF, Font.BOLD, 11);

		// Load icons
		BufferedImage goblinImg = ImageUtil.loadImageResource(getClass(), "goblinicon.png");
		BufferedImage playersImg = ImageUtil.loadImageResource(getClass(), "players.png");
		BufferedImage hpIcon = ImageUtil.loadImageResource(getClass(), "hitpoints.png");
		BufferedImage prayerIcon = ImageUtil.loadImageResource(getClass(), "prayer.png");
		BufferedImage specIcon = ImageUtil.loadImageResource(getClass(), "special.png");
		BufferedImage attackIcon = ImageUtil.loadImageResource(getClass(), "attack.png");
		BufferedImage strengthIcon = ImageUtil.loadImageResource(getClass(), "strength.png");
		BufferedImage defenceIcon = ImageUtil.loadImageResource(getClass(), "defence.png");
		BufferedImage rangedIcon = ImageUtil.loadImageResource(getClass(), "ranged.png");
		BufferedImage magicIcon = ImageUtil.loadImageResource(getClass(), "magic.png");

		// Main vertical layout
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// ==========================================
		// TITLE ROW
		// ==========================================
		JPanel titleRow = new JPanel();
		titleRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		titleRow.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 0));

		if (goblinImg != null)
		{
			titleRow.add(new JLabel(new ImageIcon(goblinImg)));
		}
		titleRow.add(new ShadowLabel("Big Goblins", retroFont));
		mainPanel.add(titleRow);

		// Separator
		mainPanel.add(createSeparator());

		// ==========================================
		// MEMBERS BOX
		// ==========================================
		JPanel membersBox = createContentBox();
		membersBox.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));

		if (playersImg != null)
		{
			membersBox.add(new JLabel(new ImageIcon(playersImg)));
		}

		JLabel membersLabel = new JLabel("Members:");
		membersLabel.setForeground(Color.WHITE);
		membersLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		membersBox.add(membersLabel);

		memberCountLabel = new JLabel("--");
		memberCountLabel.setForeground(TITLE_GREEN);
		memberCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		membersBox.add(memberCountLabel);

		mainPanel.add(wrapFull(membersBox));
		mainPanel.add(createSpacer(6));

		// ==========================================
		// GAME MESSAGE SECTION
		// ==========================================
		gameMessagePanel = new GameMessagePanel();
		gameMessagePanel.setLayout(new BoxLayout(gameMessagePanel, BoxLayout.Y_AXIS));
		gameMessagePanel.setBorder(new EmptyBorder(8, 10, 8, 10));

		JLabel msgHeader = new JLabel("GAME MESSAGE", SwingConstants.CENTER);
		msgHeader.setForeground(TITLE_GREEN);
		msgHeader.setFont(sectionFont);
		msgHeader.setAlignmentX(0.5f);
		msgHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
		gameMessagePanel.add(msgHeader);
		gameMessagePanel.add(createSpacer(6));

		messageLabel = new JLabel("<html><center>No messages</center></html>");
		messageLabel.setForeground(new Color(120, 120, 120));
		messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		messageLabel.setAlignmentX(0.5f);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		messageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
		gameMessagePanel.add(messageLabel);

		mainPanel.add(wrapFull(gameMessagePanel));
		mainPanel.add(createSpacer(6));

		// ==========================================
		// PLAYER VITALS SECTION
		// ==========================================
		JPanel vitalsSection = createContentBox();
		vitalsSection.setLayout(new BoxLayout(vitalsSection, BoxLayout.Y_AXIS));

		JLabel vitalsHeader = new JLabel("PLAYER VITALS", SwingConstants.CENTER);
		vitalsHeader.setForeground(TITLE_GREEN);
		vitalsHeader.setFont(sectionFont);
		vitalsHeader.setAlignmentX(0.5f);
		vitalsHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
		vitalsSection.add(vitalsHeader);
		vitalsSection.add(createSpacer(6));

		hpBar = new RetroProgressBar("HP", HP_START, HP_END);
		vitalsSection.add(createBarRow(hpIcon, "HP", hpBar));
		vitalsSection.add(createSpacer(4));

		prayerBar = new RetroProgressBar("Prayer", PRAYER_START, PRAYER_END);
		vitalsSection.add(createBarRow(prayerIcon, "Prayer", prayerBar));
		vitalsSection.add(createSpacer(4));

		specBar = new RetroProgressBar("Spec", SPEC_START, SPEC_END);
		vitalsSection.add(createBarRow(specIcon, "Spec", specBar));

		mainPanel.add(wrapFull(vitalsSection));
		mainPanel.add(createSpacer(6));

		// ==========================================
		// COMBAT STATS SECTION
		// ==========================================
		JPanel combatSection = createContentBox();
		combatSection.setLayout(new BoxLayout(combatSection, BoxLayout.Y_AXIS));

		JLabel combatHeader = new JLabel("COMBAT", SwingConstants.CENTER);
		combatHeader.setForeground(TITLE_GREEN);
		combatHeader.setFont(sectionFont);
		combatHeader.setAlignmentX(0.5f);
		combatHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
		combatSection.add(combatHeader);
		combatSection.add(createSpacer(6));

		JPanel statsGrid = new JPanel(new GridLayout(5, 1, 0, 3));
		statsGrid.setBackground(CONTENT_BG);

		attackLabel = new JLabel("--");
		strengthLabel = new JLabel("--");
		defenceLabel = new JLabel("--");
		rangedLabel = new JLabel("--");
		magicLabel = new JLabel("--");

		statsGrid.add(createStatRow(attackIcon, "Atk", attackLabel, STAT_ATK));
		statsGrid.add(createStatRow(strengthIcon, "Str", strengthLabel, STAT_STR));
		statsGrid.add(createStatRow(defenceIcon, "Def", defenceLabel, STAT_DEF));
		statsGrid.add(createStatRow(rangedIcon, "Rng", rangedLabel, STAT_RNG));
		statsGrid.add(createStatRow(magicIcon, "Mag", magicLabel, STAT_MAG));

		combatSection.add(statsGrid);
		mainPanel.add(wrapFull(combatSection));
		mainPanel.add(createSpacer(6));

		// ==========================================
		// EFFECTS / TIMERS SECTION
		// ==========================================
		effectsSection = createContentBox();
		effectsSection.setLayout(new BoxLayout(effectsSection, BoxLayout.Y_AXIS));

		JLabel effectsHeader = new JLabel("EFFECTS", SwingConstants.CENTER);
		effectsHeader.setForeground(TITLE_GREEN);
		effectsHeader.setFont(sectionFont);
		effectsHeader.setAlignmentX(0.5f);
		effectsHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
		effectsSection.add(effectsHeader);
		effectsSection.add(createSpacer(4));

		effectsContainer = new JPanel();
		effectsContainer.setBackground(CONTENT_BG);
		effectsContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 3));

		JLabel noEffects = new JLabel("No active effects");
		noEffects.setForeground(new Color(100, 100, 100));
		noEffects.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
		effectsContainer.add(noEffects);

		effectsSection.add(effectsContainer);
		mainPanel.add(wrapFull(effectsSection));
		mainPanel.add(createSpacer(6));

		// ==========================================
		// OPPONENT SECTION
		// ==========================================
		opponentSection = createContentBox();
		opponentSection.setLayout(new BoxLayout(opponentSection, BoxLayout.Y_AXIS));

		JLabel oppHeader = new JLabel("TARGET", SwingConstants.CENTER);
		oppHeader.setForeground(TITLE_GREEN);
		oppHeader.setFont(sectionFont);
		oppHeader.setAlignmentX(0.5f);
		oppHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
		opponentSection.add(oppHeader);
		opponentSection.add(createSpacer(6));

		opponentNameLabel = new JLabel("No target", SwingConstants.CENTER);
		opponentNameLabel.setForeground(SECTION_HEADER);
		opponentNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		opponentNameLabel.setAlignmentX(0.5f);
		opponentNameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		opponentSection.add(opponentNameLabel);
		opponentSection.add(createSpacer(4));

		opponentHpBar = new RetroProgressBar("HP", OPP_HP_START, OPP_HP_END);
		opponentHpBar.setPercentMode(true);
		JPanel oppBarWrapper = new JPanel(new BorderLayout());
		oppBarWrapper.setBackground(CONTENT_BG);
		oppBarWrapper.add(opponentHpBar, BorderLayout.CENTER);
		oppBarWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		opponentSection.add(oppBarWrapper);

		mainPanel.add(wrapFull(opponentSection));

		add(mainPanel, BorderLayout.NORTH);

		// Animation timer for warning pulse + message glow (repaints every 80ms)
		animationTimer = new Timer(80, e ->
		{
			effectsContainer.repaint();
			gameMessagePanel.repaint();
		});
		animationTimer.start();
	}

	// === Public update methods ===

	public void dispose()
	{
		animationTimer.stop();
		scaledImageCache.clear();
	}

	public void updateMemberCount(int count)
	{
		memberCountLabel.setText(count < 0 ? "--" : String.valueOf(count));
	}

	public void updateHitpoints(int current, int max)
	{
		hpBar.update(current, max);
	}

	public void updatePrayer(int current, int max)
	{
		prayerBar.update(current, max);
	}

	public void updateSpecial(int percent)
	{
		specBar.update(percent, 100);
	}

	public void updateCombatStats(int atkB, int atkR, int strB, int strR,
								  int defB, int defR, int rngB, int rngR,
								  int magB, int magR)
	{
		updateStatLabel(attackLabel, atkB, atkR, STAT_ATK);
		updateStatLabel(strengthLabel, strB, strR, STAT_STR);
		updateStatLabel(defenceLabel, defB, defR, STAT_DEF);
		updateStatLabel(rangedLabel, rngB, rngR, STAT_RNG);
		updateStatLabel(magicLabel, magB, magR, STAT_MAG);
	}

	private void updateStatLabel(JLabel label, int boosted, int base, Color baseColor)
	{
		label.setText(boosted + "/" + base);
		if (boosted > base)
		{
			label.setForeground(BOOST_COLOR);
		}
		else if (boosted < base)
		{
			label.setForeground(DRAIN_COLOR);
		}
		else
		{
			label.setForeground(baseColor);
		}
	}

	public void updateOpponent(String name, int healthRatio, int healthScale)
	{
		if (name == null)
		{
			opponentNameLabel.setText("No target");
			opponentHpBar.update(0, 1);
		}
		else
		{
			opponentNameLabel.setText(name);
			opponentHpBar.update(healthRatio, healthScale);
		}
	}

	public void updateEffects(Map<String, int[]> effects)
	{
		effectsContainer.removeAll();

		if (effects.isEmpty())
		{
			JLabel noEffects = new JLabel("No active effects");
			noEffects.setForeground(DIM_GRAY);
			noEffects.setFont(EMPTY_ITALIC_FONT);
			effectsContainer.add(noEffects);
		}
		else
		{
			for (Map.Entry<String, int[]> entry : effects.entrySet())
			{
				int itemId = entry.getValue()[0];
				int ticks = entry.getValue()[1];

				int totalSeconds = (int) (ticks * 0.6);
				int minutes = totalSeconds / 60;
				int seconds = totalSeconds % 60;
				String timeStr = String.format("%d:%02d", minutes, seconds);

				boolean warning = ticks <= WARNING_TICKS;

				EffectTile effectTile = new EffectTile(warning);
				effectTile.setToolTipText(entry.getKey());

				// Cached scaled item icon
				BufferedImage scaled = getScaledItemImage(itemId);
				if (scaled != null)
				{
					JLabel iconLabel = new JLabel(new ImageIcon(scaled));
					iconLabel.setAlignmentX(0.5f);
					effectTile.add(iconLabel);
				}

				// Timer text
				JLabel timerLabel = new JLabel(timeStr);
				timerLabel.setForeground(warning ? Color.WHITE : TIMER_COLOR);
				timerLabel.setFont(EFFECT_TIMER_FONT);
				timerLabel.setAlignmentX(0.5f);
				effectTile.add(timerLabel);

				effectsContainer.add(effectTile);
			}
		}

		effectsContainer.revalidate();
		effectsContainer.repaint();
	}

	/**
	 * Updates the game message display. Pass null to clear.
	 */
	public void updateGameMessage(String message)
	{
		if (message == null)
		{
			messageLabel.setText("<html><center>No messages</center></html>");
			messageLabel.setForeground(new Color(120, 120, 120));
		}
		else
		{
			String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
			messageLabel.setText("<html><center>" + safe + "</center></html>");
			messageLabel.setForeground(new Color(220, 220, 220));
			gameMessagePanel.flash();
		}
	}

	// === Helper methods ===

	private BufferedImage getScaledItemImage(int itemId)
	{
		BufferedImage cached = scaledImageCache.get(itemId);
		if (cached != null)
		{
			return cached;
		}

		BufferedImage raw = itemManager.getImage(itemId);
		if (raw == null)
		{
			return null;
		}

		BufferedImage scaled = new BufferedImage(24, 22, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = scaled.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(raw, 0, 0, 24, 22, null);
		g2.dispose();

		scaledImageCache.put(itemId, scaled);
		return scaled;
	}

	private JPanel createBarRow(BufferedImage icon, String labelText, RetroProgressBar bar)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBackground(CONTENT_BG);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

		JPanel leftSide = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 0));
		leftSide.setBackground(CONTENT_BG);
		leftSide.setPreferredSize(new Dimension(65, 24));

		if (icon != null)
		{
			leftSide.add(new JLabel(new ImageIcon(icon)));
		}

		JLabel label = new JLabel(labelText);
		label.setForeground(LABEL_COLOR);
		label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		leftSide.add(label);

		row.add(leftSide, BorderLayout.WEST);
		row.add(bar, BorderLayout.CENTER);
		return row;
	}

	private JPanel createStatRow(BufferedImage icon, String name, JLabel valueLabel, Color valueColor)
	{
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setBackground(CONTENT_BG);

		JPanel leftSide = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
		leftSide.setBackground(CONTENT_BG);

		if (icon != null)
		{
			leftSide.add(new JLabel(new ImageIcon(icon)));
		}

		JLabel nameLabel = new JLabel(name);
		nameLabel.setForeground(LABEL_COLOR);
		nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		leftSide.add(nameLabel);

		row.add(leftSide, BorderLayout.WEST);

		valueLabel.setForeground(valueColor);
		valueLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
		valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		row.add(valueLabel, BorderLayout.EAST);

		return row;
	}

	private JPanel createContentBox()
	{
		JPanel box = new JPanel();
		box.setBackground(CONTENT_BG);
		box.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(CONTENT_BORDER, 1),
			new EmptyBorder(8, 10, 8, 10)
		));
		return box;
	}

	private JPanel wrapFull(JPanel inner)
	{
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.add(inner, BorderLayout.CENTER);
		return wrapper;
	}

	private JPanel createSeparator()
	{
		JSeparator sep = new JSeparator();
		sep.setForeground(new Color(80, 80, 80));
		sep.setBackground(ColorScheme.DARK_GRAY_COLOR);
		sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setBorder(new EmptyBorder(8, 0, 8, 0));
		wrapper.add(sep, BorderLayout.CENTER);
		return wrapper;
	}

	private JPanel createSpacer(int height)
	{
		JPanel spacer = new JPanel();
		spacer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		spacer.setPreferredSize(new Dimension(0, height));
		spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		return spacer;
	}

	private Font loadRetroFont()
	{
		try (InputStream is = getClass().getResourceAsStream("fonts/PressStart2P-Regular.ttf"))
		{
			if (is != null)
			{
				Font font = Font.createFont(Font.TRUETYPE_FONT, is);
				return font.deriveFont(Font.PLAIN, 14f);
			}
		}
		catch (FontFormatException | IOException e)
		{
			// Fall back
		}
		return new Font(Font.MONOSPACED, Font.BOLD, 14);
	}

	// ==========================================
	// Custom components
	// ==========================================

	/**
	 * Compact effect tile with pulsing red glow when about to expire.
	 */
	private static class EffectTile extends JPanel
	{
		private final boolean warning;

		EffectTile(boolean warning)
		{
			this.warning = warning;
			setOpaque(false);
			setBorder(new EmptyBorder(2, 3, 2, 3));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (warning)
			{
				double pulse = (Math.sin(System.currentTimeMillis() / 250.0) + 1) / 2;
				int r = (int) (40 + pulse * 80);
				int gb = (int) (20 - pulse * 10);
				g2.setColor(new Color(r, Math.max(0, gb), Math.max(0, gb)));
			}
			else
			{
				g2.setColor(new Color(35, 35, 35));
			}
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

			if (warning)
			{
				double pulse = (Math.sin(System.currentTimeMillis() / 250.0) + 1) / 2;
				int alpha = (int) (80 + pulse * 175);
				g2.setColor(new Color(255, 50, 50, alpha));
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 5, 5);
			}
			else
			{
				g2.setColor(new Color(60, 60, 60));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
			}

			g2.dispose();
			super.paintComponent(g);
		}
	}

	/**
	 * Game message panel with a green flash/glow that fades over time.
	 */
	private static class GameMessagePanel extends JPanel
	{
		private long lastFlashTime = 0;
		private static final long GLOW_DURATION_MS = 1500;

		GameMessagePanel()
		{
			setOpaque(false);
		}

		void flash()
		{
			lastFlashTime = System.currentTimeMillis();
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			long elapsed = System.currentTimeMillis() - lastFlashTime;
			double intensity = Math.max(0, 1.0 - (double) elapsed / GLOW_DURATION_MS);

			// Background — fades from green-tinted to dark
			int bgG = (int) (25 + intensity * 45);
			g2.setColor(new Color(25, bgG, 25));
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

			// Border — fades from bright green to default gray
			if (intensity > 0)
			{
				int alpha = (int) (intensity * 220);
				g2.setColor(new Color(0, 200, 0, alpha));
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 5, 5);
			}
			else
			{
				g2.setColor(CONTENT_BORDER);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
			}

			g2.dispose();
			super.paintComponent(g);
		}
	}

	/**
	 * Custom JLabel with retro shadow/outline text.
	 */
	private static class ShadowLabel extends JLabel
	{
		private final Font retroFont;

		ShadowLabel(String text, Font font)
		{
			super(text);
			this.retroFont = font;
			setFont(font);
			setForeground(TITLE_GREEN);
			setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Dimension getPreferredSize()
		{
			FontMetrics fm = getFontMetrics(retroFont);
			int width = fm.stringWidth(getText()) + 6;
			int height = fm.getHeight() + 6;
			return new Dimension(width, height);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g2.setFont(retroFont);

			FontMetrics fm = g2.getFontMetrics();
			int x = (getWidth() - fm.stringWidth(getText())) / 2;
			int y = fm.getAscent() + (getHeight() - fm.getHeight()) / 2;

			g2.setColor(SHADOW_COLOR);
			for (int dx = -2; dx <= 2; dx++)
			{
				for (int dy = -2; dy <= 2; dy++)
				{
					if (dx != 0 || dy != 0)
					{
						g2.drawString(getText(), x + dx, y + dy);
					}
				}
			}

			g2.setColor(TITLE_GREEN);
			g2.drawString(getText(), x, y);
			g2.dispose();
		}
	}
}
