import { Button } from "@/components/ui/button";
import Link from "next/link";
import React from "react";

/**
 * Functional component that represents the state when no SDK is present.
 * Renders a section with a message to add Quash SDK to the application and a button to add the SDK.
 */

const NoSDKState = () => {
  return (
    <section className="add-sdk-container">
      <p className="add-sdk-text ">
        Add Quash SDK to your application to get bug reports here
      </p>
      <Button
        className="add-sdk-link"
        aria-label="Add Quash to your Application"
      >
        <Link href="/add-quash">Add Quash to your Application</Link>
      </Button>
    </section>
  );
};

export default NoSDKState;
