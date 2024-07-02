import React from "react";
import { Spinner } from "../../app/lib/icons";

const SpinLoader = ({ size = 20, className = "" }) => {
  return (
    <Spinner
      size={size}
      className={`flex animate-spin spinner text-[#ffff] dark:text-[#000] ${className}`}
    />
  );
};

export default SpinLoader;
