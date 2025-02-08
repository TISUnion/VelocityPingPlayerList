package me.fallenbreath.velocitypingplayerlist.sampler;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.fallenbreath.velocitypingplayerlist.Config;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ClassicPlayerSampler implements PlayerSampler
{
	@Override
	public SampleResult sample(ProxyServer server, Config config)
	{
		List<ServerPing.SamplePlayer> samplePlayers = Lists.newArrayList();
		Collection<Player> allPlayers = server.getAllPlayers();
		for (Player player : allPlayers)
		{
			samplePlayers.add(new ServerPing.SamplePlayer(player.getUsername(), player.getUniqueId()));
		}
		var players = sampleAndSortByConfig(samplePlayers.stream(), config).toList();
		return new SampleResult(allPlayers.size(), players);
	}

	public static Stream<ServerPing.SamplePlayer> sampleAndSortByConfig(Stream<ServerPing.SamplePlayer> stream, Config config)
	{
		if (config.shouldSortPlayer())
		{
			stream = stream.sorted(Comparator.comparing(ServerPing.SamplePlayer::getName));
		}
		if (config.getMaxDisplayedPlayerPerServer() > 0)
		{
			stream = stream.limit(config.getMaxDisplayedPlayerPerServer());
		}
		return stream;
	}
}
