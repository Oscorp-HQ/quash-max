"use client";
import Link from "next/link";
import React from "react";
import { ArrowRight } from "../lib/icons";
import { useSearchParams } from "next/navigation";

/**
 * Renders a link to the dashboard if the 'source' query parameter is not present.
 *
 * @returns {JSX.Element} The rendered link to the dashboard or an empty fragment.
 */

const BackToDashBoard = (): JSX.Element | null => {
  const searchParams = useSearchParams();
  const source = searchParams.get("source");

  if (source) {
    return null;
  }

  return (
    <Link className="go-to-dashboard" href="/dashboard">
      Go to Dashboard {"  "}
      <ArrowRight size={16} className="ml-2" />
    </Link>
  );
};

export default BackToDashBoard;
