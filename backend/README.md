# Official Backend for [Quash](https://quashbugs.com/)

Welcome to the Quash Backend repository, part of the [Quash](https://quashbugs.com/) project. This repository contains the code for the Quash Web Backend, a crucial component of your ultimate in-app bug reporting tool. Built by developers for developers, this backend supports the web dashboard in capturing everything you need to start fixing issues right away, including crash logs, session replays, network logs, device information, and much more.

<div align="center">
    <img src="https://github.com/dhairya-quash/TEST-REPO/assets/161799860/7f7b7ffd-66f4-45d7-b68e-01fcedff0a75" alt="Logo" width=1000>
</div>
<br>

| **Reporting** 🗒️ | **Resolution** ✅ | **Collaboration** 🤝🏻 |
| :--------: | :---------: | :---------: |
| Raise comprehensive tickets with minimal effort | Know exactly where the bug is - and how to fix it | Manage all your testing workflows in a single place |

---

## Table of Contents
- [Project Architecture](#project-architecture)
- [Installation](#installation)
- [Configuration](#configuration)
- [Optional Integrations](#optional-integrations)
- [Run Locally](#run-locally)

## Project Architecture

The project is structured into the following layers:

- **Controller**
- **Service**
- **Repository**
- **Model**
- **DTO**
- **Utility**

<div align="center"><img src="https://github.com/dhairya-quash/TEST-REPO/assets/161799860/2f2e2354-0158-4cd8-879b-4d32aeb9ac13" alt="Architecture"></div>

## DB Schema
<div align="center"> <img src="https://github.com/Oscorp-HQ/quash-backend/assets/161799860/9ffd2570-a050-4e39-87a9-4d04800efea5" alt="Flow" width=1000> </div>
<br>

## Report Generation Flow
<div align="center"> <img src="https://github.com/Oscorp-HQ/quash-backend/assets/161799860/65d6b494-4867-43ed-970a-676feb6a7272" alt="Flow" width=1000> </div>
<br>

This guide will provide you steps to setup and configure the Backend for Quash.

Quash Backend is built using the following technologies:
- [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started.html#getting-started.installing)
- [MongoDB](https://www.mongodb.com/docs/manual/installation/)
- Maven
- JWT Authentication

# Installation
```bash
# Move to your workspace
cd your-workspace

# Clone the parent repository
git clone https://github.com/Oscorp-HQ/quash-max.git

# Move to the backend directory
cd quash-max/backend

# Run this maven command
mvn clean install
```

## Configuration


## Application.properties
> Navigate to the resources directory and open the `application.properties` file. Here you will add your database connection strings, access tokens, secret keys for different integrations and services.<br>
>


**MongoDB Connection String for Database** 
```java
spring.data.mongodb.uri=mongodb_connection_string
```

JWT configurations -
```java
jwt.secret='your_secret'
jwt.expirationMs='expiration_time'
token.signing.key='jwt_singing_key'
```

**Set a Jasypt Password Encryption Key**
```java
jasypt.encryption.password='encryption_password'
```

Add your Frontend URL
```java
spring.frontend.url='your_frontend_url'
```

Add your Spring Base URL
```java
spring.url='your_spring_url'
```
Set Access and Refresh Token expiry time
```java
# Access Token expiry - 6 days
token.accessToken.expiration=518400000
# Refresh Token expiry - 8 days
token.refreshToken.expiration=691200000
```
Some extra properties
```java
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=15MB
spring.servlet.multipart.max-request-size=15MB
management.endpoints.web.exposure.include=*
spring.main.lazy-initialization=true
```

**Mail Service**
Setup a mail service and get required credentials
```java
spring.mail.host='email_host'
spring.mail.username='email_username'
spring.mail.password='email_password'
spring.mail.properties.mail.transport.protocol=smtp
spring.mail.properties.mail.smtp.port='mail_port'
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
from.email.address='your_email@example.com'
```

### Optionally you can use Google OAuth for signin and signup.
**Google OAuth**
```java
spring.security.oauth2.client.registration.google.client-id='google_client_id'
spring.security.oauth2.client.registration.google.client-secret='google_client_secret'
```

## Optional Integrations
Below are some integrations where you can export your tickets to. Configure the integration of your choice by adding the required credentials mentioned.
- [Jira](#jira-integration)
- [Google Sheets](#google-sheets-integration)
- [Slack](#slack-integration)
- [Linear](#linear-integration)
- [Github](#github-integration)

<h3 id="jira-integration"> Jira integration </h3>
<div>Add your Jira account credentials</div>

```java
spring.atlassian.jira.client_id='jira_client_id'
spring.atlassian.jira.client_secret='jira_client_secret'
spring.atlassian.jira.auth_endpoint=https://auth.atlassian.com/oauth/token
spring.atlassian.jira.accessible_resource_endpoint=https://api.atlassian.com/oauth/token/accessible-resources
```


<h3 id="slack-integration"> Slack integration </h3>

Add your [Slack](https://api.slack.com/apps) account credentials
```java
spring.slack.clientId='slack_client_id'
spring.slack.clientSecret='slack_client_secret'
spring.slack.quash.redirectUri='slack_redirect_uri'
```


<h3 id="linear-integration"> Linear integration </h3>

Add your Linear account credentials
```java
spring.linear.redirect_uri='linear_redirect_uri'
spring.linear.auth_endpoint='auth_endpoint'
spring.linear.client_id='linear_client_id'
spring.linear.client_secret='linear_client_secret'
```

<h3 id="github-integration"> Github </h3>

Add your Github account credentials
```java
spring.github.client_id='github_client_id'
spring.github.client_secret='github_client_secret'
```

## Run Locally
Run the `QuashApplication` File
```java
mvn spring-boot:run
```

## Repository Structure

This backend repository is part of the larger Quash project, located in the parent repository at https://github.com/Oscorp-HQ/quash-max. The parent repository contains multiple components of the Quash project, including this backend.

For information on other components and how they interact, please refer to the main README in the parent repository.

## Contributing

For contribution guidelines, please refer to the CONTRIBUTING.md file in the parent repository.

## License

This project is licensed under the terms specified in the LICENSE file in the parent repository.
