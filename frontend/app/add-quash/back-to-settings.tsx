"use client";
import Link from "next/link";
import React from "react";
import { useSearchParams } from "next/navigation";
import { ArrowLeft } from "../lib/icons";

/**
 * Renders a link to navigate back to the settings page.
 * If the "source" query parameter is present in the URL, it renders a link with the text "Back to Settings" and an arrow icon.
 * Otherwise, it doesn't render anything.
 */
const BackToSettings = (): JSX.Element | null => {
  const searchParams = useSearchParams();
  const source = searchParams.get("source");

  if (!source) {
    return null;
  }

  return (
    <Link className="back-to-settings" href="/settings/application">
      <ArrowLeft size={24} className="icon" aria-hidden="true" />
      Back to Settings
    </Link>
  );
};

export default BackToSettings;
