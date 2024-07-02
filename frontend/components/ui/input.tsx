import * as React from "react";

import { cn } from "@/lib/utils";
import { useState } from "react";
import { Eye, EyeSlash } from "@/app/lib/icons";
export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  Icon?: React.ReactNode;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, Icon, type, placeholder, ...props }, ref) => {
    const [showPassword, setShowPassword] = useState(false);
    const togglePasswordVisibility = () => {
      setShowPassword(!showPassword);
    };
    return (
      <div className="relative ">
        {Icon ? <div className="absolute left-3 top-[12px]">{Icon}</div> : null}

        <input
          type={showPassword ? "text" : type}
          placeholder={placeholder ? placeholder : ""}
          className={cn(
            `flex h-10 rounded-lg border border-input dark:border-[#424242] bg-grey-50 dark:bg-[rgb(48,56,70)] ${
              Icon ? "px-8" : "px-3"
            } py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50`,
            className,
          )}
          ref={ref}
          {...props}
        />
        {type === "password" && (
          <div className="absolute right-3 top-[12px]">
            {!showPassword ? (
              <Eye
                size={16}
                onClick={togglePasswordVisibility}
                className="hover:cursor-pointer text-custom"
              />
            ) : (
              <EyeSlash
                size={16}
                onClick={togglePasswordVisibility}
                className="hover:cursor-pointer text-custom"
              />
            )}
          </div>
        )}
      </div>
    );
  },
);
Input.displayName = "Input";

export { Input };
