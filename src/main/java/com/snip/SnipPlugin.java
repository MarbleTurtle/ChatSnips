package com.snip;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Chat Transcripts"
)
public class SnipPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private SnipConfig config;

	private SnipPanel panel;
	private NavigationButton button;

	@Override
	protected void startUp() throws Exception
	{
		panel = new SnipPanel(config,client);
		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "/227-0.png");

		button = NavigationButton.builder()
				.tooltip("Chat Transcripts")
				.icon(icon)
				.priority(333)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(button);

	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(button);
	}

	@Provides
	SnipConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SnipConfig.class);
	}
}
