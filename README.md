# SmartFarm AI Assistant

An Android smart farming app integrating IoT sensor monitoring, plant recognition, AI assistant chat, and device management.

## Features

- User login and profile center
- Real-time environment monitoring (temperature, humidity, light, soil)
- Device management and status overview
- Plant image analysis and recognition
- AI chat assistant for farming guidance
- Mini-program style modules (tools, education, entertainment)

## Tech Stack

- Android (Java)
- Gradle (Kotlin DSL)
- OkHttp + Retrofit + Gson
- CameraX
- TensorFlow Lite + ML Kit OCR
- Filament (3D rendering)
- Room (local storage)

## Project Structure

- `app/src/main/java/com/linjiu/recognize` - main business logic
- `app/src/main/res` - UI layouts, drawables, values
- `app/src/main/assets` - model and media assets
- `docs` - project documents

## Quick Start

1. Open this project in Android Studio.
2. Ensure JDK 17 is configured.
3. Sync Gradle dependencies.
4. Connect an Android device or start an emulator.
5. Run the `app` module.

## Security Notes

- Disable local mock login for production builds.
- Avoid storing plaintext passwords locally.
- Prefer HTTPS backend endpoints in release environments.

## License

No license has been declared yet.
