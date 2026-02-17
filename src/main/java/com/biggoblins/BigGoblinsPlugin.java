package com.biggoblins;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClanChannelChanged;
import net.runelite.api.events.ClanMemberJoined;
import net.runelite.api.events.ClanMemberLeft;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Big Goblins",
	description = "Big Goblins clan plugin",
	tags = {"goblins", "panel", "clan", "combat"}
)
public class BigGoblinsPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(BigGoblinsPlugin.class);

	// How many ticks with no interaction before clearing target (~10 seconds)
	private static final int OPPONENT_TIMEOUT_TICKS = 16;

	// =====================================================================
	// Varbit IDs
	// =====================================================================
	private static final int VARBIT_ANTIFIRE = 3981;
	private static final int VARBIT_SUPER_ANTIFIRE = 6101;
	private static final int VARBIT_STAMINA_ACTIVE = 25;
	private static final int VARBIT_STAMINA_DURATION = 24;
	private static final int VARBIT_DIVINE_COMBAT = 13663;
	private static final int VARBIT_DIVINE_BASTION = 13664;
	private static final int VARBIT_DIVINE_BATTLEMAGE = 13665;
	private static final int VARBIT_DIVINE_ATTACK = 8429;
	private static final int VARBIT_DIVINE_STRENGTH = 8430;
	private static final int VARBIT_DIVINE_DEFENCE = 8431;
	private static final int VARBIT_DIVINE_RANGE = 8432;
	private static final int VARBIT_DIVINE_MAGIC = 8433;
	private static final int VARBIT_OVERLOAD_NMZ = 3955;
	private static final int VARBIT_OVERLOAD_COX = 5418;
	private static final int VARBIT_IMBUED_HEART = 5361;
	private static final int VARBIT_PRAYER_REGEN = 11361;
	private static final int VARBIT_SMELLING_SALTS = 14344;
	private static final int VARBIT_MENAPHITE_REMEDY = 14448;
	private static final int VARBIT_LIQUID_ADRENALINE_ACTIVE = 14361;

	// =====================================================================
	// Item IDs for icons
	// =====================================================================
	private static final int ITEM_ANTIFIRE = 2452;
	private static final int ITEM_SUPER_ANTIFIRE = 21978;
	private static final int ITEM_STAMINA = 12625;
	private static final int ITEM_DIVINE_SUPER_COMBAT = 23685;
	private static final int ITEM_DIVINE_BASTION = 24635;
	private static final int ITEM_DIVINE_BATTLEMAGE = 24623;
	private static final int ITEM_DIVINE_ATTACK = 23697;
	private static final int ITEM_DIVINE_STRENGTH = 23709;
	private static final int ITEM_DIVINE_DEFENCE = 23721;
	private static final int ITEM_DIVINE_RANGE = 23733;
	private static final int ITEM_DIVINE_MAGIC = 23745;
	private static final int ITEM_OVERLOAD_NMZ = 11730;
	private static final int ITEM_OVERLOAD_COX = 20996;
	private static final int ITEM_IMBUED_HEART = 20724;
	private static final int ITEM_PRAYER_REGEN = 30125;
	private static final int ITEM_SMELLING_SALTS = 27343;
	private static final int ITEM_MENAPHITE_REMEDY = 27202;
	private static final int ITEM_PRAYER_ENHANCE_COX = 20972;
	private static final int ITEM_SILK_DRESSING = 27323;
	private static final int ITEM_CRYSTAL_SCARAB = 27335;
	private static final int ITEM_LIQUID_ADRENALINE = 27339;

	// =====================================================================
	// Chat-based effect durations (game ticks)
	// =====================================================================
	private static final int PRAYER_ENHANCE_TICKS = 483;
	private static final int SILK_DRESSING_TICKS = 100;
	private static final int CRYSTAL_SCARAB_TICKS = 40;
	private static final int LIQUID_ADRENALINE_TICKS = 250;

	// =====================================================================
	// Thrall tracking
	// =====================================================================
	private static final int VARBIT_ARCEUUS_RESURRECTION = 12330;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	private NavigationButton navButton;
	private BigGoblinsPanel panel;

	// Chat-message-based effect timers
	private final Map<String, int[]> chatBasedEffects = new LinkedHashMap<>();

	// Smooth countdown tracking
	private final Map<String, Integer> lastVarbitRaw = new LinkedHashMap<>();
	private final Map<String, int[]> smoothEffects = new LinkedHashMap<>();

	// Sticky opponent tracking
	private Actor lastOpponent;
	private int lastOpponentTick;
	private String lastOpponentName;
	private int lastOpponentRatio;
	private int lastOpponentScale;

	// Thrall tracking
	private String thrallType = null;
	private int thrallTicksRemaining = 0;

	@Override
	protected void startUp() throws Exception
	{
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "goblinicon.png");

		panel = new BigGoblinsPanel(itemManager);

		navButton = NavigationButton.builder()
			.tooltip("Big Goblins")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		log.info("Big Goblins plugin started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		chatBasedEffects.clear();
		smoothEffects.clear();
		lastVarbitRaw.clear();
		thrallType = null;
		thrallTicksRemaining = 0;
		clearOpponent();
		panel.dispose();
		log.info("Big Goblins plugin stopped");
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		updatePlayerVitals();
		updateCombatStats();
		updateOpponent();
		updateEffectTimers();
		updateThrall();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			updateMemberCount();
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			chatBasedEffects.clear();
			smoothEffects.clear();
			lastVarbitRaw.clear();
			thrallType = null;
			thrallTicksRemaining = 0;
			clearOpponent();
			SwingUtilities.invokeLater(() ->
			{
				panel.updateMemberCount(-1);
				panel.updateHitpoints(0, 1);
				panel.updatePrayer(0, 1);
				panel.updateSpecial(0);
				panel.updateCombatStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
				panel.updateOpponent(null, 0, 1);
				panel.updateEffects(new LinkedHashMap<>());
				panel.updateThrall(null, 0);
				panel.updateGameMessage(null);
			});
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		// Effect tracking (SPAM + GAMEMESSAGE)
		if (event.getType() == ChatMessageType.SPAM || event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			String msg = event.getMessage();

			if (msg.contains("You drink some of your") && msg.contains("prayer enhance"))
			{
				chatBasedEffects.put("Prayer Enhance", new int[]{ITEM_PRAYER_ENHANCE_COX, PRAYER_ENHANCE_TICKS});
			}
			else if (msg.contains("Your prayer enhance effect has worn off"))
			{
				chatBasedEffects.remove("Prayer Enhance");
			}

			if (msg.contains("You quickly apply the dressing to your wounds"))
			{
				chatBasedEffects.put("Silk Dressing", new int[]{ITEM_SILK_DRESSING, SILK_DRESSING_TICKS});
			}

			if (msg.contains("You crack the crystal in your hand"))
			{
				chatBasedEffects.put("Crystal Scarab", new int[]{ITEM_CRYSTAL_SCARAB, CRYSTAL_SCARAB_TICKS});
			}

			if (msg.contains("You drink some of the potion, reducing the energy cost"))
			{
				chatBasedEffects.put("Liquid Adrenaline", new int[]{ITEM_LIQUID_ADRENALINE, LIQUID_ADRENALINE_TICKS});
			}

			// Thrall detection
			if (msg.contains("resurrect a") && msg.contains("thrall"))
			{
				String lower = msg.toLowerCase();
				if (lower.contains("ghost"))
				{
					thrallType = "Ghost";
				}
				else if (lower.contains("skeleton"))
				{
					thrallType = "Skeleton";
				}
				else if (lower.contains("zombie"))
				{
					thrallType = "Zombie";
				}
				else
				{
					thrallType = "Thrall";
				}
				thrallTicksRemaining = client.getBoostedSkillLevel(Skill.MAGIC);
			}
		}

		// Game message feed (GAMEMESSAGE only)
		if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			String clean = event.getMessage().replaceAll("<[^>]*>", "").trim();
			if (!clean.isEmpty())
			{
				SwingUtilities.invokeLater(() -> panel.updateGameMessage(clean));
			}
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (event.getActor() == lastOpponent || event.getActor() == client.getLocalPlayer())
		{
			clearOpponent();
			SwingUtilities.invokeLater(() -> panel.updateOpponent(null, 0, 1));
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() == client.getLocalPlayer())
		{
			Actor target = client.getLocalPlayer().getInteracting();
			if (target != null)
			{
				lastOpponent = target;
				lastOpponentTick = client.getTickCount();
			}
		}
	}

	@Subscribe
	public void onClanChannelChanged(ClanChannelChanged event)
	{
		updateMemberCount();
	}

	@Subscribe
	public void onClanMemberJoined(ClanMemberJoined event)
	{
		updateMemberCount();
	}

	@Subscribe
	public void onClanMemberLeft(ClanMemberLeft event)
	{
		updateMemberCount();
	}

	private void updateMemberCount()
	{
		ClanSettings clanSettings = client.getClanSettings();
		int count = clanSettings != null ? clanSettings.getMembers().size() : -1;
		SwingUtilities.invokeLater(() -> panel.updateMemberCount(count));
	}

	private void updatePlayerVitals()
	{
		int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
		int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
		int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
		int specPercent = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;

		SwingUtilities.invokeLater(() ->
		{
			panel.updateHitpoints(currentHp, maxHp);
			panel.updatePrayer(currentPrayer, maxPrayer);
			panel.updateSpecial(specPercent);
		});
	}

	private void updateCombatStats()
	{
		int atkBoosted = client.getBoostedSkillLevel(Skill.ATTACK);
		int atkBase = client.getRealSkillLevel(Skill.ATTACK);
		int strBoosted = client.getBoostedSkillLevel(Skill.STRENGTH);
		int strBase = client.getRealSkillLevel(Skill.STRENGTH);
		int defBoosted = client.getBoostedSkillLevel(Skill.DEFENCE);
		int defBase = client.getRealSkillLevel(Skill.DEFENCE);
		int rngBoosted = client.getBoostedSkillLevel(Skill.RANGED);
		int rngBase = client.getRealSkillLevel(Skill.RANGED);
		int magBoosted = client.getBoostedSkillLevel(Skill.MAGIC);
		int magBase = client.getRealSkillLevel(Skill.MAGIC);

		SwingUtilities.invokeLater(() -> panel.updateCombatStats(
			atkBoosted, atkBase, strBoosted, strBase,
			defBoosted, defBase, rngBoosted, rngBase,
			magBoosted, magBase
		));
	}

	private void updateOpponent()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			clearOpponent();
			SwingUtilities.invokeLater(() -> panel.updateOpponent(null, 0, 1));
			return;
		}

		// If we're currently interacting, refresh the tick timer
		Actor current = localPlayer.getInteracting();
		if (current != null)
		{
			if (current != lastOpponent)
			{
				// Switched to a new target
				lastOpponent = current;
			}
			lastOpponentTick = client.getTickCount();
		}

		if (lastOpponent != null)
		{
			// Read live health data
			String name = lastOpponent.getName();
			int ratio = lastOpponent.getHealthRatio();
			int scale = lastOpponent.getHealthScale();

			// Store latest valid name
			if (name != null)
			{
				lastOpponentName = name;
			}

			// Store latest visible health (ratio > 0 means health bar is showing)
			if (ratio > 0)
			{
				lastOpponentRatio = ratio;
				lastOpponentScale = scale;
			}
			else if (ratio == 0)
			{
				// Target died
				clearOpponent();
				SwingUtilities.invokeLater(() -> panel.updateOpponent(null, 0, 1));
				return;
			}
			// ratio == -1 means health bar hidden — keep showing last known values

			// Timeout: if not interacting for too long, clear
			if (current == null && client.getTickCount() - lastOpponentTick > OPPONENT_TIMEOUT_TICKS)
			{
				clearOpponent();
				SwingUtilities.invokeLater(() -> panel.updateOpponent(null, 0, 1));
				return;
			}

			final String displayName = lastOpponentName;
			final int displayRatio = lastOpponentRatio;
			final int displayScale = lastOpponentScale;
			SwingUtilities.invokeLater(() -> panel.updateOpponent(displayName, displayRatio, displayScale));
		}
		else
		{
			SwingUtilities.invokeLater(() -> panel.updateOpponent(null, 0, 1));
		}
	}

	private void clearOpponent()
	{
		lastOpponent = null;
		lastOpponentName = null;
		lastOpponentRatio = 0;
		lastOpponentScale = 1;
	}

	// =====================================================================
	// Effect timers
	// =====================================================================

	private void updateEffectTimers()
	{
		Map<String, int[]> effects = new LinkedHashMap<>();

		checkSmoothEffect(effects, "Antifire", VARBIT_ANTIFIRE, ITEM_ANTIFIRE, 30);
		checkSmoothEffect(effects, "S. Antifire", VARBIT_SUPER_ANTIFIRE, ITEM_SUPER_ANTIFIRE, 20);
		checkSmoothStamina(effects);

		boolean divineCombat = client.getVarbitValue(VARBIT_DIVINE_COMBAT) > 0;
		boolean divineBastion = client.getVarbitValue(VARBIT_DIVINE_BASTION) > 0;
		boolean divineBattlemage = client.getVarbitValue(VARBIT_DIVINE_BATTLEMAGE) > 0;

		checkSmoothEffect(effects, "Divine Combat", VARBIT_DIVINE_COMBAT, ITEM_DIVINE_SUPER_COMBAT, 1);
		checkSmoothEffect(effects, "Divine Bastion", VARBIT_DIVINE_BASTION, ITEM_DIVINE_BASTION, 1);
		checkSmoothEffect(effects, "Divine Mage", VARBIT_DIVINE_BATTLEMAGE, ITEM_DIVINE_BATTLEMAGE, 1);

		if (!divineCombat)
		{
			checkSmoothEffect(effects, "Divine Atk", VARBIT_DIVINE_ATTACK, ITEM_DIVINE_ATTACK, 1);
			checkSmoothEffect(effects, "Divine Str", VARBIT_DIVINE_STRENGTH, ITEM_DIVINE_STRENGTH, 1);
		}
		else
		{
			clearSmooth("Divine Atk");
			clearSmooth("Divine Str");
		}

		if (!divineCombat && !divineBastion && !divineBattlemage)
		{
			checkSmoothEffect(effects, "Divine Def", VARBIT_DIVINE_DEFENCE, ITEM_DIVINE_DEFENCE, 1);
		}
		else
		{
			clearSmooth("Divine Def");
		}

		if (!divineBastion)
		{
			checkSmoothEffect(effects, "Divine Rng", VARBIT_DIVINE_RANGE, ITEM_DIVINE_RANGE, 1);
		}
		else
		{
			clearSmooth("Divine Rng");
		}

		if (!divineBattlemage)
		{
			checkSmoothEffect(effects, "Divine Mag", VARBIT_DIVINE_MAGIC, ITEM_DIVINE_MAGIC, 1);
		}
		else
		{
			clearSmooth("Divine Mag");
		}

		checkSmoothEffect(effects, "Overload", VARBIT_OVERLOAD_NMZ, ITEM_OVERLOAD_NMZ, 25);
		checkSmoothEffect(effects, "Overload+", VARBIT_OVERLOAD_COX, ITEM_OVERLOAD_COX, 25);
		checkSmoothEffect(effects, "Smelling Salts", VARBIT_SMELLING_SALTS, ITEM_SMELLING_SALTS, 25);
		checkSmoothEffect(effects, "Menaphite Remedy", VARBIT_MENAPHITE_REMEDY, ITEM_MENAPHITE_REMEDY, 25);
		checkSmoothEffect(effects, "Imbued Heart", VARBIT_IMBUED_HEART, ITEM_IMBUED_HEART, 10);
		checkSmoothEffect(effects, "Prayer Regen", VARBIT_PRAYER_REGEN, ITEM_PRAYER_REGEN, 12);

		// Chat-based effects
		Iterator<Map.Entry<String, int[]>> it = chatBasedEffects.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, int[]> entry = it.next();
			entry.getValue()[1]--;
			if (entry.getValue()[1] <= 0)
			{
				it.remove();
			}
		}

		if (chatBasedEffects.containsKey("Liquid Adrenaline")
			&& client.getVarbitValue(VARBIT_LIQUID_ADRENALINE_ACTIVE) == 0)
		{
			chatBasedEffects.remove("Liquid Adrenaline");
		}

		effects.putAll(chatBasedEffects);

		SwingUtilities.invokeLater(() -> panel.updateEffects(effects));
	}

	private void checkSmoothEffect(Map<String, int[]> effects, String name,
									int varbitId, int itemId, int ticksPerUnit)
	{
		int raw = client.getVarbitValue(varbitId);
		if (raw <= 0)
		{
			clearSmooth(name);
			return;
		}

		Integer prevRaw = lastVarbitRaw.get(name);
		int[] prevData = smoothEffects.get(name);

		int smoothTicks;
		if (prevRaw != null && prevRaw == raw && prevData != null)
		{
			smoothTicks = Math.max(1, prevData[1] - 1);
		}
		else
		{
			smoothTicks = raw * ticksPerUnit;
		}

		lastVarbitRaw.put(name, raw);
		smoothEffects.put(name, new int[]{itemId, smoothTicks});
		effects.put(name, new int[]{itemId, smoothTicks});
	}

	private void checkSmoothStamina(Map<String, int[]> effects)
	{
		int active = client.getVarbitValue(VARBIT_STAMINA_ACTIVE);
		int duration = client.getVarbitValue(VARBIT_STAMINA_DURATION);
		if (active != 1 || duration <= 0)
		{
			clearSmooth("Stamina");
			return;
		}

		Integer prevRaw = lastVarbitRaw.get("Stamina");
		int[] prevData = smoothEffects.get("Stamina");

		int smoothTicks;
		if (prevRaw != null && prevRaw == duration && prevData != null)
		{
			smoothTicks = Math.max(1, prevData[1] - 1);
		}
		else
		{
			smoothTicks = duration * 10;
		}

		lastVarbitRaw.put("Stamina", duration);
		smoothEffects.put("Stamina", new int[]{ITEM_STAMINA, smoothTicks});
		effects.put("Stamina", new int[]{ITEM_STAMINA, smoothTicks});
	}

	private void clearSmooth(String name)
	{
		smoothEffects.remove(name);
		lastVarbitRaw.remove(name);
	}

	// =====================================================================
	// Thrall tracking
	// =====================================================================

	private void updateThrall()
	{
		if (thrallType != null)
		{
			if (client.getVarbitValue(VARBIT_ARCEUUS_RESURRECTION) == 0)
			{
				thrallType = null;
				thrallTicksRemaining = 0;
			}
			else
			{
				thrallTicksRemaining = Math.max(0, thrallTicksRemaining - 1);
				if (thrallTicksRemaining <= 0)
				{
					thrallType = null;
					thrallTicksRemaining = 0;
				}
			}
		}

		final String type = thrallType;
		final int ticks = thrallTicksRemaining;
		SwingUtilities.invokeLater(() -> panel.updateThrall(type, ticks));
	}
}
