package io.github.alexkond12.villagecensus.text;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.alexkond12.villagecensus.census.CensusReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the translations: every language must have exactly the keys of en_us.json,
 * with the same placeholders, and every key used in code must exist.
 */
class LangFilesTest {
    /** Languages the mod promises to ship (see README). */
    private static final Set<String> REQUIRED = Set.of(
            "en_us", "ru_ru", "uk_ua", "be_by", "pl_pl", "de_de", "fr_fr", "es_es",
            "pt_br", "it_it", "tr_tr", "zh_cn", "zh_tw", "ja_jp", "ko_kr");
    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+\\$)?s");

    private static Map<String, Map<String, String>> languages;

    @BeforeAll
    static void load() throws IOException {
        Path dir = Path.of(System.getProperty("villagecensus.langDir"));
        languages = new TreeMap<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                String name = file.getFileName().toString().replace(".json", "");
                JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                Map<String, String> entries = new TreeMap<>();
                json.entrySet().forEach(e -> entries.put(e.getKey(), e.getValue().getAsString()));
                languages.put(name, entries);
            }
        }
    }

    @Test
    void allRequiredLanguagesExist() {
        assertTrue(languages.keySet().containsAll(REQUIRED), "Missing: " + missing());
    }

    @Test
    void everyLanguageHasTheSameKeysAsEnglish() {
        Set<String> english = languages.get("en_us").keySet();
        languages.forEach((lang, entries) ->
                assertEquals(english, entries.keySet(), "Keys differ in " + lang + ".json"));
    }

    @Test
    void placeholdersMatchEnglish() {
        Map<String, String> english = languages.get("en_us");
        languages.forEach((lang, entries) -> entries.forEach((key, value) ->
                assertEquals(count(english.get(key)), count(value),
                        "Placeholder count differs for '" + key + "' in " + lang + ".json")));
    }

    @Test
    void noEmptyTranslations() {
        languages.forEach((lang, entries) -> entries.forEach((key, value) ->
                assertFalse(value.isBlank(), "Empty value for '" + key + "' in " + lang + ".json")));
    }

    @Test
    void everyKeyUsedInCodeExists() throws IllegalAccessException {
        Set<String> english = languages.get("en_us").keySet();
        List<String> used = new ArrayList<>();
        for (Field field : LangKeys.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class
                    && !field.getName().endsWith("PREFIX") && !field.getName().equals("P")) {
                field.setAccessible(true);
                used.add((String) field.get(null));
            }
        }
        for (CensusReport.Hint hint : CensusReport.Hint.values()) {
            used.add(LangKeys.hint(hint));
        }
        for (String key : used) {
            assertTrue(english.contains(key), "en_us.json is missing key used in code: " + key);
        }
    }

    private static int count(String text) {
        Matcher m = PLACEHOLDER.matcher(text);
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }

    private static List<String> missing() {
        return REQUIRED.stream().filter(l -> !languages.containsKey(l)).sorted().toList();
    }
}
