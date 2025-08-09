import {z} from "zod"
import zxcvbn from "zxcvbn";
import React from "react";
import {motion} from "framer-motion";
import {MIN_PASSWORD_SCORE} from "@/lib/constants";
import {parsePhoneNumberFromString} from 'libphonenumber-js/max'

export const signupSchema = z.object({
    name: z.string().min(2, "Name must be at least 2 characters"),
    contactMethod: z.discriminatedUnion("type", [
        z.object({
            type: z.literal("email"),
            value: z.email("Please enter a valid email address"),
        }),
        z.object({
            type: z.literal("phone"),
            value: z
                .string()
                .min(1, "Phone number is required")
                .refine((value) => {
                    const phoneNumber = parsePhoneNumberFromString(value)
                    return phoneNumber?.isValid() && phoneNumber.getType() === "MOBILE"
                }, "Only valid mobile numbers are allowed"),
        }),
    ]),
    password: z
        .string()
        .min(8, "Password must be at least 8 characters")
        .refine((password) => zxcvbn(password).score >= MIN_PASSWORD_SCORE, {
            message: "Password is too weak. Please include uppercase, lowercase, numbers, and symbols.",
        }),
    confirmPassword: z.string(),
}).superRefine((data, ctx) => {
    if (data.password !== data.confirmPassword) {
        ctx.addIssue({
            code: "custom",
            message: "Passwords do not match",
            path: ["confirmPassword"],
        })
    }
})

export type SignupData = z.infer<typeof signupSchema>

export interface SignupFormProps extends React.ComponentProps<typeof motion.div> {
    onSignup?: (data: SignupData) => Promise<void>
    defaultCountry?: string
}

export const loginSchema = z.object({
    identifier: z
        .string()
        .min(1, "Email or phone number is required")
        .refine((value) => {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
            if (emailRegex.test(value)) return true
            const phoneNumber = parsePhoneNumberFromString(value)
            return phoneNumber?.isValid() && phoneNumber.getType() === "MOBILE"
        }, {
            message: "Please enter a valid mobile number with country code or a valid email",
        }),
    password: z.string().min(6, "Password must be at least 6 characters"),
    visitorId: z.string().optional()
})

export interface LoginFormProps extends React.ComponentProps<typeof motion.div> {
    onLogin?: (data: { email: string; password: string } | { phone: string; password: string }) => Promise<void>
}