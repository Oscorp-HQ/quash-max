import { Report } from "@/app/types/dashboard-types";

/**
 * Filter data based on the selected filter criteria.
 *
 * @param selectedFilter - The filter criteria selected by the user.
 * @param dataArray - The array of data to be filtered.
 * @returns An array of filtered data based on the selected filter criteria.
 */
export const filterData = (selectedFilter: string, dataArray: Report[]) => {
  const currentDate = new Date();
  const filteredData = dataArray.filter((item: Report) => {
    const itemDate = new Date(item.createdAt); // Assuming each item has a "date" property

    switch (selectedFilter) {
      case "Today": {
        return isSameDay(itemDate, currentDate);
      }
      case "Yesterday": {
        let yesterday = new Date(currentDate);
        yesterday.setDate(currentDate.getDate() - 1);
        return isSameDay(itemDate, yesterday);
      }
      case "Last 7 days": {
        return isWithinLastNDays(itemDate, currentDate, 7);
      }
      case "Last 15 days": {
        return isWithinLastNDays(itemDate, currentDate, 15);
      }
      case "Last 30 days": {
        return isWithinLastNDays(itemDate, currentDate, 30);
      }
      case "Last 6 Months": {
        return isWithinLastNMonths(itemDate, currentDate, 6);
      }
      default: {
        return true;
      }
    }
  });
  return filteredData;
};

/**
 * Check if two dates are on the same day.
 *
 * @param {Date} date1 - The first date to compare.
 * @param {Date} date2 - The second date to compare.
 * @returns {boolean} Returns true if the two dates are on the same day, false otherwise.
 */
const isSameDay = (date1: Date, date2: Date): boolean => {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  );
};

/**
 * Check if a given date is within the last N days from the current date.
 *
 * @param {Date} date - The date to check.
 * @param {Date} currentDate - The current date.
 * @param {number} n - The number of days to check against.
 * @returns {boolean} Returns true if the date is within the last N days, false otherwise.
 */
const isWithinLastNDays = (
  date: Date,
  currentDate: Date,
  n: number,
): boolean => {
  const timeDifference = currentDate.getTime() - date.getTime();
  const daysDifference = timeDifference / (1000 * 3600 * 24);
  return daysDifference <= n;
};

/**
 * Check if a given date is within the last N months from the current date.
 *
 * @param {Date} date - The date to check.
 * @param {Date} currentDate - The current date.
 * @param {number} n - The number of months to check against.
 * @returns {boolean} Returns true if the date is within the last N months, false otherwise.
 */
const isWithinLastNMonths = (
  date: Date,
  currentDate: Date,
  n: number,
): boolean => {
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth();
  const itemYear = date.getFullYear();
  const itemMonth = date.getMonth();

  const yearDifference = currentYear - itemYear;
  const monthDifference = currentMonth - itemMonth + yearDifference * 12;

  return monthDifference <= n;
};
