"use client";

import { useAutoAnimate } from "@formkit/auto-animate/react";
import {
  CustomToast,
  CustomToastClose,
  CustomToastDescription,
  CustomToastProvider,
  CustomToastTitle,
  CustomToastViewport,
} from "./custom-toast";
import { useCustomToast } from "./custom-use-toast";
import { ToastObject } from "@/app/types/dashboard-types";

/**
 * CustomToaster component that displays toast messages using custom components.
 * It utilizes useCustomToast hook to manage toast state and dismiss functionality.
 * The component renders CustomToastProvider, CustomToast, CustomToastTitle, CustomToastDescription,
 * CustomToastClose, and CustomToastViewport components based on the provided toastObject.
 * @param {Object} toastObject - Object containing toast message and type information.
 * @returns {JSX.Element} - CustomToaster component JSX.
 */

export function CustomToaster({
  toastObject,
}: {
  toastObject: ToastObject;
}): JSX.Element {
  const { toasts, dismiss } = useCustomToast();
  const { message, type } = toastObject;
  const [parent, enableAnimations] = useAutoAnimate(/* optional config */);

  return (
    <CustomToastProvider>
      {toasts.map(function ({ id, title, description, action, ...props }) {
        return (
          <CustomToast key={id} {...props} className="custom-toast-provider">
            <div className="custom-toast-container">
              <div className="custom-toast" ref={parent}>
                {title && <CustomToastTitle>{title}</CustomToastTitle>}
                {message && (
                  <CustomToastDescription typeof={type}>
                    {message}
                  </CustomToastDescription>
                )}
              </div>
              {action}
              <CustomToastClose
                className="custom-toast-close"
                onClick={() => {
                  dismiss(id);
                }}
              />
            </div>
          </CustomToast>
        );
      })}
      <CustomToastViewport />
    </CustomToastProvider>
  );
}
