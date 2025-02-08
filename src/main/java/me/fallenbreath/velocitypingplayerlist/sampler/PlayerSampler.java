package me.fallenbreath.velocitypingplayerlist.sampler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.fallenbreath.velocitypingplayerlist.Config;

import java.util.List;

@FunctionalInterface
public interface PlayerSampler
{
	SampleResult sample(ProxyServer server, Config config);

	record SampleResult(int onlineCount, List<ServerPing.SamplePlayer> players) {}
}
