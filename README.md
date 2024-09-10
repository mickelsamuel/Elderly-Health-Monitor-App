# Elderly-Health-Monitor-App

Preventing is better than healing, which is why Elderly-Health-Monitor-App was created! Elderly-Health-Monitor-App is an android application connected to a temperature, heart rate, and accelerometer sensor through ESP32 module's bluetooth in order to monitor vitals and prevent accidents in elderly homes.

## Project Overview

Elderly-Health-Monitor-App is developed using an Agile methodology through three sprints and one final demo in approximately eigth weeks. The primary goal is to create an android application with a minimum of one sensor that is connected through a wireless module in order to give a solution to a societal problem.

## Technology Stack

### Backend Frameworks

- **Java <td><code><img width="20" src="https://user-images.githubusercontent.com/25181517/117201156-9a724800-adec-11eb-9a9d-3cd0f67da4bc.png" alt="Java" title="Java"/></code></td> (Android Studio <td><code><img width="20" src="https://user-images.githubusercontent.com/25181517/192108895-20dc3343-43e3-4a54-a90e-13a4abbc57b9.png" alt="Android Studio" title="Android Studio"/></code></td>)** : Java was used to build the features and functionality in the Android Studio IDE through activities.
  
### Frontend Frameworks

- **XML (HTML <code><img width="20" src="https://user-images.githubusercontent.com/25181517/192158954-f88b5814-d510-4564-b285-dff7d6400dad.png" alt="HTML" title="HTML"/></code> , Android Studio <td><code><img width="20" src="https://user-images.githubusercontent.com/25181517/192108895-20dc3343-43e3-4a54-a90e-13a4abbc57b9.png" alt="Android Studio" title="Android Studio"/></code></td>)**: eXtensible Markup Language, which comes from HTML, was used to create an elderly-friendly user interface that was perfected through stakeholder's feedback.
  
### Database

- **Firebase <td><code><img width="20" src="https://user-images.githubusercontent.com/25181517/189716855-2c69ca7a-5149-4647-936d-780610911353.png" alt="Firebase" title="Firebase"/></code></td>** : Firebase is a cloud real-time database, which is crucial to use when multiple users from all around the world need to access the same data with privacy and protection.

### Hardware Setup

#### Programming Language and IDE

- **C++ <code><img width="20" src="https://user-images.githubusercontent.com/25181517/192106073-90fffafe-3562-4ff9-a37e-c77a2da0ff58.png" alt="C++" title="C++"/></code> (Visual Studio Code <code><img width="20" src="https://user-images.githubusercontent.com/25181517/192108891-d86b6220-e232-423a-bf5f-90903e6887c3.png" alt="Visual Studio Code" title="Visual Studio Code"/></code>)**: Platform IO uses C++ in Visual Studio Code and contains all libraries used in Arduino IDE to setup the sensors and module needed.
  
#### Hardware Details

- **Sparkfun ESP32**: Module
- **MPU6050**: Accelerometer Sensor
- **DS18B20**: Temperature Sensor
- **MAX30102**: Heart Rate Sensor

## Project Approach

### Development Methodology 

The project follows an adapted Agile methodology, with 8 weeks divided into three sprints and a final demo, allowing for iterative progress.

### Project Timeline

The product backlog and user stories of each sprint can be found in the following excel sheet in order to understand how the project was divided.
https://docs.google.com/spreadsheets/d/1Hu21J49wH5RtLttLXAQw-GspQzKR3T2AtGSYJYI4I78/edit?gid=1262099301#gid=1262099301

### Collaboration and Communication

- **GitHub <code><img width="20" src="https://user-images.githubusercontent.com/25181517/192108374-8da61ba1-99ec-41d7-80b8-fb2f7c0a4948.png" alt="GitHub" title="GitHub"/></code>:** Centralized repository for source code, documentation, and collaboration.
- **Discord 💬:** Communication platform for weekly meetings, discussions, and issue tracking.

## Team Roles

- **Celine (@CelinZiad) 👩‍💻:** Full-Stack Developer, Hardware Programmer, Database Developer, and Scrum Master. Focuses on general sprint planning, sprint review and sprint retrospective of the project. Works on data saving and privacy, from the sensors to the front-end display.
- **Mickel (@mickelsamuel) 🧑‍💻:** Full-Stack Developer and Database Developer. Focuses on the design and structure, ensuring clarity and aesthetics throughout the application. Works on the user and nurse communication and on the alerts sent in emergencies.
- **Adam (@Adoomz) 🧑‍💻:** Full-Stack Developer, Hardware Programmer, and Database Developer. Focuses on the hardware of the monitor app and on the bluetooth connection. Works on the sensor setup and bluetooth connection, with some details on the data display.
- **Susmith (@Compileware) 🧑‍💻:** Back-end Developer and Hardware Programmer. Focuses on the hardware of the monitor app and on the bluetooth connection. Works on the sensor setup and bluetooth connection, with the data real-time display.
- **Sal (@sbruzz) 🧑‍💻:** Back-end Developer. Focuses on documentation and telephone notifications pop-ups. Works on the alert notifications of abnormal sensor readings.

## Usage Guide for Software of Monitor App

1. Install Android Studio and Visual Studio Code.

2. Install Java.

3. Clone the repository.

4.  Run the MonitorActivity.java file.
