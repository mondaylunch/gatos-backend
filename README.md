# Gatos Backend

## Running API Server

Start the API:

```bash
./gradlew :api:bootRun
```

To run and generate a run configuration for the backend in IntelliJ you can open `api/src/main/java/club/mondaylunch/gatos/api/ApiApplication.java` and press the play button.

Navigate to http://localhost:4390

## Run Tests

```bash
./gradlew :api:test
```

To run and generate a run configuration for the backend tests in IntelliJ, `ctrl` click all the `test` folders in all the modules, right-click one of them, and select `Run 'All Tests'`.
