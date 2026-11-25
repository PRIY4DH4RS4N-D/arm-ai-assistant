# Local quiz engine

Quiz generation runs on the same installed Llama PTE and ExecuTorch runtime as Tutor. The generator requests one structured question per call, with up to two attempts per question. A quiz is saved only when the full requested count has passed validation. The app surfaces failures to enable retry.

The version-one envelope requires `v`, the exact `topic`, and an array `q`. Each question has prompt `p`, options `o`, zero-based answer index `a`, explanation `e`, and competency `c`. The validator checks strict JSON, question count, allowed fields, topic and competency, unique questions and options, answer indices and obvious answer disclosures. For Algebra, the model supplies the equation and the app solves it independently, creates distinct choices and validates the normalized object. General model-generated facts still require learner or educator review.

The options and validated answer key are saved with their model hash before play. Learners see explanations after completing the quiz. Kotlin evaluates answers and computes the percentage from valid responses. A learner can flag a question to exclude it from score, mastery and revision evidence. Choice questions and binary true/false responses are representable. Written rubric scoring and timed sessions are future work.
