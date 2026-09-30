# ACTA architecture

The UI is state-driven Compose. `ActaViewModel` coordinates user actions, `ActaRepository` owns business rules and audit entries, and Room stores meetings, participants, sessions, transcript segments, minutes, actions and audit records. Gemini is optional and receives structured local data through `GeminiMinutesService`.

The consent gate and versioning rules are covered by unit tests. Android instrumentation and physical installation must be re-run on the target machine before claiming device-level validation.
