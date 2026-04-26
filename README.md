# DAPM-22-DoctorFinderApp-API

## SMTP security note
Never commit SMTP credentials into source code.

### If credentials were leaked
1. Revoke old Gmail App Password immediately: `https://myaccount.google.com/apppasswords`
2. Create a new App Password.
3. Put credentials in environment variables only (not in `application.properties`).
4. Rewrite Git history if secret was pushed before (BFG/filter-repo), then force-push.

## Local environment variables (example)
Use values from `./.env.example`:

```env
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-gmail@gmail.com
SPRING_MAIL_PASSWORD=your-gmail-app-password
APP_MAIL_FROM=your-gmail@gmail.com
APP_MAIL_DISPLAY_NAME=WEBPC Support
FIREBASE_CREDENTIALS_PATH=secrets/firebase-service-account.json
GEMINI_API_KEY=your-gemini-api-key
GEMINI_EMBEDDING_MODEL=gemini-embedding-2
```

## Swagger UI
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Doctor image search (Gemini embeddings)
- Endpoint: `POST /api/doctors/search-by-image` (multipart form-data, field `image`, optional `limit`).
- Requires `GEMINI_API_KEY` in environment variables.
