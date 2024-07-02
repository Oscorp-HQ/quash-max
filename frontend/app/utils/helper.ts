import { baseURL } from "@/app/apis/api-config";
import { signOut } from "next-auth/react";
import { Session } from "next-auth/core/types";

/**
 * Capitalizes the first character of a given string and converts the rest of the string to lowercase.
 *
 * @param str - The input string to capitalize the first character.
 * @returns A new string with the first character capitalized and the rest of the string in lowercase.
 */
export function capitalizeFirstCharacter(str: string) {
  const firstChar = str.charAt(0).toUpperCase();
  const restOfString = str.slice(1).toLowerCase();
  return firstChar + restOfString;
}

/**
 * Fetches data from a specified URL using the provided authentication token.
 *
 * @param url - The URL to fetch data from.
 * @returns A Promise that resolves to the fetched data if the request is successful, otherwise undefined.
 */

export async function fetchData(url: string, session: Session) {
  if (session === null) {
    throw new Error("No session found");
  }

  const sessionData: Session = session;

  const authToken = sessionData?.data?.token;
  if (baseURL) {
    const data = await fetch(`${baseURL}${url}`, {
      headers: {
        Authorization: `Bearer ${authToken}`,
      },
    })
      .then((res) => {
        if (res.status === 200) return res.json();
      })
      .catch((e) => console.log(e));

    return data;
  }
}

/**
 * Fetches data for the organization dashboard from the server.
 *
 * @returns {Promise<any>} The data fetched from the server.
 */

export async function fetchDataForOrganization(session: Session) {
  try {
    return await fetchData(`/api/dashboard/organisation`, session);
  } catch (error) {
    console.log(error);
    if (typeof window !== "undefined" && typeof localStorage !== "undefined") {
      if (window.localStorage.getItem("appselected")) {
        window.localStorage.removeItem("appselected");
      }
    }
    signOut();
  }
}

/**
 * Capitalizes the first character of a given string and converts the rest of the string to lowercase.
 *
 * @param str - The input string to capitalize the first character.
 * @returns A new string with the first character capitalized and the rest of the string in lowercase.
 */
export const capitalizeFirstWord = (str: string): string => {
  return str.charAt(0).toUpperCase() + str.slice(1);
};
