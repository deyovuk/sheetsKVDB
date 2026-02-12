# Sheets KV REST Wrapper

Quickstart guide for running the Google Sheets-backed KV API locally.

## Purpose

This project exposes a simple Key–Value REST API backed by a single Google Spreadsheet.
It is intended as a lightweight proof-of-concept to store and retrieve small datasets
without running a database server. Each sheet tab is treated as a collection, and
keys/values are stored in columns A and B.

## Prerequisites

- Java 17+
- Maven 3.9+
- A public Google Spreadsheet with edit access for “anyone with the link”
- Google Sheets API key (for read-only access)
- Service account JSON (for write access without UI login)
- Sheet structure requirements:
    - Each tab represents a collection
    - Column A = key (id), Column B = value (string)

## Configure

Edit [src/main/resources/application.yml](src/main/resources/application.yml):

- `sheet.publicUrl`: full Google Sheets URL (or spreadsheet id)
- `sheet.apiKey`: Google API key
- `sheet.serviceAccountJsonPath`: path to a service account JSON file (optional)

Classpath support:

- If the JSON is placed in `src/main/resources`, set the value as `classpath:<file>`.
    Example: `classpath:service-account.json` or `classpath:keys/my-sa.json`.

### How to get the Google Sheets API key

1. Go to Google Cloud Console: https://console.cloud.google.com/
2. Create or select a project.
3. Enable the Google Sheets API (APIs & Services → Library → Google Sheets API → Enable).
4. Create credentials (APIs & Services → Credentials → Create Credentials → API key).
5. Optional: Restrict the key (HTTP referrers or IPs) and limit it to the Sheets API.

### Sheet ownership and sharing

- The sheet does not need to be owned by the same project or organization that created the API key.
- The sheet must be shared to allow access; for writes, set “anyone with the link can edit”.

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

### Create a service account (no UI login)

1. Open Google Cloud Console: https://console.cloud.google.com/
2. Create or select a project.
3. Enable the Google Sheets API (APIs & Services → Library → Google Sheets API → Enable).
4. Create a service account (IAM & Admin → Service Accounts → Create).
5. Create a JSON key for the service account (Keys → Add Key → Create new key → JSON).
6. Share the target spreadsheet with the service account email as an Editor.
7. Set `sheet.serviceAccountJsonPath` to the JSON file path.

## Run

From the project root:

    mvn spring-boot:run

Service starts at `http://localhost:8080`.

## Verify

- Health: `GET /v1/health`
- Flush: `POST /v1/flush`
- Swagger UI: `/swagger-ui.html`

## Test with Postman

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
