"use client";

import * as React from "react";
import { ThemeProvider as NextThemesProvider } from "next-themes";
import { ThemeProviderProps } from "next-themes/dist/types";
import { TooltipProvider } from "@/components/ui/tooltip";

/**
 * Provides the theme context for the application by wrapping the NextThemesProvider and TooltipProvider components.
 *
 * @param children The child components to be wrapped by the ThemeProvider.
 * @param props Additional props to be passed to the NextThemesProvider component.
 * @returns JSX element containing the NextThemesProvider and TooltipProvider components.
 */

export function ThemeProvider({ children, ...props }: ThemeProviderProps) {
  return (
    <NextThemesProvider {...props}>
      <TooltipProvider>{children}</TooltipProvider>
    </NextThemesProvider>
  );
}
