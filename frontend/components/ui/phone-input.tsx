"use client";
import React, { useEffect, useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "./input";
import { Label } from "./label";
import { countryCodesArray } from "@/app/constants/constants";

const PhoneInput = ({ register, errors, setCountryCode, countryCode }: any) => {
  const handleCountryCodeChange = (e: string) => {
    setCountryCode(e);
  };

  return (
    <div className="form-field">
      <Label htmlFor="phone">Phone Number*</Label>
      <div className="phone-input-container flex items-center w-full gap-4">
        <Select value={countryCode} onValueChange={handleCountryCodeChange}>
          <SelectTrigger className="w-[70px] h-full flex rounded-lg border border-input dark:border-[#424242] bg-grey-50 dark:bg-[rgb(48,56,70)] text-sm ring-offset-background">
            <SelectValue>{`${countryCode || "+91"}`}</SelectValue>
          </SelectTrigger>
          <SelectContent side="bottom" className="max-h-[300px]">
            {countryCodesArray.map(
              (country: { code: string; name: string }) => (
                <SelectItem key={country.code} value={country.code}>
                  {`${country.code} (${country.name})`}
                </SelectItem>
              ),
            )}
          </SelectContent>
        </Select>
        <div className="w-full flex flex-1">
          <Input
            type="tel"
            id="phone"
            {...register("phoneNumber", {
              required: "Phone number is required",
            })}
            placeholder="Enter phone number"
            className="phone-input"
          />
        </div>
      </div>
      {errors.phoneNumber && (
        <p className="error-text">{errors.phoneNumber?.message}</p>
      )}
    </div>
  );
};

export default PhoneInput;
