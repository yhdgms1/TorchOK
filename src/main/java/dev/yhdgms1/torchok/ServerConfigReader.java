package dev.yhdgms1.torchok;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfigReader {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(TorchOK.SERVER_CONFIG_FILENAME);

    private static ServerConfig getDefault() {
        return new ServerConfig();
    }

    public static ServerConfig readConfig() {
        if (Files.notExists(CONFIG_PATH)) {
            return getDefault();
        } else {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                return GSON.fromJson(reader, ServerConfig.class);
            } catch (IOException e) {
                TorchOK.LOGGER.error("Failed to load config.");

                return getDefault();
            }
        }
    }
}
