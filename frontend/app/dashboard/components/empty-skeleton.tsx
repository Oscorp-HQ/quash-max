import { Skeleton } from "@/components/ui/skeleton";
import React from "react";

const outerArray = Array.from({ length: 18 }, (_, i) => i);
const innerArray = Array.from({ length: 5 }, (_, i) => i);

/**
 * Component that renders a skeleton loading UI with multiple rows and cells.
 */

const EmptySkeleton = () => {
  const renderCells = () =>
    innerArray.map((_, index) => (
      <div className="empty-skeleton-cell long" key={`inner-${index}`}>
        <Skeleton className="empty-skeleton" />
      </div>
    ));

  return (
    <>
      {outerArray.map((_, outerIndex) => (
        <div key={outerIndex} className="empty-skeleton-row">
          <div className="empty-skeleton-cell short">
            <Skeleton className="empty-skeleton" />
          </div>
          {renderCells()}
          <div className="empty-skeleton-cell short">
            <Skeleton className="empty-skeleton" />
          </div>
        </div>
      ))}
    </>
  );
};

export default EmptySkeleton;
