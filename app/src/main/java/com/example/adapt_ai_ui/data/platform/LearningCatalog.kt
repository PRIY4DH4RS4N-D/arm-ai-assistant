package com.example.adapt_ai_ui.data.platform

import com.example.adapt_ai_ui.data.DifficultyLevel
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

data class LearningTopic(
    val id: String, val category: String, val subject: String, val name: String,
    val concepts: List<String>, val objective: String = "", val custom: Boolean = false
)

data class LearningPreferences(
    val interests: Set<String> = emptySet(),
    val customTopics: List<LearningTopic> = emptyList(),
    val activeTopic: String? = null,
    val goal: String = "",
    val level: DifficultyLevel? = null,
    val minutes: Int = 25,
    val studyDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val deadline: LocalDate? = null,
    val language: String = "English",
    val style: String = "Worked examples",
    val assistant: String = "Aurora",
    val recentTopics: List<String> = emptyList()
)

object LearningCatalog {
    private val seed = """
Science & mathematics|Mathematics|Algebra:Linear equations,Functions;Calculus:Limits,Derivatives,Integration;Geometry:Triangles,Coordinate geometry;Statistics:Probability,Sampling,Distribution
Science & mathematics|Physics|Mechanics:Motion,Forces,Energy;Electromagnetism:Fields,Circuits;Optics:Refraction,Lenses;Astronomy:Orbits,Stars
Science & mathematics|Chemistry|Organic chemistry:Functional groups,Reaction mechanisms;Inorganic chemistry:Periodic trends,Bonding
Science & mathematics|Biology|Cell biology:Cells,Organelles;Genetics:Inheritance,Genes;Anatomy:Organ systems;Environmental science:Ecosystems,Conservation
Technology|Programming|Python:Variables,Functions;C:Pointers,Arrays;C++:Classes,Templates;Java:Collections,Interfaces;Kotlin:Coroutines,Null safety
Technology|Computer Science|Data structures:Lists,Trees,Graphs;Algorithms:Search,Sorting,Complexity;Operating systems:Processes,Memory;Databases:SQL,Indexes;Computer architecture:Instruction sets,Caches
Technology|Artificial Intelligence|Machine learning:Training,Evaluation;Deep learning:Neural networks,Optimization;Computer vision:Images,Convolution;Edge AI:INT8 Weight Quantization,Memory Hierarchy
Technology|Software Development|Cybersecurity:Defensive principles,Authentication;Web development:HTML,HTTP;Mobile development:Android,Compose;Cloud computing:Storage,Compute;Data Science:Data cleaning,Visualisation
Engineering|Electronics Engineering|Digital design:Logic gates,Flip-flops;VLSI design:CMOS,Timing;Verilog:Modules,Combinational logic;Analog electronics:Transistors,Amplifiers;FPGA design:Logic blocks,Synthesis;Semiconductor technology:Fabrication,Packaging,Hybrid bonding
Engineering|ARM Architecture|ARM fundamentals:Load/store design,ARMv7,ARMv8,AArch64;AArch64 assembly:Registers,Instructions,Addressing modes;Calling conventions:AAPCS64,Stack frames,Register preservation;Exceptions and interrupts:Exception levels,Vector table,Interrupt handling;ARM memory model:Alignment,Barriers,Cache coherence;NEON and SIMD:Vector lanes,Intrinsics,Data layout;Performance:Pipeline hazards,Branch prediction,Cache misses
Engineering|Computer Architecture|Processor datapath:CPU,ALU,Control unit,Instruction cycle;RISC and CISC:Instruction encoding,Addressing,Microarchitecture;Pipelining:Stages,Forwarding,Hazards;Memory hierarchy:Cache,Locality,Virtual memory;Parallel architecture:SIMD,Multicore,Cache coherence
Engineering|Digital Electronics|Boolean algebra:Truth tables,De Morgan laws,Karnaugh maps;Combinational circuits:Adders,Multiplexers,Decoders;Sequential circuits:Flip-flops,Counters,Registers;Timing:Setup,Hold,Clock domains;Finite-state machines:Moore machines,Mealy machines,State encoding
Technology|Data Structures and Algorithms|Complexity:Big O,Amortized cost,Space usage;Linear structures:Arrays,Lists,Stacks,Queues;Trees and heaps:Traversal,Binary search trees,Priority queues;Graphs:BFS,DFS,Shortest paths;Problem solving:Recursion,Dynamic programming,Greedy algorithms
Technology|Operating Systems|Processes and threads:Scheduling,Context switches,Synchronization;Memory management:Paging,Virtual memory,Page faults;File systems:Inodes,Journaling,Permissions;Concurrency:Locks,Deadlocks,Condition variables
Engineering|Embedded Systems|Microcontrollers:Timers,Interrupts;Robotics:Sensors,Actuators;Signal processing:Sampling,Filters;Control systems:Feedback,Stability;Communications:Modulation,Bandwidth
Engineering|Mechanical Engineering|Statics:Free-body diagrams,Equilibrium;Thermodynamics:Heat,Work
Engineering|Civil Engineering|Structures:Loads,Beams;Surveying:Levels,Maps
Languages|Japanese|Hiragana:Vowels,Recognition;N5 grammar:Particles,Polite forms
Languages|English|Grammar:Sentence structure,Tenses;Writing:Paragraphs,Revision;Literature:Narration,Theme
Languages|Hindi|Devanagari:Vowels,Reading
Languages|French|Vocabulary:Greetings,Numbers
Languages|German|Grammar:Articles,Cases
Languages|Spanish|Conversation:Greetings,Present tense
Languages|Communication|Public speaking:Structure,Delivery;Communication skills:Listening,Feedback
Humanities|History|Ancient history:Civilisations,Sources;Modern history:Industrialisation,Global change
Humanities|Geography|Physical geography:Climate,Landforms
Humanities|Social Sciences|Economics:Supply,Demand;Psychology:Cognition,Memory;Philosophy:Logic,Ethics;Sociology:Institutions,Culture;Political science:Governance,Policy
Business|Finance|Financial modelling:Cash flow,Assumptions;Personal finance:Budgeting,Interest;Accounting:Ledgers,Balance sheets
Business|Business Management|Marketing:Positioning,Segmentation;Entrepreneurship:Customer discovery,Unit economics;Leadership:Delegation,Feedback;Career development:Portfolios,Interviews
Creative|Design|UI/UX Design:Research,Prototyping;Graphic Design:Typography,Composition;Digital Art:Colour,Layers
Creative|Photography|Exposure:Aperture,Shutter speed;Video editing:Continuity,Audio
Creative|Creative Skills|Music theory:Rhythm,Scales;Drawing:Perspective,Shading;Creative writing:Character,Dialogue
Exams|Competitive Examinations|GATE:Topic review,Practice;Aptitude:Ratios,Percentages;Reasoning:Patterns,Deduction
Exams|Certifications|Technical certifications:Objectives,Review;Interview preparation:Behavioural questions,Technical questions;School and university exams:Revision,Practice
    """.trimIndent()

    val topics: List<LearningTopic> = seed.lineSequence().flatMap { row ->
        val (category, subject, entries) = row.split('|')
        entries.split(';').asSequence().map { entry ->
            val (name, concepts) = entry.split(':')
            LearningTopic("${subject.slug()}/${name.slug()}", category, subject, name, concepts.split(','))
        }
    }.toList()

    fun search(query: String, category: String?, custom: List<LearningTopic>): List<LearningTopic> {
        val words = query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter(String::isNotEmpty)
        return (topics + custom).filter { topic ->
            (category == null || topic.category == category) &&
                words.all { word ->
                    (listOf(topic.subject, topic.name, topic.category) + topic.concepts)
                        .any { it.contains(word, ignoreCase = true) }
                }
        }
    }

    private fun String.slug(): String =
        lowercase(Locale.ROOT).replace("+", "-plus").replace(Regex("[^a-z0-9]+"), "-").trim('-')
}

data class StudyBlock(val date: LocalDate, val title: String, val minutes: Int)

fun studyWeek(topic: LearningTopic, preferences: LearningPreferences, revisionDue: LocalDate?, today: LocalDate): List<StudyBlock> {
    val dates = (0L..6L).map(today::plusDays).filter { date ->
        date.dayOfWeek in preferences.studyDays && (preferences.deadline?.let { date <= it } ?: true)
    }
    val reviewDate = revisionDue?.let { due -> dates.firstOrNull { it >= due } }
    return dates.mapIndexed { index, date ->
        val title = if (date == reviewDate) {
            "Review ${topic.name}"
        } else if (topic.concepts.isEmpty()) topic.name else topic.concepts[index % topic.concepts.size]
        StudyBlock(date, title, preferences.minutes.coerceIn(5, 180))
    }
}
