# Gatos Backend

## Running API Server

First, you must provide some secrets in a `secrets.properties` file in the root of the project. The following variables are required:

```properties
AUTH0_CLIENT_ID=[Auth0 client ID]
AUTH0_CLIENT_SECRET=[Auth0 client secret]
AUTH0_ISSUER=[Auth0 issuer URL]
AUTH0_AUDIENCE=[Auth0 API audience]
AUTH0_TOKEN_URL=[Auth0 API token URL, looks like https://<domain>/oauth/token]
AUTH0_MANAGEMENT_AUDIENCE=[Auth0 Management API audience]
```

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
