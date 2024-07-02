import React from "react";
import ReactAudioPlayer from "react-audio-player";

/**
 * Functional component that renders an audio player with specified source and controls.
 */

const AudioComponent = React.memo(({ src }: { src: string }) => (
  <div className="flex h-[54px] flex-col items-center justify-center  w-[240px]">
    <ReactAudioPlayer
      src={src}
      controls
      controlsList="nodownload noremoteplayback noplaybackrate foobar "
      autoPlay={false}
      className="audio-player"
    />
  </div>
));

export default AudioComponent;
