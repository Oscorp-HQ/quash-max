import "./globals.css";
// import type { Metadata } from "next";
import { Inter } from "next/font/google";
import AuthProvider from "@/context/auth-provider";
import { Toaster } from "@/components/ui/toaster";
import { Analytics } from "@vercel/analytics/react";
import { ThemeProvider } from "./dashboard/components/providers";
import { Providers } from "@/lib/providers";

const inter = Inter({
  subsets: ["latin"],
  weight: ["400", "500", "700"],
  variable: "--font-inter",
});

/**
 * RootLayout component.
 *
 * This component is responsible for rendering the root layout of the application.
 * It wraps the children components with various providers and sets up the necessary
 * HTML structure for the layout.
 *
 * @component
 * @param {React.ReactNode} children - The children components to be rendered within the layout.
 * @returns {React.ReactNode} The rendered root layout.
 */

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Providers>
      <html lang="en" suppressHydrationWarning>
        <body className={`${inter.variable} font-sans `}>
          <AuthProvider>
            <ThemeProvider
              attribute="class"
              defaultTheme="system"
              enableSystem
              disableTransitionOnChange
            >
              <div className="h-screen layout-container overflow-scroll">
                {children}
              </div>
            </ThemeProvider>

            <Analytics />
            <Toaster />
          </AuthProvider>
        </body>
      </html>
    </Providers>
  );
}
