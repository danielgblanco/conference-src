# Conference Demos

Source code used in different talks at conferences.

## Project Structure

This is a multi-project Gradle build containing independent conference demos:

```
conference-src/
├── kcd-uk-2025/           # KCD Edinburgh 2025 demo
├── kubecon-na-2025/       # KubeCon NA 2025 - Trace splitting demo
├── settings.gradle        # Multi-project configuration
└── build.gradle           # Root build configuration
```

## IntelliJ IDEA Setup

To open this repository in IntelliJ IDEA:

1. **Close the current project** if you have it open
2. **Open the project**: `File → Open` and select the `conference-src` directory
3. IntelliJ will automatically detect the Gradle multi-project configuration
4. Both subprojects will appear in the Project view as separate modules
5. You can run tasks for each project independently

## Working with Subprojects

### List all projects
```bash
./gradlew projects
```

### Run a specific project
```bash
./gradlew :kubecon-na-2025:run
./gradlew :kcd-uk-2025:run
```

### Build all projects
```bash
./gradlew build
```

### Work on a specific project
Each subproject has its own README with specific instructions:
- [kubecon-na-2025/README.md](kubecon-na-2025/README.md)

## Adding New Conference Demos

1. Create a new directory for your conference: `mkdir conference-name-year`
2. Add your Gradle project files inside
3. Update `settings.gradle` to include the new project:
   ```gradle
   include 'conference-name-year'
   ```
4. IntelliJ will automatically detect the new subproject

## Notes

- Each subproject is independent and can have different dependencies
- Each subproject can be built and run separately
- The root project serves only as a container for organization
