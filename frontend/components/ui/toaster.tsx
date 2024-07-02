"use client";

import {
  Toast,
  ToastClose,
  ToastDescription,
  ToastProvider,
  ToastTitle,
  ToastViewport,
} from "@/components/ui/toast";
import { useToast } from "@/components/ui/use-toast";

export function Toaster() {
  const { toasts } = useToast();

  return (
    <ToastProvider>
      {toasts.map(function ({ id, title, description, action, ...props }) {
        return (
          <Toast
            key={id}
            {...props}
            className="bg-[#212121] text-white font-[500] text-[14px] rounded-[12px] dark:border-[#424242]"
          >
            <div className="flex">
              <div className="grid gap-1 p-3 border-r-[1px] border-solid border-[#606060]">
                {title && <ToastTitle>{title}</ToastTitle>}
                {description && (
                  <ToastDescription typeof={props.typeof}>
                    {description}
                  </ToastDescription>
                )}
              </div>
              {action}
              <ToastClose className="p-3" />
            </div>
          </Toast>
        );
      })}
      <ToastViewport />
    </ToastProvider>
  );
}
