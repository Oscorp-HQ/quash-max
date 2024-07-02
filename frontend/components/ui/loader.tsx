"use client";
import { Player } from "@lottiefiles/react-lottie-player";
import loader from "@/lotties/loaderquash.json";
import loaderWhite from "@/lotties/loader-lottie-white.json";
import { useTheme } from "next-themes";

export default function Loader() {
  // Detect system theme
  const { theme, systemTheme } = useTheme();
  const currentTheme = theme === "system" ? systemTheme : theme;

  // Choose the appropriate animation file based on the system theme
  const lottieSrc = currentTheme === "dark" ? loaderWhite : loader;

  return (
    <div style={{ backgroundColor: "transparent" }}>
      <Player
        src={lottieSrc}
        className="player"
        loop
        autoplay
        style={{ width: "150px" }}
      />
    </div>
  );
}
