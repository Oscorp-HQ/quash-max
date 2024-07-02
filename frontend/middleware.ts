export { default } from "next-auth/middleware";

export const config = {
  matcher: [
    "/onboarding/:path*",
    "/add-quash/:path*",
    "/settings/:path*",
    "/verify",
    "/dashboard/:path*",
  ],
};
