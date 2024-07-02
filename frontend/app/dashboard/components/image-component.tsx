/**
 * React component that displays an image with specified source.
 *
 * @param {string} src - The source of the image to display.
 * @returns {JSX.Element} A div element containing the Image component.
 */
import Image from "next/image";
import React from "react";

const ImageComponent = React.memo(({ src }: { src: string }) => (
  <div className=" h-[140px] w-[240px] flex overflow-hidden  items-center justify-center">
    <Image
      src={src}
      alt="Picture Preview"
      fill={true}
      className="hover:cursor-pointer bg-[#7BB3F2]"
      loading="lazy"
    />
  </div>
));

export default ImageComponent;
