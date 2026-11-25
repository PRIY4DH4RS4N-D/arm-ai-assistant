package com.example.adapt_ai_ui.data.platform

data class CodingProgress(val drafts: Map<String, String> = emptyMap(), val solved: Set<String> = emptySet())

internal data class CodingExercise(
    val id: String, val language: String, val title: String, val example: String, val answer: String,
    val explanation: String
)

internal val codingExercises = listOf(
    CodingExercise("c-loop", "C", "Trace the accumulator", """
        #include <stdio.h>

        int main(void) {
            int total = 0;
            for (int i = 1; i <= 4; i++) {
                total += i;
            }
            printf("%d\n", total);
        }
    """.trimIndent(), "10", "The loop adds 1, 2, 3 and 4 to total."),
    CodingExercise("cpp-reference", "C++", "Debug the unexpected value", """
        #include <iostream>

        int main() {
            int value = 3;
            int& alias = value;
            alias += 4;
            std::cout << value;
        }
    """.trimIndent(), "7", "A reference aliases the same object, so changing alias changes value."),
    CodingExercise("python-list", "Python", "Trace a list alias", """
        values = [2, 4]
        alias = values
        alias.append(6)
        print(sum(values))
    """.trimIndent(), "12", "Both names reference one list. Its elements are 2, 4 and 6 after the append.")
)
