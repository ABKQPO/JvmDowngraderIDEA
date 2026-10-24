package com.hfstudio.jvmdowngraderidea.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JavaSourceLanguageFeatureDetectorTest {
    @Test
    fun `detects local var as Java 10`() {
        assertEquals(
            10,
            JavaSourceLanguageFeatureDetector.detect("class Demo { void run() { var value = 1; } }"),
        )
    }

    @Test
    fun `detects var lambda parameter as Java 11`() {
        assertEquals(
            11,
            JavaSourceLanguageFeatureDetector.detect(
                "class Demo { java.util.function.Function<String, Integer> run() { return (var value) -> value.length(); } }",
            ),
        )
    }

    @Test
    fun `detects record declaration as Java 16`() {
        assertEquals(
            16,
            JavaSourceLanguageFeatureDetector.detect("record Demo(String value) { }"),
        )
    }

    @Test
    fun `detects pattern instance of as Java 16`() {
        assertEquals(
            16,
            JavaSourceLanguageFeatureDetector.detect(
                "class Demo { boolean run(Object value) { return value instanceof String text; } }",
            ),
        )
    }

    @Test
    fun `detects enhanced switch as Java 14`() {
        assertEquals(
            14,
            JavaSourceLanguageFeatureDetector.detect(
                "class Demo { int run(int value) { return switch (value) { case 1 -> 1; default -> 0; }; } }",
            ),
        )
    }

    @Test
    fun `detects text block as Java 15`() {
        assertEquals(
            15,
            JavaSourceLanguageFeatureDetector.detect(
                "class Demo { String text = \"\"\"line 1\\nline 2\"\"\"; }",
            ),
        )
    }

    @Test
    fun `detects pattern switch as Java 21`() {
        assertEquals(
            21,
            JavaSourceLanguageFeatureDetector.detect(
                "class Demo { int run(Object value) { return switch (value) { case String text -> text.length(); default -> 0; }; } }",
            ),
        )
    }

    @Test
    fun `detects sealed declaration as Java 17`() {
        assertEquals(
            17,
            JavaSourceLanguageFeatureDetector.detect("sealed interface Demo permits Child { }"),
        )
    }

    @Test
    fun `detects string template as Java 22`() {
        assertEquals(
            22,
            JavaSourceLanguageFeatureDetector.detect(
                """
                class Demo {
                    String text = STR."hello \{name}";
                }
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `ignores modern tokens in comments strings and character literals`() {
        assertNull(
            JavaSourceLanguageFeatureDetector.detect(
                """
                class Demo {
                    String text = "var value = 1; case String text -> 0";
                    char quote = '\\'';
                    // value instanceof String text
                    /* switch (value) { case String text -> 0; } */
                }
                """.trimIndent(),
            ),
        )
    }
}
