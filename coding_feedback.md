# Coding rules

- Use collaborators instead of static calls. Pass dependencies through the constructor (e.g., `Supplier<Provisioning>`) rather than calling static methods like `Provisioning.getInstance()` inside service classes.
- Give names to things and keep methods small. Extract meaningful helper methods (e.g., `getSharedAccountIds`) rather than inlining complex logic in a single method.
