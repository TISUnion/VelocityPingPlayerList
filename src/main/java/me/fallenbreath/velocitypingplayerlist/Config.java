package me.fallenbreath.velocitypingplayerlist;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Config
{
	private ConfigData configData = new ConfigData();

	private final Logger logger;
	private final Path configFile;

	private Mode mode = Mode.DEFAULT;

	public Config(Logger logger, Path configFile)
	{
		this.logger = logger;
		this.configFile = configFile;
	}

	public boolean load()
	{
		File configDir = this.configFile.getParent().toFile();
		if (!configDir.exists() && !configDir.mkdir())
		{
			this.logger.error("Create data directory {} failed",configDir);
			return false;
		}

		File file = this.configFile.toFile();
		if (!file.exists())
		{
			try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml"))
			{
				Files.copy(Objects.requireNonNull(in), file.toPath());
			}
			catch (Exception e)
			{
				this.logger.error("Generate default config failed", e);
				return false;
			}
		}

		try
		{
			Yaml yaml = new Yaml(new Constructor(ConfigData.class));
			this.configData = yaml.loadAs(Files.readString(file.toPath()), ConfigData.class);
		}
		catch (Exception e)
		{
			this.logger.error("Read config failed", e);
			return false;
		}

		try
		{
			this.mode = Mode.valueOf(this.configData.mode.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			this.mode = Mode.DEFAULT;
			this.logger.error("Invalid mode: {}, use default mode {} instead", this.configData.mode, this.mode.toString().toLowerCase());
		}
		return true;
	}

	public boolean isEnabled()
	{
		return this.configData.enabled;
	}

	public Mode getMode()
	{
		return this.mode;
	}

	public int getMaxDisplayedServer()
	{
		return this.configData.max_displayed_server;
	}

	public int getMaxDisplayedPlayerPerServer()
	{
		return this.configData.max_displayed_player_per_server;
	}

	public boolean shouldSortServer()
	{
		return this.configData.sort_server;
	}

	public boolean shouldSortPlayer()
	{
		return this.configData.sort_player;
	}
	
	private static class ConfigData
	{
		public boolean enabled = false;
		public String mode = Mode.DEFAULT.name().toLowerCase();

		public int max_displayed_server = 3;
		public int max_displayed_player_per_server = 3;
		public boolean sort_server = true;
		public boolean sort_player = true;
	}
}
