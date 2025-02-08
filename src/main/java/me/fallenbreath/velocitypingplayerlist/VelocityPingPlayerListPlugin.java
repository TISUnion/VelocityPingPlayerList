package me.fallenbreath.velocitypingplayerlist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.fallenbreath.velocitypingplayerlist.sampler.ClassicPlayerSampler;
import me.fallenbreath.velocitypingplayerlist.sampler.GroupedPlayerSampler;
import me.fallenbreath.velocitypingplayerlist.sampler.PlayerSampler;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
		id = PluginMeta.ID, name = PluginMeta.NAME, version = PluginMeta.VERSION,
		url = "https://github.com/TISUnion/VelocityPingPlayerList",
		description = "A velocity plugin to remember the last server you logged in",
		authors = {"Fallen_Breath"}
)
public class VelocityPingPlayerListPlugin
{
	private final ProxyServer server;
	private final Logger logger;
	private final Config config;

	@Inject
	public VelocityPingPlayerListPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory)
	{
		this.server = server;
		this.logger = logger;
		this.config = new Config(logger, dataDirectory.resolve("config.yml"));
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event)
	{
		if (!this.config.load())
		{
			this.logger.warn("Failed to load plugin config, the plugin will be disabled");
			return;
		}
		if (!this.config.isEnabled())
		{
			this.logger.info("Plugin is disabled by config");
		}
		this.server.getEventManager().register(this, ProxyPingEvent.class, this::handleProxyPingEvent);
	}

	private void handleProxyPingEvent(ProxyPingEvent event)
	{
		ServerPing.Builder builder = event.getPing().asBuilder();

		PlayerSampler sampler = switch (this.config.getMode())
		{
			case CLASSIC -> new ClassicPlayerSampler();
			case GROUPED -> new GroupedPlayerSampler();
		};

		var sampleResult = sampler.sample(this.server, this.config);
		builder.onlinePlayers(sampleResult.onlineCount());
		builder.samplePlayers(sampleResult.players().toArray(new ServerPing.SamplePlayer[0]));

		event.setPing(builder.build());
	}
}
