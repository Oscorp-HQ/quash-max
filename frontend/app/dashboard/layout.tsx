"use client";
import React from "react";
import Header from "../dashboard/components/header";

interface DashboardLayoutProps {
  children: React.ReactNode;
}

/**
 * Functional component for the dashboard layout.
 * Renders the Header component and the children components within a dashboard layout container.
 */

const DashboardLayout: React.FC<DashboardLayoutProps> = (props) => {
  return (
    <div className="dashboard-layout">
      <Header />
      <div className="dashboard-body">{props.children}</div>
    </div>
  );
};

export default DashboardLayout;
