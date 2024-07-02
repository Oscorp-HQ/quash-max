import * as React from "react";
import { CheckIcon } from "@radix-ui/react-icons";
import { Column } from "@tanstack/react-table";

import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { CaretDown, X } from "@/app/lib/icons";
import { useEffect, useState } from "react";
import { useAutoAnimate } from "@formkit/auto-animate/react";

interface DataTableFacetedFilter<TData, TValue> {
  column?: Column<TData, TValue>;
  title?: string;
  options: Option[];
}

interface Option {
  label: string;
  value: any;
  icon?: JSX.Element;
}

/**
 * Function component for rendering a faceted filter in a data table.
 *
 * @template TData - The type of data in the table.
 * @template TValue - The type of value in the table.
 *
 * @param {DataTableFacetedFilter<TData, TValue>} props - The props for the faceted filter component.
 * @param {Column<TData, TValue>} props.column - The column data for filtering.
 * @param {string} props.title - The title of the filter.
 * @param {Array<{ label: string, value: string | boolean, icon?: JSX.Element }>} props.options - The filter options with labels and values.
 *
 * @returns {JSX.Element} A JSX element representing the faceted filter component.
 */

export function DataTableFacetedFilter<TData, TValue>({
  column,
  title,
  options,
}: DataTableFacetedFilter<TData, TValue>): JSX.Element {
  const facets = column?.getFacetedUniqueValues();
  const selectedValues = new Set(column?.getFilterValue() as string[]);
  const [filterList, setfilterList] = useState<Option[]>([]);
  const [parent, enableAnimations] = useAutoAnimate(/* optional config */);

  useEffect(() => {
    if (facets !== undefined) {
      let temp: Option[] = [];
      temp = Array.from(facets.keys()).map((el: string) => ({
        value: el,
        label: el,
      }));
      if (column?.id === "source" || column?.id === "reportedByName") {
        setfilterList([...temp]);
      } else {
        setfilterList([...options]);
      }
    }
  }, [facets]);

  return (
    <Popover>
      <div className="flex h-8 min-w-fit   border-solid custom-border border rounded-[8px] overflow-hidden">
        <PopoverTrigger asChild>
          <Button
            variant="ghost"
            className={` rounded-none min-w-fit  ${
              selectedValues?.size > 0 && ""
            } filter-action-button`}
          >
            {title}
            <CaretDown className="ml-2" size={16} color="#9E9E9E" />
          </Button>
        </PopoverTrigger>

        {selectedValues?.size > 0 && (
          <>
            <Badge
              variant="secondary"
              className="px-1 rounded-l-none font-normal lg:hidden bg-[#FAFAFA] "
            >
              {selectedValues.size}
            </Badge>
            <div className="hidden lg:flex" ref={parent}>
              {selectedValues.size > 3 ? (
                <Badge
                  variant="secondary"
                  className="px-1 min-w-fit rounded-l-none custom-border border-l border-r-0 border-b-0 border-t-0 font-normal  dark:hover:bg-slate-800 filter-value"
                >
                  {selectedValues.size} selected
                </Badge>
              ) : (
                filterList
                  .filter((option: Option) => selectedValues.has(option.value))
                  .map((option: Option) => (
                    <Badge
                      variant="secondary"
                      key={option.value}
                      className="rounded-none min-w-fit px-1 font-normal flex gap-1 custom-border border-l border-r-0 border-b-0 border-t-0  dark:hover:bg-slate-800 filter-value"
                    >
                      {option.label}

                      <X
                        className="text-[#424242] dark:text-[#9E9E9E] hover:cursor-pointer"
                        size={16}
                        onClick={() => {
                          selectedValues.delete(option.value);
                          const filterValues = Array.from(selectedValues);
                          column?.setFilterValue(
                            filterValues.length ? filterValues : undefined,
                          );
                        }}
                      />
                    </Badge>
                  ))
              )}
            </div>
          </>
        )}
      </div>

      <PopoverContent className="w-[200px] p-2" align="start">
        <Command>
          {column?.id === "reportedByName" && (
            <CommandInput placeholder={title} />
          )}
          <CommandList>
            <CommandEmpty>No results found.</CommandEmpty>
            <CommandGroup>
              <div className="flex gap-2 px-2 py-3 items-center justify-between text-[14px] font-[500] leading-[18.2px]">
                <span className="text-[#9E9E9E]">Filters</span>
                <span
                  onClick={() => column?.setFilterValue(undefined)}
                  className={`${
                    selectedValues.size > 0
                      ? "text-black-white hover:cursor-pointer"
                      : "text-[#9E9E9E]"
                  }`}
                >
                  Clear all
                </span>
              </div>
              {filterList.map((option: Option, key: number) => {
                const isSelected = selectedValues.has(option.value);
                return (
                  <CommandItem
                    className="flex gap-2 px-2 py-3"
                    key={key}
                    onSelect={() => {
                      if (isSelected) {
                        selectedValues.delete(option.value);
                      } else {
                        selectedValues.add(option.value);
                      }
                      const filterValues = Array.from(selectedValues);
                      column?.setFilterValue(
                        filterValues.length ? filterValues : undefined,
                      );
                    }}
                  >
                    <div
                      className={cn(
                        "mr-2 flex h-4 w-4 items-center justify-center border border-primary shadow ",
                        isSelected
                          ? "bg-primary text-primary-foreground"
                          : " [&_svg]:invisible",
                      )}
                    >
                      <CheckIcon className={cn("h-4 w-4")} />
                    </div>
                    <div className="flex gap-2 items-center">
                      {option.icon && option.icon}
                      <span>{option.label}</span>
                    </div>

                    {facets?.get(option.value) ? (
                      <span className="ml-auto flex h-4 w-4 items-center justify-center font-mono text-xs">
                        {facets.get(option.value)}
                      </span>
                    ) : (
                      <span className="ml-auto flex h-4 w-4 items-center justify-center font-mono text-xs">
                        0
                      </span>
                    )}
                  </CommandItem>
                );
              })}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
