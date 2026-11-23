package com.example.adapt_ai_ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface TutorEngine {
    suspend fun generateResponse(request: TutorRequest): TutorResponse
    fun getSuggestedPrompts(domainId: String, currentTopic: String, mode: TutorMode): List<String>
    fun getWelcomeMessage(
        learnerName: String,
        domainTitle: String,
        currentTopic: String,
        mode: TutorMode
    ): TutorConversationMessage
}

class PrototypeTutorEngine : TutorEngine {

    override suspend fun generateResponse(request: TutorRequest): TutorResponse {
        // Simulate realistic on-device neural network token generation latency
        delay(400)

        val q = request.query.lowercase()
        val topic = request.topicTitle
        val mode = request.mode

        return when (mode) {
            TutorMode.TEACH_ME -> generateTeachMeResponse(q, topic, request)
            TutorMode.SOCRATIC -> generateSocraticResponse(q, topic, request)
            TutorMode.DEBUG -> generateDebugResponse(q, topic, request)
            TutorMode.PRACTICE -> generatePracticeResponse(q, topic, request)
            TutorMode.REVIEW -> generateReviewResponse(q, topic, request)
        }
    }

    override fun getSuggestedPrompts(domainId: String, currentTopic: String, mode: TutorMode): List<String> {
        return when (mode) {
            TutorMode.TEACH_ME -> listOf(
                "Can you explain this with a real-world analogy?",
                "Why is this concept foundational for beginners?",
                "What happens step-by-step during execution?"
            )
            TutorMode.SOCRATIC -> when (domainId) {
                "ai_ml" -> listOf(
                    "Why does INT8 save 4x memory over FP32?",
                    "What happens to activations if scale S is too large?",
                    "How does symmetric quantization eliminate zero-point subtraction?"
                )
                "cs" -> listOf(
                    "Why is binary search tree lookup O(log N)?",
                    "How does a hash collision affect lookup latency?",
                    "What is the difference between BFS and DFS traversal?"
                )
                "embedded" -> listOf(
                    "Why should interrupt service routines (ISRs) be short?",
                    "How does DMA transfer data without CPU intervention?",
                    "What is the role of the NVIC in ARM Cortex-M?"
                )
                "programming" -> listOf(
                    "Why does RAII eliminate manual delete calls in C++?",
                    "How does std::atomic prevent data races?",
                    "What causes pointer aliasing to hinder compiler optimization?"
                )
                else -> listOf(
                    "What are the core trade-offs in $currentTopic?",
                    "How does this connect to real-world hardware limits?",
                    "Can you challenge my intuition on this topic?"
                )
            }
            TutorMode.DEBUG -> listOf(
                "Why am I getting accuracy loss or overflow here?",
                "Can you review this code snippet for edge device bottlenecks?",
                "How do I trace cache misses in this loop?"
            )
            TutorMode.PRACTICE -> listOf(
                "Give me a scenario-based question on $currentTopic",
                "Test my understanding of edge constraints",
                "Give me a quick calculation drill"
            )
            TutorMode.REVIEW -> listOf(
                "Summarize the key mathematical formulas",
                "What are the 3 most common learner misconceptions?",
                "Give me a cheat-sheet checklist for this milestone"
            )
        }
    }

    override fun getWelcomeMessage(
        learnerName: String,
        domainTitle: String,
        currentTopic: String,
        mode: TutorMode
    ): TutorConversationMessage {
        val intro = when (mode) {
            TutorMode.TEACH_ME ->
                "Hello $learnerName! I'm your interactive tutor for $domainTitle. Ask me anything about $currentTopic and I'll break it down into intuitive mental models."
            TutorMode.SOCRATIC ->
                "Welcome $learnerName! I'm in Socratic Mode for $domainTitle. Rather than just giving answers, I'll guide you through $currentTopic with targeted hints and thought-provoking questions. What would you like to explore?"
            TutorMode.DEBUG ->
                "Hello $learnerName! Debug & Code Mode active for $domainTitle. Share a code snippet, error message, or performance issue in $currentTopic to inspect execution bottlenecks."
            TutorMode.PRACTICE ->
                "Practice Mode active for $domainTitle. Ready to test your mastery of $currentTopic with tailored conceptual drills?"
            TutorMode.REVIEW ->
                "Review Mode active for $domainTitle. Let's recap key takeaways, formulas, and identify any weak spots in $currentTopic."
        }

        return TutorConversationMessage(
            id = "welcome_${System.currentTimeMillis()}",
            sender = "ADAPT-AI",
            isTutor = true,
            content = intro,
            mode = mode,
            structuredPayload = TutorResponse(
                text = intro,
                conceptExplanation = "Active Track: $domainTitle • Current Focus: $currentTopic",
                keyTakeaways = listOf(
                    "Socratic guidance adapts to your skill level (${mode.title} active)",
                    "Ready for on-device lightweight execution (ARM SoC Challenge prototype)",
                    "Select any suggested prompt below or type your own question"
                ),
                followUpPrompts = getSuggestedPrompts("ai_ml", currentTopic, mode)
            )
        )
    }

    // -------------------------------------------------------------
    // Private Mode-Specific Response Generators
    // -------------------------------------------------------------

    private fun generateTeachMeResponse(q: String, topic: String, req: TutorRequest): TutorResponse {
        val isMemoryOrQuant = q.contains("quant") || q.contains("int8") || q.contains("memory") || q.contains("fp32")
        return if (isMemoryOrQuant || req.domainId == "ai_ml") {
            TutorResponse(
                text = "Think of 32-bit floating point numbers like sending oversized shipping containers for items that could fit in a lunchbox. By quantizing to 8-bit integers, you shrink every weight by 75% without losing the neural network's functional shape.",
                conceptExplanation = "In full-precision FP32, every weight takes 4 bytes. In INT8, each weight takes only 1 byte. On embedded microcontrollers, DRAM memory bus bandwidth is the #1 consumer of battery and thermal headroom.",
                steps = listOf(
                    "Find the dynamic range [r_min, r_max] across sample calibration data.",
                    "Compute Scale S = (r_max - r_min) / 255.",
                    "Compute integer Zero-Point offset Z = round(-r_min / S).",
                    "Map floating-point activations to 8-bit grid: q = round(r / S) + Z."
                ),
                exampleOrAnalogy = "Analogy: Imagine a high-definition 4K photo versus an optimized 1080p compression. To a human viewer on a phone screen (or an edge classifier detecting an object), the compressed image preserves 99% of the actionable detail while using 4x less cellular data.",
                formula = "r ≈ S × (q - Z)\nwhere S ∈ ℝ+ (scale), Z ∈ ℤ (zero-point), q ∈ [-128, 127] or [0, 255]",
                keyTakeaways = listOf(
                    "75% memory footprint reduction allows models to fit in on-chip SRAM.",
                    "SRAM reads use up to 100x less energy than external DRAM transfers.",
                    "ARM NEON SIMD hardware executes up to 16 INT8 MACs in a single clock cycle."
                ),
                followUpPrompts = listOf(
                    "What is the difference between symmetric and asymmetric quantization?",
                    "How does zero-point offset prevent precision skew?",
                    "Can we quantize activations as well as weights?"
                )
            )
        } else {
            TutorResponse(
                text = "Let's explore ${req.topicTitle} from foundational principles. In ${req.domainTitle}, complex systems are built from clean composable primitives.",
                conceptExplanation = "Mastering ${req.topicTitle} requires understanding how state transitions and constraints govern behavior.",
                steps = listOf(
                    "Identify the core inputs and operational constraints.",
                    "Trace the execution or mathematical transformation.",
                    "Observe how the system handles boundary cases."
                ),
                exampleOrAnalogy = "Analogy: Think of this like balancing a scale: whenever you adjust precision or speed on one side, you must account for overhead on the other.",
                keyTakeaways = listOf(
                    "Begin with intuitive mental models before applying advanced optimizations.",
                    "Always measure empirical performance against theoretical Big-O complexity."
                ),
                followUpPrompts = listOf(
                    "Can you walk me through a simple code example?",
                    "What is the most common beginner mistake here?"
                )
            )
        }
    }

    private fun generateSocraticResponse(q: String, topic: String, req: TutorRequest): TutorResponse {
        val hasMemoryQuery = q.contains("4x") || q.contains("memory") || q.contains("why")
        return if (hasMemoryQuery || req.domainId == "ai_ml") {
            TutorResponse(
                text = "That is a central insight in hardware-software co-design! Let me ask you: when an edge processor runs an inference loop, which operation costs more battery power: computing 2 + 2 in the ALU, or fetching that 2 from external DRAM?",
                conceptExplanation = "ALU integer math takes a fraction of a picojoule (pJ). Accessing DRAM off-chip can burn hundreds of picojoules per word. When weights shrink from 4 bytes to 1 byte, what happens to your memory bus traffic?",
                steps = listOf(
                    "Step 1: Ponder the physical memory hierarchy (DRAM -> Cache -> Registers).",
                    "Step 2: Calculate bus cycles required to stream 10 million weights in FP32 vs INT8.",
                    "Step 3: Consider how on-chip SRAM eliminates off-chip bus wakeups."
                ),
                exampleOrAnalogy = "Think about ordering groceries from across town versus taking ingredients directly from your refrigerator. If your model fits in SRAM (the fridge), you rarely have to drive into town (DRAM).",
                keyTakeaways = listOf(
                    "DRAM data movement dominates inference power consumption on edge SoCs.",
                    "Shrinking weights by 4x enables zero-DRAM residency for compact vision models."
                ),
                followUpPrompts = listOf(
                    "How does this tie into ARM NEON vector registers?",
                    "What happens if our calibration range r_max is set too low?",
                    "Why doesn't INT8 reduce accuracy by 75%?"
                )
            )
        } else {
            TutorResponse(
                text = "Great question! Let's work backwards from first principles. If you were designing a system for $topic from scratch, what invariant must never be violated?",
                conceptExplanation = "Consider how changing the input scale influences latency and memory. What trade-off are you willing to accept?",
                keyTakeaways = listOf(
                    "Socratic inquiry builds robust mental models that survive edge cases.",
                    "Formulate your hypothesis before checking the textbook solution."
                ),
                followUpPrompts = listOf(
                    "Give me a hint on the first step.",
                    "What would happen in the worst-case scenario?"
                )
            )
        }
    }

    private fun generateDebugResponse(q: String, topic: String, req: TutorRequest): TutorResponse {
        return TutorResponse(
            text = "Let's inspect potential edge bugs and numerical instability in $topic. A common bug in edge integer quantization is integer overflow during accumulation and unaligned vector loads.",
            conceptExplanation = "When multiplying two 8-bit integers (e.g. weight × activation), the result requires up to 16 bits. If you sum 1024 products in an INT8 accumulator, it will immediately overflow!",
            codeSnippet = """
// Safe INT8 Vector Dot-Product with INT32 Accumulation:
#include <arm_neon.h>

int32_t dot_product_int8(const int8_t* a, const int8_t* b, int count) {
    int32x4_t acc = vdupq_n_s32(0); // 32-bit vector accumulator
    for (int i = 0; i < count; i += 16) {
        int8x16_t va = vld1q_s8(a + i);
        int8x16_t vb = vld1q_s8(b + i);
        // Multiply and accumulate into 32-bit registers to prevent overflow
        acc = vdotq_s32(acc, va, vb); 
    }
    return vaddvq_s32(acc); // Horizontal sum
}
            """.trimIndent(),
            codeLanguage = "C++ (ARM NEON)",
            steps = listOf(
                "Verify that inputs 'a' and 'b' are 16-byte memory aligned.",
                "Always accumulate 8-bit MAC results into 32-bit integer registers.",
                "Re-quantize output back to INT8 using the layer's output scale before the next layer."
            ),
            keyTakeaways = listOf(
                "Never accumulate intermediate products in an 8-bit register.",
                "Use ARM NEON vdotq_s32 intrinsics for high-throughput 4-way parallel dot products."
            ),
            followUpPrompts = listOf(
                "How do I prevent zero-point subtraction overhead in inner loops?",
                "What is Per-Channel Quantization versus Per-Tensor?",
                "How do I profile cache misses on an ARM Cortex core?"
            )
        )
    }

    private fun generatePracticeResponse(q: String, topic: String, req: TutorRequest): TutorResponse {
        return TutorResponse(
            text = "Here is a quick conceptual drill for $topic to verify your mastery:",
            conceptExplanation = "Hardware Scenario: You have an ARM Cortex-M55 microcontroller with 512KB of on-chip SRAM. Your FP32 MobileNet model weights occupy 1.6MB. Can this model run without an external DRAM chip?",
            practiceQuestion = "Scenario: After applying INT8 quantization, what is the new weight footprint, and does it fit in the 512KB SRAM?",
            steps = listOf(
                "Original FP32 size = 1.6 MB = 1,600 KB.",
                "Calculate INT8 compression ratio: 8 bits / 32 bits = 1/4 = 0.25.",
                "Quantized size = 1,600 KB / 4 = 400 KB.",
                "Compare 400 KB against 512 KB SRAM ceiling: 400 KB < 512 KB!"
            ),
            keyTakeaways = listOf(
                "Yes! INT8 compresses the 1.6MB model down to 400KB, allowing it to fit with 112KB left over for activation buffers and stack.",
                "This eliminates the need for expensive, power-hungry external DRAM."
            ),
            followUpPrompts = listOf(
                "What if activations also take 150KB?",
                "How does layer-by-layer buffer ping-ponging work?",
                "Give me another drill question!"
            )
        )
    }

    private fun generateReviewResponse(q: String, topic: String, req: TutorRequest): TutorResponse {
        return TutorResponse(
            text = "Here is your executive review and milestone recap for $topic:",
            conceptExplanation = "Quantization and edge deployment summarize into three essential pillars: Memory Footprint, Memory Bandwidth, and Integer Vector Math.",
            formula = "Affine Transformation:\nq = clip(round(r / S) + Z, q_min, q_max)\nr = S × (q - Z)",
            steps = listOf(
                "Scale Factor S = (r_max - r_min) / (q_max - q_min)",
                "Zero-Point Z = round(q_min - r_min / S)",
                "Symmetric Quantization sets Z = 0, simplifying hardware MACs."
            ),
            keyTakeaways = listOf(
                "INT8 reduces model size and DRAM transfer energy by 75%.",
                "Calibration dataset (100-200 samples) determines the optimal dynamic range [r_min, r_max].",
                "ARM NEON SIMD acceleration provides up to 4x throughput over scalar FP32."
            ),
            followUpPrompts = listOf(
                "What are the top 3 interview questions on edge AI quantization?",
                "How do we detect if a layer requires float fallback?",
                "I'm ready to resume the practical lesson!"
            )
        )
    }
}

// -------------------------------------------------------------
// Tutor Session State & Repository
// -------------------------------------------------------------
object TutorRepository {
    private val engine: TutorEngine = PrototypeTutorEngine()

    var activeMode by mutableStateOf(TutorMode.SOCRATIC)
    var isGenerating by mutableStateOf(false)
    val conversationMessages = mutableStateListOf<TutorConversationMessage>()

    fun initializeSession(
        learnerName: String,
        domainTitle: String,
        currentTopic: String,
        initialPrompt: TutorQuickPrompt? = null
    ) {
        if (conversationMessages.isEmpty()) {
            val welcome = engine.getWelcomeMessage(learnerName, domainTitle, currentTopic, activeMode)
            conversationMessages.add(welcome)

            if (initialPrompt != null) {
                // Auto-submit initial prompt if provided from Lesson or Home
                sendUserQuery(
                    query = initialPrompt.prompt,
                    domainId = "ai_ml",
                    domainTitle = domainTitle,
                    currentTopic = currentTopic,
                    goal = "Build Real Edge AI Projects",
                    skillLevel = DifficultyLevel.INTERMEDIATE,
                    learningStyle = "Hands-on Projects & Code"
                )
            }
        }
    }

    fun setMode(mode: TutorMode, learnerName: String, domainTitle: String, currentTopic: String) {
        activeMode = mode
        val switchMsg = TutorConversationMessage(
            id = "mode_switch_${System.currentTimeMillis()}",
            sender = "ADAPT-AI",
            isTutor = true,
            content = "Switched to ${mode.title} Mode (${mode.iconEmoji}). ${mode.description}.",
            mode = mode,
            structuredPayload = TutorResponse(
                text = "Switched to ${mode.title} Mode.",
                conceptExplanation = mode.description,
                followUpPrompts = engine.getSuggestedPrompts("ai_ml", currentTopic, mode)
            )
        )
        conversationMessages.add(switchMsg)
        notifyPersistenceChanged()
    }

    suspend fun sendUserQueryAsync(
        query: String,
        domainId: String,
        domainTitle: String,
        currentTopic: String,
        goal: String,
        skillLevel: DifficultyLevel,
        learningStyle: String
    ) {
        if (query.isBlank() || isGenerating) return

        val userMsg = TutorConversationMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "You",
            isTutor = false,
            content = query.trim(),
            mode = activeMode
        )
        conversationMessages.add(userMsg)
        notifyPersistenceChanged()
        isGenerating = true

        try {
            val request = TutorRequest(
                query = query.trim(),
                mode = activeMode,
                domainId = domainId,
                domainTitle = domainTitle,
                topicTitle = currentTopic,
                goal = goal,
                skillLevel = skillLevel,
                learningStyle = learningStyle,
                conversationHistory = conversationMessages.toList()
            )

            val response = engine.generateResponse(request)
            val tutorMsg = TutorConversationMessage(
                id = "tutor_${System.currentTimeMillis()}",
                sender = "ADAPT-AI",
                isTutor = true,
                content = response.text,
                mode = activeMode,
                structuredPayload = response
            )
            conversationMessages.add(tutorMsg)
            notifyPersistenceChanged()
        } catch (e: Exception) {
            val errorMsg = TutorConversationMessage(
                id = "error_${System.currentTimeMillis()}",
                sender = "ADAPT-AI",
                isTutor = true,
                content = "Could not generate response. Tap retry to run inference again.",
                mode = activeMode,
                isError = true
            )
            conversationMessages.add(errorMsg)
            notifyPersistenceChanged()
        } finally {
            isGenerating = false
        }
    }

    fun sendUserQuery(
        query: String,
        domainId: String,
        domainTitle: String,
        currentTopic: String,
        goal: String,
        skillLevel: DifficultyLevel,
        learningStyle: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            sendUserQueryAsync(query, domainId, domainTitle, currentTopic, goal, skillLevel, learningStyle)
        }
    }

    fun clearSession(learnerName: String, domainTitle: String, currentTopic: String) {
        conversationMessages.clear()
        val welcome = engine.getWelcomeMessage(learnerName, domainTitle, currentTopic, activeMode)
        conversationMessages.add(welcome)
        notifyPersistenceChanged()
    }

    fun notifyPersistenceChanged() {
        if (AdaptPersistence.isInitialized) {
            AdaptPersistence.scheduleSaveTutor(activeMode, conversationMessages.toList())
        }
    }

    fun getSuggestedPrompts(domainId: String, currentTopic: String): List<String> {
        return engine.getSuggestedPrompts(domainId, currentTopic, activeMode)
    }
}
