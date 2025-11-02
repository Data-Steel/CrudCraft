/**
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

/**
 * Utility for transforming strings into a specific casing style.
 */
public enum StringCase {
    /**
     * lowerCamelCase style.
     */
    CAMEL {
        @Override
        public String apply(String input) {
            String pascal = PASCAL.apply(input);
            if (pascal.isEmpty()) return "";
            return pascal.substring(0, 1).toLowerCase() + pascal.substring(1);
        }
    },
    /**
     * UpperCamelCase style.
     */
    PASCAL {
        @Override
        public String apply(String input) {
            StringBuilder sb = new StringBuilder();
            boolean upper = true;
            for (char c : input.toCharArray()) {
                if (!Character.isLetterOrDigit(c)) {
                    upper = true;
                    continue;
                }
                sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upper = false;
            }
            return sb.toString();
        }
    },
    /**
     * snake_case style.
     */
    SNAKE {
        @Override
        public String apply(String input) {
            return toDelimited(input, '_').toLowerCase();
        }
    },
    /**
     * kebab-case style.
     */
    KEBAB {
        @Override
        public String apply(String input) {
            return toDelimited(input, '-').toLowerCase();
        }
    },
    /**
     * UPPER_SNAKE_CASE style.
     */
    UPPER_SNAKE {
        @Override
        public String apply(String input) {
            return toDelimited(input, '_').toUpperCase();
        }
    };

    /**
     * Applies the case conversion to the given input.
     *
     * @param input a non-null string
     * @return the converted value
     */
    public abstract String apply(String input);

    private static String toDelimited(String input, char delimiter) {
        if (input == null) throw new NullPointerException("input");
        if (input.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        boolean lastWasDelim = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Non-alnum → single delimiter
            if (!Character.isLetterOrDigit(c)) {
                if (!lastWasDelim && !sb.isEmpty()) {
                    sb.append(delimiter);
                    lastWasDelim = true;
                }
                continue;
            }

            boolean isUpper = Character.isUpperCase(c);

            // Find previous alnum
            Character prevAlnum = null;
            for (int j = i - 1; j >= 0; j--) {
                char pj = input.charAt(j);
                if (Character.isLetterOrDigit(pj)) { prevAlnum = pj; break; }
            }
            char next = (i + 1 < input.length()) ? input.charAt(i + 1) : 0;
            boolean nextIsLower = Character.isLetterOrDigit(next) && Character.isLowerCase(next);

            boolean needDelim = false;
            if (!sb.isEmpty() && !lastWasDelim && isUpper && prevAlnum != null) {
                boolean prevLowerOrDigit = Character.isLowerCase(prevAlnum) || Character.isDigit(prevAlnum);
                boolean prevUpper = Character.isUpperCase(prevAlnum);
                needDelim = prevLowerOrDigit || (prevUpper && nextIsLower);
            }

            if (needDelim) {
                sb.append(delimiter);
                lastWasDelim = true;
            }

            sb.append(Character.toLowerCase(c));
            lastWasDelim = false;
        }

        // Trim trailing delimiter if any
        int len = sb.length();
        if (len > 0 && sb.charAt(len - 1) == delimiter) {
            sb.setLength(len - 1);
        }

        return sb.toString();
    }
}
