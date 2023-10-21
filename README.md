# Public Poll Mobile App - Backend README

This is the README for the backend of the "Public Poll Mobile App." The backend serves as the server-side component responsible for managing user accounts, polls, and various interactions within the mobile application. Below, you'll find information about the technologies used, how the backend works, and how to set it up.

## Technologies Used

- **Backend Language:** Java
- **Backend Framework:** Apache Tomcat
- **Session Information:** Redis
- **Database:** MySQL
- **Cloud Service:** Digital Ocean
- **Image Storage:** AWS S3
- **API:** REST

## Setup

To set up and run the backend of the Public Poll Mobile App, follow these steps:

1. **Prerequisites:**

   - Ensure you have Java, Apache Tomcat, MySQL, Redis, and other dependencies installed on your server or development environment.

2. **Database Configuration:**

   - Create a MySQL database for the application.
   - Update the database connection details in the backend configuration.

3. **API Configuration:**

   - Configure the REST API endpoints and routes for the application.

4. **Redis Configuration:**

   - Set up Redis to manage session information. Configure Redis settings in the backend.

5. **AWS S3 Configuration:**

   - Set up AWS S3 for image storage and configure access credentials in the backend.

6. **Deployment:**

   - Deploy the backend on a server or cloud service like Digital Ocean.
   - Optionally set up a proxy layer in front of Tomcat, such as nginx, to provide SSL certficiate functionality. 

7. **Testing and Debugging:**

   - Test the API endpoints and resolve any issues.

## How It Works

The backend of the Public Poll Mobile App provides the necessary services and APIs to support the mobile application's functionality. Here's an overview of how it works:

- **User Management:** The backend manages user authentication, including sign-up and sign-in functionality. User data is stored in the MySQL database.

- **Poll Management:** Users can create, respond to, and save polls. The backend handles poll creation, storage, and retrieval from the database. It also manages the results of polls.

- **Session Handling:** Redis is used for session management, allowing users to stay authenticated between requests.

- **Image Storage:** AWS S3 is utilized for storing user profile pictures and other images associated with polls.

- **API Endpoints:** The backend exposes RESTful API endpoints for the mobile app to interact with, including user account management, poll creation, and polling.

- **Settings:** Users can customize their profile information and app theme through the settings.

For more details about the frontend and how it interacts with the backend, please refer to the [Frontend README](frontend/README.md) in the linked GitHub repository.
