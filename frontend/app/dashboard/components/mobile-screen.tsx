import Image from "next/image";
import React from "react";

/**
 * Functional component for rendering a mobile screen layout with Quash branding.
 * Includes an image of the Quash logo, a description, and information text.
 */

const MobileScreen = () => {
  return (
    <div className="mobile-screen-conatiner">
      <div className="mobile-logo-container">
        <Image
          src="/dashboard-mobile.png"
          width={212}
          height={242}
          alt="Quash Logo"
        />
      </div>

      <div className="mobile-screen-description-container text-center">
        <p className="mobile-screen-description">
          For a better experience please <br /> launch Quash on your desktop
        </p>
      </div>

      <div className="mobile-screen-info-container">
        <p className="mobile-screen-info">
          We’re working to bring Quash to your mobile screens ;)
        </p>
      </div>
    </div>
  );
};

export default MobileScreen;
