# Testing rules

- No comments in tests unless the test is very complex.
- Give names to things and keep tests short — extract helpers (e.g., `createMountpoint`, `createFolder`) and use builders where it helps readability.
- No mock libraries (Mockito, etc.). Keep tests as real as possible using actual infrastructure. Only fake things when truly unavoidable.
- Split tests by layer: `*ResourceIT` for HTTP-level happy path and failure responses, `*ServiceIT` for edge cases and business logic.
