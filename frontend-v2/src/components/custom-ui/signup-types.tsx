import { z } from "zod"
import {isValidPhoneNumber} from "react-phone-number-input";
import zxcvbn from "zxcvbn";
import React from "react";
import {motion} from "framer-motion";

const MIN_PASSWORD_SCORE = 2
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
                .refine((value) => isValidPhoneNumber(value), "Please enter a valid international phone number"),
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