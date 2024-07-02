import React from "react";

/**
 * Functional component that renders an empty state message for bug reports.
 * Displays a title and description prompting the user to start testing on their device.
 */

const EmptyState = () => {
  return (
    <div className="empty-state-container">
      <span className="empty-state-title ">
        Your bug reports will show up here..
      </span>
      <span className="empty-state-description">
        Start testing on your device to record reports
      </span>
    </div>
  );
};

export default EmptyState;
