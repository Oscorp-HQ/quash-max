"use client";
import Image from "next/image";
import React from "react";

/**
 * Renders an error screen with an image, title, description, and contact link.
 * @returns JSX code representing the error screen.
 */

const Error = () => {
  return (
    <div className="error-screen-container">
      <div className="error-container">
        <Image
          src="/something-error.svg"
          alt="something-error"
          height="242"
          width="212"
        />
        <div className="error-screen-title">Something went wrong..</div>
        <div className="error-screen-description">
          Refresh your page or try again after sometime
        </div>
        <p className="error-screen-contact">
          If this doesn’t resolve in a short while, feel free to{" "}
          <a href="mailto:hello@quashbugs.com" className="link">
            Contact us
          </a>
        </p>
      </div>
    </div>
  );
};

export default Error;
