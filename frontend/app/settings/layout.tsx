"use client";
import React, { useMemo } from "react";
import Link from "next/link";
import {
  Folders,
  Money,
  SlidersHorizontal,
  Users,
  Plug,
  CaretLeft,
} from "../lib/icons";
import { usePathname } from "next/navigation";
import Header from "../dashboard/components/header";
import MobileScreen from "../dashboard/components/mobile-screen";

interface SettingsLayoutProps {
  children: React.ReactNode;
}

const links = [
  {
    label: "General",
    name: "general",
    active: true,
    icon: <SlidersHorizontal />,
  },
  {
    label: "Application",
    name: "application",
    active: false,
    icon: <Folders />,
  },
  {
    label: "Integrations",
    name: "integrations",
    active: false,
    icon: <Plug />,
  },
  { label: "Members", name: "members", active: false, icon: <Users /> },
];

/**
 * React functional component for rendering a settings layout with a sidebar and navigation links.
 * This component includes a header, sidebar with navigation links, and a content section.
 * @param children - React node representing the content to be displayed within the layout.
 * @returns JSX.Element - The rendered settings layout component.
 */

const SettingsLayout: React.FC<SettingsLayoutProps> = ({ children }) => {
  const path = usePathname();

  const status = useMemo(() => path.split("/")[2], [path]);

  return (
    <div className="settings-layout">
      <Header />
      <div className="body">
        <div className="settings-sidebar">
          <Link className="back-to-dashboard" href={"/dashboard"}>
            <CaretLeft />
            Back to Dashboard
          </Link>
          <div className="nav-links-container">
            {links.map((link, index) => (
              <div key={index}>
                <Link
                  href={{
                    pathname: link.name,
                    query: {
                      source: status,
                    },
                  }}
                  className={`nav-links ${
                    status === link.name
                      ? "nav-link-selected"
                      : "nav-link-not-selected"
                  }`}
                >
                  {link.icon}
                  {link.label}
                </Link>
                {index === links.length - 1 ? null : <hr className="divider" />}
              </div>
            ))}
          </div>
        </div>
        <div className="content">{children}</div>
      </div>
      <MobileScreen />
    </div>
  );
};

export default SettingsLayout;
