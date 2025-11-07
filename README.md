# USTH GitHub Client

This project is a native **Android** application developed as a client for the **GitHub API**. It provides a mobile interface for users to authenticate and interact with GitHub services.

## Project Idea

The core idea of the **USTH GitHub Client** is to offer a simple and functional mobile experience for GitHub.

### Primary Features

- **PAT Authentication**: Users log in using a GitHub **Personal Access Token (PAT)** rather than a traditional username/password. The application securely stores and uses this token for all subsequent API requests.
- **GitHub API Interaction**: The app communicates with the official GitHub API (`https://api.github.com/`) to fetch and display data.
- **Core Functionality**: Based on the activities defined (`AuthenticationActivity`, `MainActivity`, `UserProfileActivity`), the app allows users to:
  - Log in
  - View a main screen (likely for repositories or user activity)
  - Browse specific user profiles

## Project Structure

The application is built using a modern Android architecture and libraries, centered around the **MVVM (Model–View–ViewModel)** pattern.

### 1. View (UI Layer)

- **Activities & Fragments** act as UI controllers:
  - `AuthenticationActivity`
  - `MainActivity`
  - `UserProfileActivity`
- **ViewBinding** is enabled (`viewBinding = true`) for safer, easier interaction with UI components.

### 2. ViewModel

- Classes like `AuthViewModel` hold and manage UI-related data.
- They expose data via **LiveData** (`androidx.lifecycle:lifecycle-livedata`) so the UI always reflects the current state.

### 3. Model (Data Layer)

- **Networking**:
  - Handled by **Retrofit** (`com.squareup.retrofit2:retrofit`) for API declaration.
  - **OkHttp** (`com.squareup.okhttp3:logging-interceptor`) is used for executing requests and logging.
- **ApiClient (`ApiClient.java`)**:
  - Configures the Retrofit client.
  - Sets the base URL: `https://api.github.com/`
  - Uses an interceptor to inject the header: `Authorization: Bearer <TOKEN>` into every request.
- **Repository Pattern**:
  - Repositories such as `AuthRepository`, `RepoRepository`, and `UserRepository` abstract the data sources.
  - ViewModels call these repositories; repositories call the `ApiClient`.
- **Service Locator (`ServiceLocator.java`)**:
  - Provides singleton instances of repositories, mappers, and the `ApiClient`.
  - Avoids the complexity of a full DI framework (like Dagger/Hilt) for this project size.

## Setup and Build

Follow these steps to set up and build the project in **Android Studio**.

### Requirements

- **Android Studio**: latest stable version recommended
- **Java**: JDK 17
- **Android SDK**:
  - Compile SDK: **34**
  - Min SDK: **26**

### Build Steps

1. **Clone the Repository**  
   ```bash
   git clone https://github.com/nhu220840/usth-github-client.git
   ```

2. **Open in Android Studio**
   - Open Android Studio.
   - Select **Open** and navigate to the cloned project directory.
   - Trust the project if prompted.

3. **Gradle Sync**
   - Android Studio will automatically sync the project and download all required dependencies listed in `app/build.gradle.kts` (Retrofit, Glide, AndroidX, etc.).

4. **Generate a GitHub PAT**
   - Go to: **GitHub** → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**.
   - Generate a **new token**.
   - Give it appropriate scopes (for example: `user`, `repo`, `read:org`) depending on what the app needs.
   - **Important**: Copy the token immediately — GitHub will not show it again.

5. **Build and Run**
   - Connect an Android device (API 26+) or start an emulator.
   - Select the **`app`** run configuration and click **Run**.
   - When the app launches, enter the **PAT** you generated in the previous step to log in.

---

## License

MIT License. See [LICENSE](LICENSE) for details.

---

## Contact

For questions or contributions, please open an issue or contact:

- 📧 Email: [gianhuw.work@gmail.com](mailto:gianhuw.work@gmail.com)
- 💻 GitHub: [nhu220840](https://github.com/nhu220840)
