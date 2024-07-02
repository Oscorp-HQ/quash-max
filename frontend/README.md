# Official Web Dashboard for [Quash](https://quashbugs.com/)

Welcome to the Quash Web Dashboard, your ultimate in-app bug reporting tool! Built by developers for developers, this web dashboard captures everything you need to start fixing issues right away. It dispalys crash logs, session replays, network logs, device information, and much more, ensuring you have all the details at your fingertips.

<p align="center">
    <a href="https://quashbugs.com">
        <img src="https://storage.googleapis.com/misc_quash_static/quash-frontend.png"/>
    </a>
</p>

## Features

- Session Replays: Watch exactly what happened during the bug.
- Crash Logs & Network Logs: Detailed technical information for swift debugging.
- Device Information: Detailed device information including OS version, device model, and more.
- AI-Powered Solutions: Suggested fixes for each bug report.

## Tech Stack

- [Typescript](https://www.typescriptlang.org/) - Language
- [Next.js](https://nextjs.org/) - Framework
- [Tailwind](https://tailwindcss.com/) - CSS
- [shadcn/ui](https://ui.shadcn.com/) - Component Library
- [NextAuth.js](https://next-auth.js.org/) - Authentication
- [Google cloud platform](https://cloud.google.com/) - Hosting

## Getting Started

### Prerequisites

- Node.js (version 18 or later)

### Installation

1. **Clone the repository:**

   ```sh
   git clone https://github.com/Oscorp-HQ/QuashDashboard.git
   cd QuashDashboard

   ```

2. **Install dependencies:**

   using npm:

   ```sh
   npm install
   ```

   or using yarn:

   ```sh
   yarn install
   ```

3. **Set up environment variables:**

   Create a .env.local file in the root of your project and add necessary environment variables as shown in .env.example.

### Usage

1. **Run the development server:**

   using npm:

   ```sh
   npm run dev
   ```

   or using yarn:

   ```sh
   yarn dev
   ```

   Open http://localhost:3000 with your browser to see the result.
