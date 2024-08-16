# Official Web Dashboard for [Quash](https://quashbugs.com/)

Welcome to the Quash Frontend repository, part of the [Quash](https://quashbugs.com/) project. This repository contains the code for the Quash Web Dashboard, your ultimate in-app bug reporting tool. Built by developers for developers, this web dashboard captures everything you need to start fixing issues right away. It displays crash logs, session replays, network logs, device information, and much more, ensuring you have all the details at your fingertips.

<p align="center">
    <a href="https://quashbugs.com">
        <img src="https://storage.googleapis.com/misc_quash_static/quash-frontend.png"/>
    </a>
</p>

## Features

- Session Replays: Watch exactly what happened during the bug.
- Crash Logs & Network Logs: Detailed technical information for swift debugging.
- Device Information: Detailed device information including OS version, device model, and more.

## Tech Stack

[![Typescript](https://img.shields.io/badge/Typescript-white?logo=TypeScript)](https://www.typescriptlang.org/) [![Next.js](https://img.shields.io/badge/Next.js-000000?logo=Next.js)](https://nextjs.org/) [![Tailwind](https://img.shields.io/badge/Tailwind-white?logo=tailwindcss)](https://tailwindcss.com/) [![shadcn/ui](https://img.shields.io/badge/shadcn_ui-000000?logo=shadcnui)](https://ui.shadcn.com/) [![Google Cloud Platform](https://img.shields.io/badge/Google_Cloud_Platform-white?logo=googlecloud)](https://cloud.google.com/)

- [Typescript](https://www.typescriptlang.org/) - Language
- [Next.js](https://nextjs.org/) - Framework
- [Tailwind](https://tailwindcss.com/) - CSS
- [shadcn/ui](https://ui.shadcn.com/) - Component Library
- [NextAuth.js](https://next-auth.js.org/) - Authentication
- [Google Cloud Platform](https://cloud.google.com/) - Hosting

## Getting Started

### Note

> Quash Dashboard has a dependency on backend for providing the neccesary actions and data to go beyond the signin and signup page. So in order to use the dashboard or view any changes that you make in the dashboard, you need a working backend for it. To do so, you can either run the code manually or use a pre-built docker image. Please refer to the [Backend Setup](https://github.com/dhairya-quash/TEST-REPO/tree/main/backend) for detailed steps.

### Prerequisites

- Node.js (version 18 or later)

### Installation

1. **Clone the parent repository and move to the frontend directory:**

   ```sh
   git clone https://github.com/Oscorp-HQ/quash-max.git
   cd quash-max/frontend
   ```

2. **Install dependencies:**
   Using npm:

   ```sh
   npm install
   ```

   Or using yarn:

   ```sh
   yarn install
   ```

3. **Set up environment variables:**
   Create a `.env.local` file in the `frontend` directory and add necessary environment variables as shown in `.env.example`.

### Usage

1. **Run the development server:**
   Using npm:

   ```sh
   npm run dev
   ```

   Or using yarn:

   ```sh
   yarn dev
   ```

### Running With Docker:

1. Pull Max's frontend docker image from docker hub.

   ```bash
   docker pull quashbugs/quash-max-frontend:latest
   ```

2. Create a container without running.

   ```bash
   docker create --name max-frontend -p 3000:3000 quashbugs/quash-max-frontend:latest
   ```

3. Extract the `application.properties` file to configure environment variables.

   ```bash
   docker cp max-frontend:/app/frontend/.env.example ./.env.local
   ```

4. Edit the `.env.local` file on your local machine by adding your tokens and secrets and after that, mount it back to the container.

   ```bash
   docker cp ./.env.local max-frontend:/app/frontend/.env.local
   ```

5. Run the container.

   ```bash
   docker start max-frontend
   ```

Open http://localhost:3000 with your browser to see the result.

## Repository Structure

This frontend repository is part of the larger Quash project, located in the parent repository at https://github.com/Oscorp-HQ/quash-max. The parent repository contains multiple components of the Quash project, including this frontend.

For information on other components and how they interact, please refer to the main README in the parent repository.

## Contributing

For contribution guidelines, please refer to the CONTRIBUTING.md file in the parent repository.

## License

This project is licensed under the terms specified in the LICENSE file in the parent repository.
