package com.affirmations;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import java.awt.Color;

@ConfigGroup("affirmations")
public interface AffirmationsConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings for affirmations",
			position = 0
	)
	String generalSection = "general";

	@ConfigSection(
			name = "Affirmation Packs",
			description = "Configure which affirmation packs to use",
			position = 1
	)
	String packsSection = "packs";

	@ConfigSection(
			name = "Custom Affirmations",
			description = "Settings for custom affirmations",
			position = 2
	)
	String customSection = "custom";

	@ConfigSection(
			name = "Appearance",
			description = "Settings for the display mode",
			position = 3
	)
	String appearanceSection = "appearance";

	// Affirmation Pack Settings
	@ConfigItem(
			keyName = "useStandardPack",
			name = "Standard Affirmations",
			description = "Enable the standard affirmations pack",
			section = packsSection,
			position = 0
	)
	default boolean useStandardPack()
	{
		return true;
	}

	@ConfigItem(
			keyName = "useOSRSPack",
			name = "OSRS Style Affirmations",
			description = "Enable OSRS themed affirmations",
			section = packsSection,
			position = 1
	)
	default boolean useOSRSPack()
	{
		return true;
	}

	// Custom Affirmations Settings
	@ConfigItem(
			keyName = "customAffirmations",
			name = "Custom Affirmations",
			description = "A list of custom affirmations to display, separated by semicolons",
			section = customSection,
			position = 0
	)
	default String customAffirmations()
	{
		return "";
	}

	@ConfigItem(
			keyName = "useOnlyCustom",
			name = "Use Only Custom",
			description = "If enabled, only custom affirmations will be displayed",
			section = customSection,
			position = 1
	)
	default boolean useOnlyCustom()
	{
		return false;
	}

	// General Settings
	@ConfigItem(
			keyName = "displayDuration",
			name = "Display Ticks",
			description = "How long each affirmation should be displayed (in ticks)",
			section = generalSection,
			position = 0
	)
	default int displayDuration()
	{
		return 15;
	}

	@ConfigItem(
			keyName = "pauseDuration",
			name = "Pause Ticks",
			description = "How long to pause between affirmations (in ticks)",
			section = generalSection,
			position = 1
	)
	default int pauseDuration()
	{
		return 5;
	}

	@ConfigItem(
			keyName = "fadeDuration",
			name = "Fade Ticks",
			description = "How long the fade in/out effect should last (in milliseconds)",
			section = generalSection,
			position = 2
	)
	default int fadeDuration()
	{
		return 4;
	}

	// Display Settings (moved from fullWidth to appearance)
	@ConfigItem(
			keyName = "useFullWidthDisplay",
			name = "Use Full Width Display",
			description = "If enabled, displays affirmations as a large string across the top of the screen",
			section = appearanceSection,
			position = 0
	)
	default boolean useFullWidthDisplay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "textColor",
			name = "Text Colour",
			description = "Colour of the affirmation text",
			section = appearanceSection,
			position = 1
	)
	default Color textColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
			keyName = "fontFamily",
			name = "Font Family",
			description = "Choose the font family for affirmations",
			section = appearanceSection,
			position = 2
	)
	default FontFamily fontFamily()
	{
		return FontFamily.TIMES;
	}

	@ConfigItem(
			keyName = "fontStyle",
			name = "Font Style",
			description = "Choose the font style for affirmations",
			section = appearanceSection,
			position = 3
	)
	default FontStyle fontStyle()
	{
		return FontStyle.PLAIN;
	}

	@ConfigItem(
			keyName = "fontSize",
			name = "Font Size",
			description = "Font size for the display",
			section = appearanceSection,
			position = 4
	)
	default int fontSize()
	{
		return 45;
	}

	@ConfigItem(
			keyName = "strokeWidth",
			name = "Stroke Width",
			description = "Width of the text outline (0 for no outline)",
			section = appearanceSection,
			position = 5
	)
	default int strokeWidth()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "strokeColor",
			name = "Stroke Colour",
			description = "Colour of the text outline (if stroke width > 0)",
			section = appearanceSection,
			position = 6
	)
	default Color strokeColor()
	{
		return Color.BLACK;
	}

	enum FontStyle
	{
		PLAIN("Plain"),
		BOLD("Bold"),
		ITALIC("Italic"),
		BOLD_ITALIC("Bold Italic");

		private final String name;

		FontStyle(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public int toAwtFontStyle()
		{
			switch (this)
			{
				case BOLD:
					return java.awt.Font.BOLD;
				case ITALIC:
					return java.awt.Font.ITALIC;
				case BOLD_ITALIC:
					return java.awt.Font.BOLD | java.awt.Font.ITALIC;
				default:
					return java.awt.Font.PLAIN;
			}
		}
	}

	enum FontFamily
	{
		ARIAL("Arial"),
		HELVETICA("Helvetica"),
		VERDANA("Verdana"),
		TAHOMA("Tahoma"),
		TIMES("Times New Roman"),
		GEORGIA("Georgia"),
		TREBUCHET("Trebuchet MS"),
		COURIER("Courier New"),
		COMICSANS("Comic Sans"),
		IMPACT("Impact");

		private final String fontName;

		FontFamily(String fontName)
		{
			this.fontName = fontName;
		}

		@Override
		public String toString()
		{
			return fontName;
		}

		public String getFontName()
		{
			return fontName;
		}
	}
}