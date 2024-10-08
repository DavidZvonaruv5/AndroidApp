---

# 🎯 **TaskFlow - User Management Application**

**TaskFlow** is a powerful Android application designed to streamline user management. With an intuitive interface, it allows users to view, add, edit, and delete user information effortlessly, featuring robust data synchronization and local storage capabilities.

---

## 🚀 **Getting Started**

### **Prerequisites**
Ensure you have the following tools installed:
- **Android Studio** (latest version recommended)
- **JDK 8** or higher
- **Android SDK** with minimum API level 21

### **Build and Run Instructions**
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/DavidZvonaruv5/AndroidApp.git
   ```
2. **Open the Project:**
    - Launch Android Studio and select "Open an Existing Project".
    - Navigate to the cloned repository and select the project folder.
3. **Sync and Index:**
    - Wait for the project to sync and index all files.
4. **Run the Application:**
    - Connect an Android device or start an emulator.
    - Click the **Run** button (green triangle) in Android Studio.

---

## ✨ **Features**

### 🏠 **User Dashboard**
- **Total Users**: View the total number of users.
- **Recent Additions**: Track the count of recently added users.
- **Inspirational Quotes**: Enjoy rotating inspirational quotes.

### 📋 **User List**
- **Scroll & View**: Access all users in a scrollable list.
- **Search**: Filter users easily with a search feature.
- **Sort Options**: Sort users by name, ID, or date added.

### 🛠️ **User Management**
- **Add Users**: Input first name, last name, email, and avatar.
- **Edit Users**: Modify existing user details.
- **Delete Users**: Remove users with a confirmation prompt.

### 👤 **User Details**
- **Detailed View**: Examine information on each user.
- **Update Info**: Edit user details seamlessly.
- **Delete User**: Remove users directly from the details screen.

### 🔄 **Data Synchronization**
- **API Sync**: Sync user data with a remote API.
- **Offline Access**: Store data locally for access without internet.

### 🎨 **UI/UX**
- **Material Design**: Utilize modern Material Design components.
- **Smooth Animations**: Enjoy animations for an enhanced experience.
- **Responsive Layout**: Adapts seamlessly to various screen sizes.

---

## 🛠️ **Technologies and Libraries**

1. **Android Architecture Components**
    - `ViewModel`, `LiveData`, `Room Database`
2. **Networking**
    - `Retrofit` for API communication.
    - `OkHttp` for efficient HTTP requests.
3. **Image Loading**
    - `Glide` for smooth image loading and caching.
4. **Concurrency**
    - `Java Executors` for managing background tasks.
5. **UI Components**
    - `RecyclerView` with `ListAdapter` for efficient list rendering.
    - `ConstraintLayout` for responsive design.
    - Material Design components.
6. **Data Storage**
    - `SharedPreferences` for small data storage.
    - `Room Database` for persistent local data.
---

## 🗂️ **Project Structure**
Built on top of the MVVM design pattern
- **`ui`**: Activities and adapters for user interface.
- **`viewmodel`**: ViewModel classes for managing UI data.
- **`model`**: Data model classes.
- **`repository`**: Repositories for data operations.
- **`api`**: API service interfaces and response models.
- **`database`**: Room database, DAO, and related classes.
 
---
