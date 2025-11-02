/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.util;

import java.util.Map;

/**
 * Utility class providing very simple English pluralization and singularization.
 */
public final class Pluralizer {

    private static final Map<String, String> IRREGULAR_SINGULARS = Map.of(
            "indices", "index"
    );

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Pluralizer() {
    }

    /**
     * Returns a plural form of the given word using a small subset
     * of English pluralization rules.
     *
     * @param word singular noun
     * @return plural noun
     */
    public static String pluralize(String word) {
        if (word == null || word.isBlank()) {
            return word;
        }
        String lower = word.toLowerCase();
        if (lower.endsWith("s") || lower.endsWith("x") || lower.endsWith("z")
                || lower.endsWith("ch") || lower.endsWith("sh")) {
            return word + "es";
        }
        if (lower.endsWith("y") && word.length() > 1 && !isVowel(lower.charAt(lower.length() - 2))) {
            return word.substring(0, word.length() - 1) + "ies";
        }
        return word + "s";
    }

    /**
     * Returns a singular form of the given word using a small subset
     * of English singularization rules.
     *
     * @param word plural noun
     * @return singular noun
     */
    public static String singularize(String word) {
        if (word == null || word.isBlank()) {
            return word;
        }
        String lower = word.toLowerCase();
        String irregular = IRREGULAR_SINGULARS.get(lower);
        if (irregular != null) {
            if (Character.isUpperCase(word.charAt(0))) {
                return Character.toUpperCase(irregular.charAt(0)) + irregular.substring(1);
            }
            return irregular;
        }
        if (lower.endsWith("ies") && word.length() > 3 && !isVowel(lower.charAt(lower.length() - 4))) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (lower.endsWith("es")
                && (lower.endsWith("ses") || lower.endsWith("xes") || lower.endsWith("zes")
                || lower.endsWith("ches") || lower.endsWith("shes"))) {
            return word.substring(0, word.length() - 2);
        }
        if (lower.endsWith("s") && word.length() > 1) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private static boolean isVowel(char c) {
        return "aeiou".indexOf(Character.toLowerCase(c)) >= 0;
    }
}
