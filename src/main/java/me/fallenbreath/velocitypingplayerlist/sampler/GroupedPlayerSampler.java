package me.fallenbreath.velocitypingplayerlist.sampler;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.fallenbreath.velocitypingplayerlist.Config;

import java.util.*;

public class GroupedPlayerSampler implements PlayerSampler
{
	private static final UUID NIL_UUID = new UUID(0L, 0L);

	@Override
	public SampleResult sample(ProxyServer server, Config config)
	{
		Map<String, List<ServerPing.SamplePlayer>> playersByServer = Maps.newLinkedHashMap();
		Collection<Player> allPlayers = server.getAllPlayers();
		for (Player player : allPlayers)
		{
			Optional<ServerConnection> currentServerOpt = player.getCurrentServer();
			currentServerOpt.ifPresent(serverConnection -> playersByServer.
					computeIfAbsent(serverConnection.getServerInfo().getName(), k -> Lists.newArrayList()).
					add(new ServerPing.SamplePlayer(player.getUsername(), player.getUniqueId()))
			);
		}

		var stream = playersByServer.entrySet().stream();
		if (config.shouldSortServer())
		{
			stream = stream.sorted(Map.Entry.comparingByKey());
		}
		if (config.getMaxDisplayedServer() > 0)
		{
			stream = stream.limit(config.getMaxDisplayedServer());
		}

		var lines = stream.
				map(e -> {
					var samplePlayers = ClassicPlayerSampler.sampleAndSortByConfig(e.getValue().stream(), config).toList();
					String text =
							"§3" + "[" + e.getKey() + "] " +
							"§7" + "(" + samplePlayers.size() + "): " +
							"§r" +
							Joiner.on("§7,§r ").join(samplePlayers.stream().
									map(ServerPing.SamplePlayer::getName).
									toList()
							);
					return new ServerPing.SamplePlayer(text, NIL_UUID);
				}).
				toList();

		return new SampleResult(allPlayers.size(), lines);
	}
}
