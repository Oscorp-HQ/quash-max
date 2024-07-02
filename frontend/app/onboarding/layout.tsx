import React from "react";
import Image from "next/image";
import Logo from "../../public/logo-yellow.svg";
import MobileScreen from "../dashboard/components/mobile-screen";

interface OnBoardingLayoutProps {
  children: React.ReactNode;
}

const OnBoardingLayout: React.FC<OnBoardingLayoutProps> = (props) => {
  return (
    <div className="onboarding-layout">
      <div className="header">
        <Image src={Logo} width={96} height={24} alt="Quash Logo" />
        <div className="action-items">
          <div className="header-icon"></div>
          <div className="header-dropdown"></div>
        </div>
      </div>
      <div className="content">{props.children}</div>
      <Image
        src="/onboard.svg"
        width={564 * 0.7}
        height={434 * 0.7}
        alt="Onboard Image"
        className="absolute right-0 bottom-10 hidden md:flex"
      />
      <MobileScreen />
    </div>
  );
};

export default OnBoardingLayout;
