import { ArrowRight, X } from "@/app/lib/icons";
import { Button } from "@/components/ui/button";
import Image from "next/image";
import Link from "next/link";
import React from "react";
import success from "../../../public/success.svg";

interface Banner {
  title: string;
  desc: string;
  link: string;
  imgsrc: string;
  route: string;
}

const bannerArray: Banner[] = [
  {
    title: "Set up Integrations",
    desc: "Export bug reports to workflow management tools as tickets",
    link: "Set up Integration",
    imgsrc: "https://storage.googleapis.com/misc_quash_static/google-sheet.svg",
    route: "/settings/integrations",
  },
  {
    title: "Invite your team",
    desc: "Bring your team to Quash for efficient collaboration",
    link: "Invite Members",
    imgsrc: "https://storage.googleapis.com/misc_quash_static/member.svg",
    route: "/settings/members",
  },
  {
    title: "Configure Columns",
    desc: "Get only the things relevant to you, in the format you recognise ",
    link: "Configure",
    imgsrc: "https://storage.googleapis.com/misc_quash_static/bug-report.svg",
    route: "/settings/integrations",
  },
];

/**
 * Functional component for rendering an integration banner with dynamic content.
 * Handles displaying different cards based on the index provided.
 * Utilizes Button component for rendering buttons with different actions.
 *
 * @param {Object} props - Props for the IntegrationBanner component.
 * @param {Function} props.setShowBanner - Function to control the visibility of the banner.
 * @param {boolean} props.showInviteMember - Flag to determine if invite member section should be shown.
 * @param {Array} props.integrationsDone - Array of integrations that are completed.
 * @returns {JSX.Element} IntegrationBanner component JSX element.
 */

const IntegrationBanner = ({
  setShowBanner,
  showInviteMember,
  integrationsDone,
}: {
  setShowBanner: React.Dispatch<React.SetStateAction<boolean>>;
  showInviteMember: boolean;
  integrationsDone: string[];
}): JSX.Element => {
  const handleBannerClose = () => {
    setShowBanner(false);
  };

  const renderButton = (index: number, route: string, link: string) => {
    if (index === 0) {
      return integrationsDone.length > 0 ? (
        <Button className="banner-success-button" variant="ghost">
          <Image
            src={success}
            alt="success icon"
            width={16}
            height={16}
            priority
          />
          Integration Done!
        </Button>
      ) : (
        <Link href={route}>
          <Button className="banner-link-button" variant="ghost">
            {link}
            <ArrowRight className="banner-arrow-icon" />
          </Button>
        </Link>
      );
    }

    if (index === 1) {
      return showInviteMember ? (
        <Link href={route}>
          <Button className="banner-link-button" variant="ghost">
            {link}
            <ArrowRight className="banner-arrow-icon" />
          </Button>
        </Link>
      ) : (
        <Button className="banner-success-button" variant="ghost">
          <Image
            src={success}
            alt="success icon"
            width={16}
            height={16}
            priority
          />
          Team Members Invited!
        </Button>
      );
    }

    return (
      <Link href={route}>
        <Button className="banner-link-button" variant="ghost">
          {link}
          <ArrowRight className="banner-arrow-icon" />
        </Button>
      </Link>
    );
  };

  return (
    <div className="banner-container">
      <div className="banner-header ">
        <span className="banner-header-title">Get started with Quash</span>
        <X
          size={24}
          className="banner-header-icon"
          onClick={handleBannerClose}
        />
      </div>
      <div className="banner-content">
        {bannerArray.map((el: Banner, index) => (
          <div className={`banner-card`} key={index}>
            <div className="banner-card-details">
              <div className="banner-card-info">
                <span className="banner-card-title">{el.title}</span>
                <span
                  className="banner-card-description
                "
                >
                  {el.desc}
                </span>
              </div>
              {renderButton(index, el.route, el.link)}
            </div>
            <Image
              src={el.imgsrc}
              alt={el.link}
              width={360}
              height={168}
              priority
            />
          </div>
        ))}
      </div>
    </div>
  );
};

export default IntegrationBanner;
