package com.example.adapt_ai_ui.data

object DomainRepository {

    val domains: List<LearningDomain> = listOf(
        // 1. Artificial Intelligence & Machine Learning (Default Track)
        LearningDomain(
            id = "ai_ml",
            title = "Artificial Intelligence & ML",
            tagline = "On-Device Neural Networks, Edge Inference & Quantization",
            description = "Master machine learning from deep neural network architectures down to hardware-constrained edge deployment on ARM SoCs and NPUs.",
            iconEmoji = "🤖",
            suggestedTopics = listOf(
                "INT8 Weight Quantization",
                "MobileNet & EfficientNet",
                "ARM NEON SIMD Vectorization",
                "TensorFlow Lite & ExecuTorch",
                "On-Device Attention & Transformers"
            ),
            recommendedGoals = listOf(
                "Deploy Neural Networks on Low-Power ARM Chips",
                "Optimize Models for Minimal Latency & Energy",
                "Master Model Compression & Quantization",
                "Transition into Edge AI Engineering"
            ),
            estimatedHours = 28,
            defaultPathwayNodes = listOf(
                PathwayNode("node_1", 1, "MobileNet Architecture", "Model Compression", MilestoneStatus.COMPLETED, 15),
                PathwayNode("node_2", 2, "INT8 Weight Quantization", "Precision Scaling", MilestoneStatus.IN_PROGRESS, 12),
                PathwayNode("node_3", 3, "Tensor Memory Layouts", "Cache Optimization", MilestoneStatus.LOCKED, 20),
                PathwayNode("node_4", 4, "Edge Pipeline Benchmark", "Profiling & Latency", MilestoneStatus.LOCKED, 25)
            ),
            defaultModule = LearningModule(
                id = "mod_quant_1",
                title = "INT8 Weight Quantization",
                summary = "Learn how converting 32-bit floating point weights into 8-bit integers accelerates edge neural networks while reducing memory bandwidth.",
                trackName = "Edge AI Systems",
                estimatedMinutes = 12,
                currentStep = 3,
                totalSteps = 4,
                difficulty = DifficultyLevel.INTERMEDIATE
            ),
            diagnosticQuestions = listOf(
                DiagnosticQuestion(
                    id = "diag_ai_1",
                    question = "What is the primary motivation for quantizing neural network weights to 8-bit integers on edge devices?",
                    options = listOf(
                        "To reduce model file size and memory bandwidth consumption",
                        "To increase network parameter count",
                        "To eliminate the need for training data",
                        "To run floating-point matrix multiplication faster"
                    ),
                    correctIndex = 0,
                    explanation = "INT8 quantization cuts parameter memory footprint by 75% and saves DRAM fetch energy."
                ),
                DiagnosticQuestion(
                    id = "diag_ai_2",
                    question = "Which hardware unit on modern ARM mobile processors accelerates integer matrix convolutions?",
                    options = listOf(
                        "Audio DSP",
                        "Dedicated NPU / NEON Vector Engine",
                        "Camera Image Sensor",
                        "Baseband Controller"
                    ),
                    correctIndex = 1,
                    explanation = "ARM NEON vector engines and NPUs execute multiple packed integer operations per clock cycle."
                )
            )
        ),

        // 2. Computer Science
        LearningDomain(
            id = "cs",
            title = "Computer Science",
            tagline = "Data Structures, Algorithms & Systems Architecture",
            description = "Explore foundational computer science principles, operating systems concepts, memory management, and algorithmic complexity.",
            iconEmoji = "💻",
            suggestedTopics = listOf(
                "Algorithmic Complexity & Big-O",
                "Trees, Graphs & Dynamic Programming",
                "Memory Virtualization & Paging",
                "Concurrency, Threads & Mutexes",
                "Computer Architecture & Cache Hierarchies"
            ),
            recommendedGoals = listOf(
                "Crack Coding Interviews & Problem Solving",
                "Understand Low-Level System Internals",
                "Design Scalable High-Performance Systems",
                "Master Core CS Degree Fundamentals"
            ),
            estimatedHours = 34,
            defaultPathwayNodes = listOf(
                PathwayNode("cs_1", 1, "Asymptotic Analysis & Big-O", "Algorithms", MilestoneStatus.COMPLETED, 15),
                PathwayNode("cs_2", 2, "Binary Trees & BSTs", "Data Structures", MilestoneStatus.IN_PROGRESS, 20),
                PathwayNode("cs_3", 3, "Virtual Memory & Paging", "Operating Systems", MilestoneStatus.LOCKED, 25),
                PathwayNode("cs_4", 4, "Concurrent Lock-Free Queues", "Systems Architecture", MilestoneStatus.LOCKED, 30)
            ),
            defaultModule = LearningModule(
                id = "mod_cs_trees",
                title = "Balanced Binary Search Trees",
                summary = "Explore AVL and Red-Black tree rebalancing mechanisms for maintaining logarithmic lookup guarantees.",
                trackName = "Computer Science Core",
                estimatedMinutes = 20,
                currentStep = 2,
                totalSteps = 5,
                difficulty = DifficultyLevel.INTERMEDIATE
            ),
            diagnosticQuestions = listOf(
                DiagnosticQuestion(
                    id = "diag_cs_1",
                    question = "What is the worst-case search time complexity of a balanced binary search tree with N elements?",
                    options = listOf("O(1)", "O(log N)", "O(N)", "O(N log N)"),
                    correctIndex = 1,
                    explanation = "Balanced BSTs (such as AVL or Red-Black trees) guarantee O(log N) height and search time."
                )
            )
        ),

        // 3. Mathematics
        LearningDomain(
            id = "math",
            title = "Mathematics",
            tagline = "Linear Algebra, Calculus & Discrete Probability",
            description = "Build deep intuitive fluency in the mathematical foundations of modern technology, cryptography, and artificial intelligence.",
            iconEmoji = "📐",
            suggestedTopics = listOf(
                "Vectors, Matrices & Eigenvalues",
                "Multivariate Calculus & Gradients",
                "Bayesian Probability & Distributions",
                "Discrete Math & Graph Theory",
                "Optimization Techniques (SGD, Adam)"
            ),
            recommendedGoals = listOf(
                "Master Linear Algebra for Machine Learning",
                "Understand Gradient Descent & Loss Landscapes",
                "Gain Confidence in College-Level Math",
                "Apply Statistical Modeling to Real Data"
            ),
            estimatedHours = 30,
            defaultPathwayNodes = listOf(
                PathwayNode("math_1", 1, "Vector Spaces & Dot Products", "Linear Algebra", MilestoneStatus.COMPLETED, 15),
                PathwayNode("math_2", 2, "Matrix Transformations & Rank", "Linear Algebra", MilestoneStatus.IN_PROGRESS, 18),
                PathwayNode("math_3", 3, "Partial Derivatives & Jacobians", "Multivariate Calculus", MilestoneStatus.LOCKED, 22),
                PathwayNode("math_4", 4, "Singular Value Decomposition (SVD)", "Advanced Algebra", MilestoneStatus.LOCKED, 28)
            ),
            defaultModule = LearningModule(
                id = "mod_math_matrices",
                title = "Matrix Transformations & Rank",
                summary = "Learn how matrix transformations rotate and stretch coordinate spaces, and what matrix rank reveals about information preservation.",
                trackName = "Linear Algebra for AI",
                estimatedMinutes = 18,
                currentStep = 1,
                totalSteps = 3,
                difficulty = DifficultyLevel.INTERMEDIATE
            )
        ),

        // 4. Science
        LearningDomain(
            id = "science",
            title = "Science",
            tagline = "Physics, Quantum Fundamentals & Scientific Computing",
            description = "Explore physical systems, electromagnetism, semiconductor physics, and modern computational science simulation.",
            iconEmoji = "🔬",
            suggestedTopics = listOf(
                "Semiconductor Physics & Bandgaps",
                "Electromagnetism & Wave Optics",
                "Thermodynamics & Heat Dissipation",
                "Quantum Bits & Superposition Basics",
                "Computational Physics Simulations"
            ),
            recommendedGoals = listOf(
                "Understand How Silicon Transistors Work",
                "Study Energy & Thermal Laws in Computing",
                "Explore Quantum Mechanics Basics",
                "Bridge Physics with Hardware Engineering"
            ),
            estimatedHours = 24,
            defaultPathwayNodes = listOf(
                PathwayNode("sci_1", 1, "P-N Junctions & Diode Physics", "Semiconductors", MilestoneStatus.COMPLETED, 15),
                PathwayNode("sci_2", 2, "MOSFET Channel Inversion", "Device Physics", MilestoneStatus.IN_PROGRESS, 20),
                PathwayNode("sci_3", 3, "Thermal Joule Heating & Cooling", "Thermodynamics", MilestoneStatus.LOCKED, 20),
                PathwayNode("sci_4", 4, "Quantum Tunneling in Sub-3nm Chips", "Quantum Physics", MilestoneStatus.LOCKED, 30)
            ),
            defaultModule = LearningModule(
                id = "mod_sci_mosfet",
                title = "MOSFET Channel Inversion",
                summary = "Understand how gate voltages modulate conductive carrier channels in modern FinFET and Gate-All-Around transistors.",
                trackName = "Applied Semiconductor Physics",
                estimatedMinutes = 20,
                currentStep = 1,
                totalSteps = 4,
                difficulty = DifficultyLevel.INTERMEDIATE
            )
        ),

        // 5. Electronics & Embedded Systems
        LearningDomain(
            id = "embedded",
            title = "Electronics & Embedded Systems",
            tagline = "Microcontrollers, ARM Architecture, I/O & RTOS",
            description = "Design embedded software for bare-metal microcontrollers, ARM Cortex-M processors, sensor interfaces (I2C, SPI, UART), and real-time operating systems.",
            iconEmoji = "⚡",
            suggestedTopics = listOf(
                "ARM Cortex-M Registers & Assembly",
                "Interrupt Service Routines (NVIC)",
                "Hardware Protocols: I2C, SPI, CAN & UART",
                "Real-Time OS: FreeRTOS Task Scheduling",
                "Direct Memory Access (DMA) Controllers"
            ),
            recommendedGoals = listOf(
                "Write Bare-Metal Firmware in C and Rust",
                "Interface Sensors & Actuators in Real Time",
                "Build Battery-Powered IoT Devices",
                "Master ARM Embedded Processor Hardware"
            ),
            estimatedHours = 32,
            defaultPathwayNodes = listOf(
                PathwayNode("emb_1", 1, "Memory-Mapped I/O & Registers", "ARM Cortex-M", MilestoneStatus.COMPLETED, 15),
                PathwayNode("emb_2", 2, "Hardware Interrupts & NVIC", "Embedded Systems", MilestoneStatus.IN_PROGRESS, 18),
                PathwayNode("emb_3", 3, "DMA High-Speed Buffer Transfers", "System Peripherals", MilestoneStatus.LOCKED, 25),
                PathwayNode("emb_4", 4, "FreeRTOS Preemptive Multitasking", "Real-Time OS", MilestoneStatus.LOCKED, 30)
            ),
            defaultModule = LearningModule(
                id = "mod_emb_interrupts",
                title = "Hardware Interrupts & NVIC",
                summary = "Configure the Nested Vectored Interrupt Controller (NVIC) on ARM Cortex-M to handle sensor triggers with microsecond latency.",
                trackName = "ARM Embedded Systems",
                estimatedMinutes = 18,
                currentStep = 2,
                totalSteps = 4,
                difficulty = DifficultyLevel.INTERMEDIATE
            )
        ),

        // 6. Programming
        LearningDomain(
            id = "programming",
            title = "Programming & Software Engineering",
            tagline = "Kotlin, Modern C++, Rust & Software Design",
            description = "Develop rigorous software engineering skills with modern programming languages, type safety, memory management, and clean architecture.",
            iconEmoji = "⌨️",
            suggestedTopics = listOf(
                "Idiomatic Kotlin & Coroutines",
                "Modern C++ (C++20/23) & Smart Pointers",
                "Rust Ownership, Lifetimes & Safety",
                "Object-Oriented & Functional Design",
                "Test-Driven Development & Clean Architecture"
            ),
            recommendedGoals = listOf(
                "Master Idiomatic Kotlin for Android & KMP",
                "Write Fast, Safe Systems Code in Rust and C++",
                "Architect Maintainable Production Software",
                "Refactor Legacy Codebases Efficiently"
            ),
            estimatedHours = 26,
            defaultPathwayNodes = listOf(
                PathwayNode("prog_1", 1, "Kotlin Coroutines & Structured Concurrency", "Kotlin Core", MilestoneStatus.COMPLETED, 15),
                PathwayNode("prog_2", 2, "Modern C++ RAII & Memory Model", "Systems Programming", MilestoneStatus.IN_PROGRESS, 20),
                PathwayNode("prog_3", 3, "Rust Borrow Checker & Zero-Cost Abstractions", "Safe Systems", MilestoneStatus.LOCKED, 25),
                PathwayNode("prog_4", 4, "Clean Architecture & Dependency Injection", "Software Architecture", MilestoneStatus.LOCKED, 25)
            ),
            defaultModule = LearningModule(
                id = "mod_prog_cpp",
                title = "Modern C++ RAII & Memory Model",
                summary = "Master Resource Acquisition Is Initialization (RAII), move semantics, and unique_ptr/shared_ptr to eliminate memory leaks.",
                trackName = "Modern Systems Programming",
                estimatedMinutes = 20,
                currentStep = 1,
                totalSteps = 3,
                difficulty = DifficultyLevel.ADVANCED
            )
        ),

        // 7. Data Science
        LearningDomain(
            id = "data_science",
            title = "Data Science & Analytics",
            tagline = "Statistical Modeling, Pandas, ETL & Visualization",
            description = "Extract actionable intelligence from large datasets through exploratory data analysis, statistical hypothesis testing, and pipeline automation.",
            iconEmoji = "📊",
            suggestedTopics = listOf(
                "Exploratory Data Analysis (EDA)",
                "Feature Engineering & Imputation",
                "Hypothesis Testing & A/B Experiments",
                "Time-Series Forecasting",
                "Data Pipelines & Vector Databases"
            ),
            recommendedGoals = listOf(
                "Transform Raw Datasets into Clean Insights",
                "Conduct Rigorous Scientific A/B Tests",
                "Build End-to-End Data Pipelines",
                "Tell Persuasive Stories with Data Visualizations"
            ),
            estimatedHours = 22,
            defaultPathwayNodes = listOf(
                PathwayNode("ds_1", 1, "Statistical Summary & Distributions", "Foundations", MilestoneStatus.COMPLETED, 15),
                PathwayNode("ds_2", 2, "Feature Scaling & Missing Data", "Data Wrangling", MilestoneStatus.IN_PROGRESS, 18),
                PathwayNode("ds_3", 3, "A/B Testing & P-Value Confidence", "Experimental Design", MilestoneStatus.LOCKED, 22),
                PathwayNode("ds_4", 4, "Vector Embeddings & Semantic Search", "Advanced Analytics", MilestoneStatus.LOCKED, 25)
            ),
            defaultModule = LearningModule(
                id = "mod_ds_features",
                title = "Feature Scaling & Missing Data",
                summary = "Learn robust strategies for normalizing multimodal data, handling outliers, and applying KNN/MICE imputation.",
                trackName = "Data Science & Analytics",
                estimatedMinutes = 18,
                currentStep = 1,
                totalSteps = 3,
                difficulty = DifficultyLevel.BEGINNER
            )
        ),

        // 8. Languages
        LearningDomain(
            id = "languages",
            title = "Languages & Linguistics",
            tagline = "Grammar Structures, Phonetics & Conversational Fluency",
            description = "Accelerate natural language acquisition through spaced vocabulary retention, phonetic pronunciation patterns, and contextual immersion.",
            iconEmoji = "🌍",
            suggestedTopics = listOf(
                "Phonetic Systems & Accent Training",
                "High-Frequency Core Vocabulary",
                "Grammatical Conjugations & Cases",
                "Conversational Idioms & Expressions",
                "Spaced Repetition Flashcard Drills"
            ),
            recommendedGoals = listOf(
                "Achieve Conversational Fluency",
                "Master Accurate Pronunciation & Phonetics",
                "Read Technical Documentation in a Second Language",
                "Retain Vocabulary with Spaced Repetition"
            ),
            estimatedHours = 35,
            defaultPathwayNodes = listOf(
                PathwayNode("lang_1", 1, "Phonetics & Sound Recognition", "Pronunciation", MilestoneStatus.COMPLETED, 15),
                PathwayNode("lang_2", 2, "Essential 500 Core Words", "Vocabulary", MilestoneStatus.IN_PROGRESS, 15),
                PathwayNode("lang_3", 3, "Sentence Patterns & Verb Conjugation", "Grammar", MilestoneStatus.LOCKED, 20),
                PathwayNode("lang_4", 4, "Contextual Dialogue & Real Listening", "Immersion", MilestoneStatus.LOCKED, 25)
            ),
            defaultModule = LearningModule(
                id = "mod_lang_vocab",
                title = "Essential 500 Core Words",
                summary = "Acquire the foundational high-frequency vocabulary that constitutes 70% of spoken everyday conversation.",
                trackName = "Applied Language Acquisition",
                estimatedMinutes = 15,
                currentStep = 2,
                totalSteps = 5,
                difficulty = DifficultyLevel.BEGINNER
            )
        ),

        // 9. Custom Learning Domain
        LearningDomain(
            id = "custom",
            title = "Custom Learning Domain",
            tagline = "Design your own personalized learning syllabus",
            description = "Create an adaptable personalized learning path tailored specifically to your unique goals, curriculum, or technical project.",
            iconEmoji = "✨",
            suggestedTopics = listOf(
                "Custom Topic 1: Core Fundamentals",
                "Custom Topic 2: Applied Techniques",
                "Custom Topic 3: Project Architecture",
                "Custom Topic 4: Mastery & Evaluation"
            ),
            recommendedGoals = listOf(
                "Tailor Study Material to University Coursework",
                "Prepare for a Specific Technical Certification",
                "Build an Autonomous Independent Project",
                "Follow a Self-Paced Reading Curriculum"
            ),
            estimatedHours = 20,
            defaultPathwayNodes = listOf(
                PathwayNode("cust_1", 1, "Core Concepts Foundation", "Custom Track", MilestoneStatus.COMPLETED, 15),
                PathwayNode("cust_2", 2, "Applied Methods & Tooling", "Custom Track", MilestoneStatus.IN_PROGRESS, 20),
                PathwayNode("cust_3", 3, "Real-World Project Build", "Custom Track", MilestoneStatus.LOCKED, 25),
                PathwayNode("cust_4", 4, "Final Review & Assessment", "Custom Track", MilestoneStatus.LOCKED, 30)
            ),
            defaultModule = LearningModule(
                id = "mod_custom_core",
                title = "Applied Methods & Tooling",
                summary = "Deep dive into your selected custom study focus with adaptive Socratic guidance.",
                trackName = "Custom Personalized Path",
                estimatedMinutes = 20,
                currentStep = 1,
                totalSteps = 3,
                difficulty = DifficultyLevel.BEGINNER
            ),
            isCustom = true
        )
    )

    fun getDomainById(id: String): LearningDomain {
        val base = domains.firstOrNull { it.id == id } ?: domains.first()
        if (base.isCustom && SampleData.customTopics.isNotEmpty()) {
            return base.copy(suggestedTopics = SampleData.customTopics)
        }
        return base
    }

    fun getQuickPromptsForDomain(domainId: String): List<TutorQuickPrompt> {
        if (domainId == "ai_ml") {
            return listOf(
                TutorQuickPrompt("p1", "Why does INT8 save 4x memory over FP32?", "Concept"),
                TutorQuickPrompt("p2", "What is post-training quantization?", "Quantization"),
                TutorQuickPrompt("p3", "How does zero-point offset work?", "Math")
            )
        }
        val domain = getDomainById(domainId)
        val topics = domain.suggestedTopics
        return listOf(
            TutorQuickPrompt(
                id = "${domainId}_p1",
                prompt = "Why is ${topics.getOrElse(0) { "this concept" }} foundational?",
                tag = "Concept"
            ),
            TutorQuickPrompt(
                id = "${domainId}_p2",
                prompt = "Walk me through ${topics.getOrElse(1) { "practical implementation" }} with an example",
                tag = "Deep Dive"
            ),
            TutorQuickPrompt(
                id = "${domainId}_p3",
                prompt = "What are common pitfalls in ${topics.getOrElse(2) { "this topic" }}?",
                tag = "Debug"
            )
        )
    }
}
