import React from "react";
import Image from "next/image";
import Logo from "../../public/logo-yellow.svg";

interface AddQuashLayoutProps {
  children: React.ReactNode;
}

/**
 * This code snippet represents a TypeScript React functional component called AddQuashLayout.
 * It is responsible for rendering a layout with a header and content section.
 * The component receives a prop called children, which represents the content to be rendered inside the layout.
 * The header section includes an Image component from the Next.js library, displaying a logo.
 * It also includes two div elements for action items, represented by CSS classes.
 * The content section renders the children prop passed to the component.
 * This component is exported as the default export of the module.
 */

const AddQuashLayout: React.FC<AddQuashLayoutProps> = ({ children }) => {
  return (
    <div className="addquash-layout">
      <div className="header">
        <Image src={Logo} width={96} height={24} alt="Quash Logo" />
        <div className="action-items">
          <button className="header-icon" aria-label="Header Icon"></button>
          <button
            className="header-dropdown"
            aria-label="Header Dropdown"
          ></button>
        </div>
      </div>
      <div className="content">{children}</div>
    </div>
  );
};

export default React.memo(AddQuashLayout);
