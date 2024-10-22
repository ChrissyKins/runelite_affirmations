package com.affirmations;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.events.ConfigChanged;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@PluginDescriptor(
		name = "Affirmations"
)
public class AffirmationsPlugin extends Plugin {
	private static final String CONFIG_GROUP = "affirmations";

	private static final List<String> STANDARD_AFFIRMATIONS = StandardAffirmations.AFFIRMATIONS;

	private static final List<String> OSRS_AFFIRMATIONS = OSRSAffirmations.AFFIRMATIONS;

	@Inject
	private Client client;

	@Inject
	private AffirmationsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AffirmationsPanelOverlay panelOverlay;

	@Inject
	private AffirmationsFullWidthOverlay fullWidthOverlay;

	private List<String> activeAffirmations;
	private Random random = new Random();
	private int tickCounter = 0;
	private boolean isPaused = false;
	private int pauseTickCounter = 0;

	@Override
	protected void startUp() {
		updateAffirmationsList();
		overlayManager.add(panelOverlay);
		overlayManager.add(fullWidthOverlay);
		updateAffirmation();
		updateAffirmationsList();
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(panelOverlay);
		overlayManager.remove(fullWidthOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(CONFIG_GROUP)) {
			return;
		}

		// List of keys that should trigger an affirmations list update
		switch (event.getKey()) {
			case "customAffirmations":
			case "useOnlyCustom":
			case "useStandardPack":
			case "useOSRSPack":
				updateAffirmationsList();
				// Update the current affirmation immediately
				tickCounter = config.displayDuration();
				break;
		}
	}

	private void updateAffirmationsList() {
		activeAffirmations = new ArrayList<>();

		// Add custom affirmations if enabled
		if (config.useOnlyCustom()) {
			String customAffirmations = config.customAffirmations().trim();
			if (!customAffirmations.isEmpty()) {
				activeAffirmations.addAll(Arrays.asList(customAffirmations.split(";")));
			}
			return;
		}

		// Add standard affirmations if enabled
		if (config.useStandardPack()) {
			activeAffirmations.addAll(STANDARD_AFFIRMATIONS);
		}

		// Add OSRS affirmations if enabled
		if (config.useOSRSPack()) {
			activeAffirmations.addAll(OSRS_AFFIRMATIONS);
		}

		// Add custom affirmations if any exist
		String customAffirmations = config.customAffirmations().trim();
		if (!customAffirmations.isEmpty()) {
			activeAffirmations.addAll(Arrays.asList(customAffirmations.split(";")));
		}

		// Remove any empty strings that might have been created by extra semicolons
		activeAffirmations.removeIf(String::isEmpty);
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (activeAffirmations.isEmpty()) {
			clearAffirmation();
			return;
		}

		if (isPaused) {
			pauseTickCounter++;
			if (pauseTickCounter >= config.pauseDuration()) {
				isPaused = false;
				pauseTickCounter = 0;
				updateAffirmation();
			}
		} else {
			tickCounter++;
			if (tickCounter >= config.displayDuration()) {
				tickCounter = 0;

				if (config.pauseDuration() > 0 && config.useFullWidthDisplay()) {
					clearAffirmation();
					isPaused = true;
				} else {
					updateAffirmation();
				}
			}
		}
	}

	private void updateAffirmation() {
		if (activeAffirmations.isEmpty()) {
			clearAffirmation();
			return;
		}
		String newAffirmation = activeAffirmations.get(random.nextInt(activeAffirmations.size()));
		if (config.useFullWidthDisplay()) {
			fullWidthOverlay.setAffirmation(newAffirmation);
		} else {
			panelOverlay.setAffirmation(newAffirmation);
		}
	}

	private void clearAffirmation() {
		if (config.useFullWidthDisplay()) {
			fullWidthOverlay.setAffirmation("");
		} else {
			panelOverlay.setAffirmation("");
		}
	}

	@Provides
	AffirmationsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(AffirmationsConfig.class);
	}
}