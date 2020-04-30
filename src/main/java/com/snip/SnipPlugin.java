package com.snip;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ClanMember;
import net.runelite.api.ClanMemberRank;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ClanManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.game.ClanManager;
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
	@Inject
	private ClanManager clanManager;
	private SnipPanel panel;
	private NavigationButton button;
	final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "/227-0.png");

	@Override
	protected void startUp() throws Exception
	{
		panel = new SnipPanel(config,client,clanManager);
		button = NavigationButton.builder()
				.tooltip("Chat Transcripts")
				.icon(icon)
				.priority(config.location())
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

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals("Chat Transcripts")) {
			clientToolbar.removeNavigation(button);
			button = NavigationButton.builder()
					.tooltip("Chat Transcripts")
					.icon(icon)
					.priority(config.location())
					.panel(panel)
					.build();
			clientToolbar.addNavigation(button);
		}
	}
}
