package io.github.alexkond12.villagecensus.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Checks the data and asset JSON files that Minecraft only validates at runtime
 * (a broken file there shows up as an error in the game log, not as a build failure).
 */
class DataFilesTest {
    private static final String NS = "villagecensus";
    private static final Path RESOURCES = Path.of(System.getProperty("villagecensus.resourcesDir", "src/main/resources"));
    /** Advancement triggers whose JSON format was checked in game on 26.1.2, 26.2 and 26.3. */
    private static final Set<String> VERSION_INDEPENDENT_TRIGGERS = Set.of("minecraft:inventory_changed");

    @Test
    void everyJsonFileParses() throws IOException {
        List<Path> files = jsonFiles(RESOURCES);
        assertFalse(files.isEmpty(), "no JSON files found in " + RESOURCES.toAbsolutePath());
        for (Path file : files) {
            read(file); // throws on malformed JSON
        }
    }

    /**
     * One advancement file is shared by every supported Minecraft version, so it may only use
     * criteria whose JSON format is the same in all of them. {@code minecraft:recipe_unlocked}
     * is not: 26.1 and 26.2 expect {@code "recipe"}, 26.3 expects {@code "recipes"}, and on 26.3
     * a broken advancement even stops worlds from loading.
     */
    @Test
    void advancementsOnlyUseVersionIndependentCriteria() throws IOException {
        List<Path> files = jsonFiles(RESOURCES.resolve("data/" + NS + "/advancement"));
        assertFalse(files.isEmpty(), "no advancements found");
        for (Path file : files) {
            JsonObject advancement = read(file).getAsJsonObject();
            JsonObject criteria = advancement.getAsJsonObject("criteria");
            for (Map.Entry<String, JsonElement> criterion : criteria.entrySet()) {
                String trigger = criterion.getValue().getAsJsonObject().get("trigger").getAsString();
                assertTrue(VERSION_INDEPENDENT_TRIGGERS.contains(trigger),
                        file + ": trigger " + trigger + " of criterion '" + criterion.getKey()
                                + "' is not known to have the same format in every supported Minecraft version");
            }
            // Every criterion must be listed in the requirements, otherwise the advancement does not load.
            List<String> required = new ArrayList<>();
            advancement.getAsJsonArray("requirements")
                    .forEach(group -> group.getAsJsonArray().forEach(name -> required.add(name.getAsString())));
            assertEquals(criteria.keySet().stream().sorted().toList(), required.stream().sorted().toList(),
                    file + ": requirements must list every criterion exactly once");

            if (advancement.has("rewards") && advancement.getAsJsonObject("rewards").has("recipes")) {
                for (JsonElement recipe : advancement.getAsJsonObject("rewards").getAsJsonArray("recipes")) {
                    assertRecipeExists(recipe.getAsString(), file);
                }
            }
        }
    }

    @Test
    void craftedItemsHaveModelsAndTextures() throws IOException {
        for (Path file : jsonFiles(RESOURCES.resolve("data/" + NS + "/recipe"))) {
            String result = read(file).getAsJsonObject().getAsJsonObject("result").get("id").getAsString();
            if (!result.startsWith(NS + ":")) {
                continue;
            }
            String item = result.substring(NS.length() + 1);
            Path definition = RESOURCES.resolve("assets/" + NS + "/items/" + item + ".json");
            assertTrue(Files.isRegularFile(definition), "missing item definition " + definition);

            String model = read(definition).getAsJsonObject().getAsJsonObject("model").get("model").getAsString();
            Path modelFile = RESOURCES.resolve("assets/" + NS + "/models/" + path(model) + ".json");
            assertTrue(Files.isRegularFile(modelFile), "missing item model " + modelFile);

            JsonObject textures = read(modelFile).getAsJsonObject().getAsJsonObject("textures");
            for (Map.Entry<String, JsonElement> texture : textures.entrySet()) {
                Path png = RESOURCES.resolve("assets/" + NS + "/textures/" + path(texture.getValue().getAsString()) + ".png");
                assertTrue(Files.isRegularFile(png), "missing texture " + png);
            }
        }
    }

    private static void assertRecipeExists(String id, Path referencedFrom) {
        if (!id.startsWith(NS + ":")) {
            fail(referencedFrom + ": unexpected recipe namespace in " + id);
        }
        Path recipe = RESOURCES.resolve("data/" + NS + "/recipe/" + path(id) + ".json");
        assertTrue(Files.isRegularFile(recipe), referencedFrom + ": recipe " + id + " does not exist");
    }

    /** {@code villagecensus:item/foo} -> {@code item/foo}. */
    private static String path(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    private static List<Path> jsonFiles(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        }
    }

    private static JsonElement read(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }
}
