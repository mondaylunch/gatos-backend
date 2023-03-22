# Gatos – Backend

This monorepo contains the backend component of Gatos.

**Contributors**

-   {names withheld}

## Deployed Software

The backend component is deployed at https://api.mondaylunch.club however users will not directly interact with this.

Please see the frontend README about how to access the software.

## Prerequisites

-   Install [Java 17 JDK or later](https://adoptium.net/en-GB/) (LTS recommended)
-   Install [Node.js 16 or later with npm](https://nodejs.org/en/)
-   Install [dotenv-cli](https://www.npmjs.com/package/dotenv-cli)
    ```bash
    npm i -g dotenv-cli
    ```

## Quick Start

You must provide some secrets in a `.env` file in the root of the project, the following variables are required ([learn more about how to create these here](https://docs.mondaylunch.club/deployment)):

```dotenv
AUTH0_CLIENT_ID=[Auth0 client ID]
AUTH0_CLIENT_SECRET=[Auth0 client secret]
AUTH0_ISSUER=[Auth0 issuer URL]
AUTH0_AUDIENCE=[Auth0 API audience]
AUTH0_TOKEN_URL=[Auth0 API token URL, looks like https://<domain>/oauth/token]
AUTH0_MANAGEMENT_AUDIENCE=[Auth0 Management API audience]
```

Start the API:

```bash
dotenv ./gradlew :api:bootRun
```

To run and generate a run configuration for the backend in IntelliJ you can open `api/src/main/java/club/mondaylunch/gatos/api/ApiApplication.java` and press the play button.

The API will be available at http://localhost:8080.

## Building for Production

This repository comes equipped with CI to automatically build production Docker images from the main branch.

To build this yourself, simply run:

```bash
docker build -t ghcr.io/mondaylunch/gatos-backend-api:master -f Dockerfile.api .
```

You may want to use a different tag, but this will replace your deployment image if you are using the default configuration.

## Run Tests

```bash
./gradlew :api:test
```

To run and generate a run configuration for the backend tests in IntelliJ, `ctrl` click all the `test` folders in all the modules, right-click one of them, and select `Run 'All Tests'`.

## Reference List

-   **Spring Boot**: Framework for building web applications available under Apache-2.0 License. Source: (https://spring.io/projects/spring-boot)
-   **GSON**: Java serialisation library under Apache-2.0 License. Source: (https://github.com/google/gson)
-   **JUnit**: Unit testing library under Eclipse Public License 2.0. Source: (https://junit.org/junit5/docs/current/user-guide/)
-   **Bouncy Castle Encryption**: Argon2 encryption library under MIT License. Source: (https://www.bouncycastle.org/)
-   **Jackson**: JSON serialisation library under Apache-2.0 License. Source: (https://github.com/FasterXML/jackson)
-   **Guava**: Core libraries provided by Google under Apache-2.0 License. Source: (https://guava.dev/)

Some code has been taken from library documentation, this is mentioned where relevant in the code.
