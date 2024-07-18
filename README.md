
<Meta title="Quash README" />

# Quash: Your Ultimate In-App Bug Reporting Solution
<div align="center"> <img src="https://github.com/user-attachments/assets/e3cdb94c-4596-4955-9f3b-14cc004b1733" alt="Quash Banner" width=1000> </div>
<br>
Welcome to Quash! We're excited to introduce our next-generation mobile development tool designed to revolutionize the way you test and debug your applications. With Quash, identifying and resolving bugs is as easy as a shake of your device. Our intuitive platform auto-captures session replays, crash logs, and more, allowing you to focus on building new features while we handle the bugs.

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Architecture](#architecture)
4. [Installation](#installation)
5. [Usage](#usage)
6. [Contributing](#contributing)
7. [License](#license)
8. [Contact](#contact)

## Introduction

Quash is an SDK-enabled bug reporting solution that helps users report bugs efficiently with just a simple shake. Our tool tracks and resolves bugs through the Quash web dashboard, ensuring your mobile apps are released on time with the highest quality standards. With Quash, teams can optimize the time and resources spent on the testing process, making mobile testing smooth and straightforward.

## Features

### Easy Setup
- **Quick Integration:** Integrate the Quash SDK with your application in less than 5 minutes.
- **Detailed Guides:** Follow our comprehensive guides for seamless setup.

### In-App Bug Reporting
- **Shake to Report:** Simply shake your device to launch the reporting window, prefilled with essential information.
- **Intuitive Interface:** Log bugs in seconds with our user-friendly interface.

### Automated Crash Detection
- **Real-Time Capture:** Automatically detect and capture crash events along with all necessary information.
- **Reproduce-Free Debugging:** No need to reproduce crashes manually.

### Information Auto-Capture
- **Comprehensive Data Collection:** Auto-capture screenshots, session recordings, crash logs, steps to reproduce, device information, and API/network calls.
- **Detailed Bug Reports:** Each bug report includes all relevant data at the moment of the crash or when shaking the device.

### External Media Attachments
- **Enhanced Reports:** Add audio recordings, pictures, videos, and other media files to bug reports.

### Bug Tracking and Collaboration
- **Web Dashboard:** Manage all reported bugs and crashes through the Quash web dashboard.
- **Collaboration Tools:** View, track, prioritize, and comment on bugs for efficient resolution.

### Seamless Integrations
- **Popular Tools:** Integrate with Jira, Slack, Google Sheets, Linear, GitHub, and more.
- **Streamlined Workflow:** Export tickets directly to these platforms for a seamless bug tracking and resolution process.

## Architecture

Quash comprises three main components: the SDK, the backend, and the frontend.

### SDK
- **Android SDK:** The Quash SDK for Android provides in-app bug reporting and crash detection.
- **Integration:** Simple integration with your application, including network interception and configuration.

### Backend
- **Spring Boot Application:** Handles API requests, processes bug reports, and manages integrations with third-party tools.
- **Scalable Infrastructure:** Built to handle high volumes of data and concurrent users.

### Frontend
- **Next.js Dashboard:** A user-friendly web interface for viewing and managing bug reports.
- **Real-Time Data:** Displays real-time data captured by the SDK, with options for collaboration and issue tracking.

## Installation

### SDK Installation
For detailed SDK installation instructions, please refer to the individual [README files in the SDK directory](./android/README.md).

### Backend Installation
For backend setup and deployment instructions, please refer to the [backend README file](./backend/README.md).

### Frontend Installation
For frontend setup and deployment instructions, please refer to the [frontend README file](./frontend/README.md).

For a detailed, step-by-step guide on setting up Quash's Frontend and Backend using Docker, whether on your local machine or a hosted VM instance, please refer to our [Quick Start Guide](https://github.com/Oscorp-HQ/quash-max/blob/main/Quickstart.md).

## Usage

### Reporting Bugs
1. **Shake Device:** Trigger the bug report by shaking your device.
2. **Fill Report:** Use the prefilled bug report window to add any additional details.
3. **Submit:** Submit the report, which is then captured and sent to the Quash web dashboard.

### Managing Bugs
1. **Dashboard Access:** Log in to the Quash web dashboard.
2. **View Reports:** Access detailed bug reports with all captured data.

## Quick Start Guide

For a detailed, step-by-step guide on setting up Quash using Docker, whether on your local machine or a hosted VM instance, please refer to our [Quick Start Guide](./Test.md).

## Contributing

We love contributions! Please read our [contribution guidelines](./CONTRIBUTING.md) to get started. We welcome all kinds of contributions, from code enhancements to documentation improvements.

## License

Quash is licensed under the [MIT License](./LICENSE).


## Contact

For any questions or support, please reach out to us at [support@quashbugs.com](mailto:support@quashbugs.com).

---

Thank you for choosing Quash! We look forward to helping you streamline your mobile app testing and bug reporting processes. Let's build something amazing together!
