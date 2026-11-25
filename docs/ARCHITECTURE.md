# Aurora architecture

```mermaid
flowchart TD
  UI[Jetpack Compose Tutor] --> VM[TutorViewModel]
  VM --> Repo[TutorRepository]
  Repo --> Engine[TutorEngine]
  Engine --> Adapter[ExecuTorchTutorEngine]
  Adapter --> Bridge[LlmModule and LlmCallback]
  Bridge --> CPU[ExecuTorch JNI and XNNPACK on ARM CPU]
  CPU --> Model[Llama PTE and tokenizer]
  Repo --> History[Private conversation JSON]
  Repo --> Curriculum[Bundled lesson and scored evidence]
  Manager[Profile Model Manager] --> Store[Private model store]
  Store --> Model
```

```mermaid
flowchart TD
  Profile[Name goal proficiency and daily budget] --> Journey[Domain and learning pathway]
  Journey --> Lesson[Local lesson]
  Lesson --> Practice[Scored practice]
  Practice --> Evidence[Attempts and topic accuracy]
  Evidence --> Revision[Mistake notebook and review intervals]
  Evidence --> Journey
  Revision --> Practice
```

The recovered branch has a single Android app module. Compose views observe shared state, and the existing persistence coordinator serializes writes for each key. Model binaries live under the application’s no-backup directory. A mutex serializes model import, generation, unloading and deletion. The repository persists completed responses and keeps active streaming output transient. When the module is missing, the Tutor surfaces the import requirement.
