# Learning engine

Learners can save multiple interests and select a topic for lessons, questions, notes and progress. Search covers offline subject labels, topics and subtopics. Custom topics accept a learner-defined objective and editable subtopic list.

A topic starts Unassessed. Self-reported proficiency directs prompt tone. Diagnostic results are stored separately from practice and excluded from topic mastery. Completed practice attempts contribute the 30 newest valid answers. The calculation weights beginner, intermediate and advanced questions by 1, 1.5 and 2, then multiplies by `1 / (1 + ageDays / 30)`. Mastery is the rounded percentage of weighted answers marked correct. Fewer than five valid answers are marked low confidence.

A disputed question is excluded from mastery, displayed scores and revision scheduling. The next recommendation prioritizes due revision, then a measured weakness, then an incomplete lesson or a fresh assessment. Revision uses one day after an incorrect attempt. Successive fully correct attempts increase the interval to two, four, eight and sixteen days.

The study planner uses the next seven dates, preferred study days, deadline and exact daily minutes. It schedules due revision on the first eligible date and fills available days with topic concepts. The learner can edit schedule and budget in preferences. No background time is counted by the active lesson timer.
