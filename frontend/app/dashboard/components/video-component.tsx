import React from "react";

/**
 * Functional component that renders a video player with the specified source.
 *
 * @param {string} src - The URL of the video source
 * @returns {JSX.Element} VideoComponent - A memoized video player component
 */

const VideoComponent = React.memo(({ src }: { src: string }) => (
  <div className=" h-[140px] w-[240px] flex overflow-hidden  items-center justify-center">
    <video
      src={src}
      controls
      width="100%"
      height="100%"
      className="hover:cursor-pointer h-full w-full "
    />
  </div>
));

export default VideoComponent;
