"use client";
const HorizontalDevider = () => {
  return (
    <div className="flex items-center gap-x-2">
      <div className="horizontal-divider w-full h-px"></div>
      <div className="horizontal-divider-text text-xs">OR</div>
      <div className="horizontal-divider w-full h-px"></div>
    </div>
  );
};

export { HorizontalDevider };
