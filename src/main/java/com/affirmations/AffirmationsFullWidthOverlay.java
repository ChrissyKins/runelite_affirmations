package com.affirmations;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

public class AffirmationsFullWidthOverlay extends Overlay {
    private final Client client;
    private final AffirmationsConfig config;
    private String currentAffirmation = "";
    private long lastUpdateTime = 0;
    private Font fallbackFont = new Font("Arial", Font.PLAIN, 20);

    private static final double TICK_LENGTH = 0.6;
    private static final int PADDING = 20; // Padding from screen edges

    @Inject
    private AffirmationsFullWidthOverlay(Client client, AffirmationsConfig config) {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(OverlayPriority.HIGHEST);
    }

    private Font createFont() {
        Font font = new Font(config.fontFamily().getFontName(),
                config.fontStyle().toAwtFontStyle(),
                config.fontSize());

        if (!font.getFamily().equalsIgnoreCase("Dialog")) {
            return font;
        }

        for (String fontName : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (fontName.toLowerCase().contains(config.fontFamily().getFontName().toLowerCase())) {
                return new Font(fontName, config.fontStyle().toAwtFontStyle(), config.fontSize());
            }
        }

        return fallbackFont;
    }

    private List<TextLayout> createWrappedTextLayouts(String text, Font font, FontRenderContext frc, float wrappingWidth) {
        List<TextLayout> layouts = new ArrayList<>();
        if (text.isEmpty()) return layouts;

        AttributedString attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FONT, font);
        AttributedCharacterIterator paragraph = attributedText.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(paragraph, frc);

        measurer.setPosition(paragraph.getBeginIndex());

        while (measurer.getPosition() < paragraph.getEndIndex()) {
            TextLayout layout = measurer.nextLayout(wrappingWidth);
            layouts.add(layout);
        }

        return layouts;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (currentAffirmation.isEmpty() || !config.useFullWidthDisplay()) {
            return null;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastUpdateTime;

        int displayDuration = (int)(config.displayDuration() * TICK_LENGTH * 1000);
        int fadeDuration = (int)(config.fadeDuration() * TICK_LENGTH * 1000);

        if (elapsedTime >= displayDuration + fadeDuration * 2) {
            return null;
        }

        float alpha = calculateAlpha(elapsedTime, displayDuration, fadeDuration);
        if (alpha <= 0) {
            return null;
        }

        Color textColor = new Color(
                config.textColor().getRed(),
                config.textColor().getGreen(),
                config.textColor().getBlue(),
                Math.max(0, Math.min(255, (int) (alpha * 255)))
        );
        Color strokeColor = new Color(
                config.strokeColor().getRed(),
                config.strokeColor().getGreen(),
                config.strokeColor().getBlue(),
                Math.max(0, Math.min(255, (int) (alpha * 255)))
        );

        Font font = createFont();
        Graphics2D g2d = (Graphics2D) graphics.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int canvasWidth = client.getCanvasWidth();
            int canvasHeight = client.getCanvasHeight();
            float wrappingWidth = canvasWidth - (PADDING * 2);

            List<TextLayout> layouts = createWrappedTextLayouts(currentAffirmation, font, g2d.getFontRenderContext(), wrappingWidth);

            if (layouts.isEmpty()) return null;

            float totalHeight = 0;
            for (TextLayout layout : layouts) {
                totalHeight += layout.getAscent() + layout.getDescent() + layout.getLeading();
            }

            float currentY = PADDING + layouts.get(0).getAscent();

            for (TextLayout layout : layouts) {
                float x = (float)(canvasWidth - layout.getBounds().getWidth()) / 2;

                AffineTransform transform = AffineTransform.getTranslateInstance(x, currentY);
                Shape shape = layout.getOutline(transform);

                g2d.setColor(strokeColor);
                g2d.setStroke(new BasicStroke(config.strokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(shape);

                g2d.setColor(textColor);
                g2d.fill(shape);

                currentY += layout.getAscent() + layout.getDescent() + layout.getLeading();
            }

            return new Dimension(canvasWidth, (int)totalHeight + PADDING * 2);
        } finally {
            g2d.dispose();
        }
    }

    public void setAffirmation(String affirmation) {
        this.currentAffirmation = affirmation;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    private float calculateAlpha(long elapsedTime, int displayDuration, int fadeDuration) {
        if (elapsedTime < fadeDuration) {
            return (float) elapsedTime / fadeDuration;
        }

        if (elapsedTime < displayDuration - fadeDuration) {
            return 1.0f;
        }

        long fadeOutStart = displayDuration - fadeDuration;
        long fadeOutProgress = elapsedTime - fadeOutStart;
        if (fadeOutProgress < fadeDuration) {
            return 1.0f - (float) fadeOutProgress / fadeDuration;
        }

        return 0.0f;
    }
}