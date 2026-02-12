# Sheets KV REST Wrapper

Google Sheets-backed Key–Value REST API. Lightweight proof-of-concept for storing
small datasets without running a database server.

## Overview

- Each sheet tab = collection
- Column A = key (id)
- Column B = value (string)

## Features

- CRUD for keys
- Collections management (tabs)
- Batch operations
- Flush endpoint (rebuild index)
- Swagger UI

## Tech Stack

- Java 17
- Spring Boot 3
- Maven
- Google Sheets API v4

## Getting Started

### Project Structure

- sheetsKV-core: reusable library with Google Sheets integration and core logic
- sheetsKV-service: Spring Boot HTTP service that depends on sheetsKV-core

### Prerequisites

- Java 17+
- Maven 3.9+
- A Google Spreadsheet shared for access
- Google Sheets API key (read-only)
- Service account JSON (write access without UI login)
- Sheet structure:
    - Each tab represents a collection
    - Column A = key (id), Column B = value (string)

### Configuration

Edit [sheetsKV-service/src/main/resources/application.yml](sheetsKV-service/src/main/resources/application.yml):

- `sheet.publicUrl`: full Google Sheets URL (or spreadsheet id)
- `sheet.apiKey`: Google API key
- `sheet.serviceAccountJsonPath`: path to a service account JSON file (optional)

Classpath support:

- If the JSON is placed in `src/main/resources`, set the value as `classpath:<file>`.
    Example: `classpath:service-account.json` or `classpath:keys/my-sa.json`.

Example:

        sheet:
            publicUrl: https://docs.google.com/spreadsheets/d/XXXX
            apiKey: YOUR_API_KEY
            serviceAccountJsonPath: /absolute/path/to/service-account.json

Example (classpath):

        sheet:
            publicUrl: https://docs.google.com/spreadsheets/d/XXXX
            apiKey: YOUR_API_KEY
            serviceAccountJsonPath: classpath:service-account.json

### API Key (read-only)

1. Go to Google Cloud Console: https://console.cloud.google.com/
2. Create or select a project.
3. Enable the Google Sheets API.
4. Create an API key (APIs & Services → Credentials).
5. Optional: restrict the key to the Sheets API.

### Service Account (write access, no UI login)

1. Open Google Cloud Console: https://console.cloud.google.com/
2. Create or select a project.
3. Enable the Google Sheets API.
4. Create a service account (IAM & Admin → Service Accounts → Create).
5. Create a JSON key (Keys → Add Key → Create new key → JSON).
6. Share the spreadsheet with the service account email as an Editor.
7. Set `sheet.serviceAccountJsonPath` to the JSON file path.

### Run

From the project root:

    mvn -pl sheetsKV-service spring-boot:run

Service starts at `http://localhost:8080`.

## Usage

- Health: `GET /v1/health`
- Flush: `POST /v1/flush`
- Swagger UI: `/swagger-ui.html`

## Postman

Import [postman_collection.json](postman_collection.json) and execute the requests.

## Notes

- The app builds an in-memory index on startup and via `/v1/flush`.
- The service buffers key-to-row mappings in memory so it can resolve keys quickly
    without scanning the sheet on every request.
- Changes made directly in Google Sheets are not immediately visible to the API until
    `/v1/flush` rebuilds the index. This keeps read performance predictable and minimizes
    API calls, at the cost of eventual consistency with external edits.
- Writes are sent directly to Google Sheets (no local write buffer). The `/v1/flush`
    endpoint does not commit writes; it only refreshes the in-memory index.
- API key access supports read-only. Write operations require service account OAuth
    (or user OAuth), and the sheet must be shared with the service account.
- This is an MVP: no auth, no concurrency handling.
