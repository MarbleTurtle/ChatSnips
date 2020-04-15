package com.snip;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Chat Transcripts")
public interface SnipConfig extends Config
{
	@ConfigItem(
		keyName = "location",
		name = "Side Panel Location",
		description = "Determines the location of the icon in the side panel."
	)
	default int location()
	{
		return 15;
	}
	@ConfigItem(
			keyName = "clipboard",
			name = "Copy to clipboard",
			description = "Copies the image to clipboard after generating."
	)
	default boolean clipboard()
	{
		return true;
	}
	@ConfigItem(
			keyName = "open",
			name = "Open after generation",
			description = "Opens the transcript image after creation."
	)
	default boolean postOpen()
	{
		return true;
	}

}
