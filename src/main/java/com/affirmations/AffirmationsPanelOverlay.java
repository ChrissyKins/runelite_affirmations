package com.affirmations;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;

public class AffirmationsPanelOverlay extends OverlayPanel {
    private final Client client;
    private final AffirmationsConfig config;
    private String currentAffirmation = "";
    private long lastUpdateTime = 0;

    @Inject
    private AffirmationsPanelOverlay(Client client, AffirmationsConfig config) {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.TOP_CENTER);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (currentAffirmation.isEmpty() || config.useFullWidthDisplay()) {
            return null;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastUpdateTime;
        int displayDuration = config.displayDuration() * 1000;
        int fadeDuration = config.fadeDuration();

        if (elapsedTime > displayDuration + fadeDuration * 2) {
            return null;
        }

        float alpha = calculateAlpha(elapsedTime, displayDuration, fadeDuration);
        Color color = new Color(
                config.textColor().getRed(),
                config.textColor().getGreen(),
                config.textColor().getBlue(),
                (int) (alpha * 255)
        );

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder()
                        .left(currentAffirmation)
                        .leftColor(color).build());

        return super.render(graphics);
    }

    public void setAffirmation(String affirmation) {
        this.currentAffirmation = affirmation;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    private float calculateAlpha(long elapsedTime, int displayDuration, int fadeDuration) {
        if (elapsedTime < fadeDuration) {
            return (float) elapsedTime / fadeDuration;
        } else if (elapsedTime > displayDuration + fadeDuration) {
            return 1 - (float) (elapsedTime - displayDuration - fadeDuration) / fadeDuration;
        } else {
            return 1;
        }
    }
}