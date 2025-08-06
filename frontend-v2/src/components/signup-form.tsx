"use client"

import React, {useState, useTransition} from "react"
import {useForm} from "react-hook-form"
import {zodResolver} from "@hookform/resolvers/zod"
import {useRouter} from "next/navigation"
import {motion} from "framer-motion"
import Link from "next/link"
import {cn} from "@/lib/utils"
import {Button} from "@/components/ui/button"
import {Input} from "@/components/ui/input"
import {Label} from "@/components/ui/label"
import {Card, CardContent, CardDescription, CardHeader, CardTitle,} from "@/components/ui/card"
import {Loader2} from "lucide-react"
import PhoneInput from "react-phone-number-input"
import "react-phone-number-input/style.css"
import {Divider} from "@/components/custom-ui/Divider"
import {PasswordStrengthMeter} from "@/components/custom-ui/password-strength-meter";
import {SignupData, SignupFormProps, signupSchema} from "@/components/custom-ui/auth-types"
import {DEFAULT_COUNTRY} from "@/lib/constants";
import {useFlag} from "@unleash/nextjs/client";
import {LegalDisclaimer} from "@/components/custom-ui/LegalDisclaimer";
import {SocialLoginButtons} from "@/components/custom-ui/SocialLoginButtons";

export function SignupForm({className, onSignup, ...props}: SignupFormProps) {
    const router = useRouter()
    const [showPassword, setShowPassword] = useState(false)
    const [isPending, startTransition] = useTransition()

    const {
        register,
        handleSubmit,
        watch,
        setValue,
        clearErrors,
        formState: {errors},
        setError,
    } = useForm<SignupData>({
        resolver: zodResolver(signupSchema),
        defaultValues: {
            contactMethod: {type: "email", value: ""},
        },
    })

    const contactMethod = watch("contactMethod")
    const password = watch("password", "")

    const onSubmit = (data: SignupData) => {
        startTransition(async () => {
            try {
                await onSignup?.(data)
                router.push("/verify")
            } catch {
                setError("root", {
                    type: "manual",
                    message: "Failed to create account. Please try again.",
                })
            }
        })
    }

    const handleContactTypeToggle = () => {
        const newType = contactMethod.type === "email" ? "phone" : "email"
        if (newType === "phone" && !showPhoneSignup) return
        setValue("contactMethod", {type: newType, value: ""})
        clearErrors("contactMethod")
    }

    const showGoogle = useFlag("enable-google-login")
    const showApple = useFlag("enable-apple-login")
    const showPhoneSignup = useFlag("enable-phone-signup")

    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.4}}
            className={cn("flex flex-col gap-6", className)}
            {...props}
        >
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-2xl">Create an account</CardTitle>
                    <CardDescription>Sign up with your Apple or Google account</CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="grid gap-6">
                        <SocialLoginButtons />

                        {(showApple || showGoogle) && <Divider/>}

                        <div className="grid gap-4">
                            <div className="grid gap-3">
                                <Label htmlFor="name">Name</Label>
                                <Input id="name" type="text" placeholder="Your full name"
                                       autoComplete="name" {...register("name")} />
                                {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
                            </div>

                            <div className="grid gap-3">
                                <div className="flex items-center justify-between">
                                    <Label
                                        htmlFor="contactMethod">{contactMethod.type === "email" ? "Email" : "Phone"}</Label>
                                    {showPhoneSignup && (
                                        <button
                                            type="button"
                                            className="text-xs text-muted-foreground hover:text-primary"
                                            onClick={handleContactTypeToggle}
                                        >
                                            Use {contactMethod.type === "email" ? "phone" : "email"} instead
                                        </button>
                                    )}
                                </div>

                                {contactMethod.type === "email" ? (
                                    <Input
                                        id="email"
                                        type="email"
                                        placeholder="you@example.com"
                                        autoComplete="username"
                                        value={contactMethod.value}
                                        onChange={(e) =>
                                            setValue("contactMethod", {
                                                type: "email",
                                                value: e.target.value
                                            }, {shouldValidate: true})
                                        }
                                    />
                                ) : (
                                    <PhoneInput
                                        international
                                        defaultCountry={DEFAULT_COUNTRY}
                                        placeholder="Enter phone number"
                                        value={contactMethod.value}
                                        onChange={(value) =>
                                            setValue("contactMethod", {
                                                type: "phone",
                                                value: value || ""
                                            }, {shouldValidate: true})
                                        }
                                        className={cn(
                                            "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm",
                                            errors.contactMethod && "border-destructive"
                                        )}
                                    />
                                )}

                                {errors.contactMethod && (
                                    <p className="text-xs text-destructive">
                                        {"message" in errors.contactMethod
                                            ? errors.contactMethod.message
                                            : errors.contactMethod.value?.message}
                                    </p>
                                )}
                            </div>

                            <div className="grid gap-3">
                                <Label htmlFor="password">Password</Label>
                                <div className="relative">
                                    <Input
                                        id="password"
                                        className="pr-12"
                                        placeholder="Create a strong password"
                                        type={showPassword ? "text" : "password"}
                                        autoComplete="new-password"
                                        {...register("password")}
                                    />
                                    {password && (
                                        <button
                                            type="button"
                                            className="absolute right-2 top-2 text-xs text-muted-foreground"
                                            onClick={() => setShowPassword(!showPassword)}
                                            aria-label={showPassword ? "Hide password" : "Show password"}
                                        >
                                            {showPassword ? "Hide" : "Show"}
                                        </button>
                                    )}
                                </div>
                                <PasswordStrengthMeter password={password}/>
                                {errors.password &&
                                    <p className="text-xs text-destructive">{errors.password.message}</p>}
                            </div>

                            <div className="grid gap-3">
                                <Label htmlFor="confirmPassword">Confirm Password</Label>
                                <Input
                                    id="confirmPassword"
                                    type="password"
                                    placeholder="Confirm your password"
                                    autoComplete="new-password"
                                    {...register("confirmPassword")}
                                />
                                {errors.confirmPassword && (
                                    <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
                                )}
                            </div>

                            {errors.root && <p className="text-xs text-destructive">{errors.root.message}</p>}

                            <Button type="submit" className="w-full" disabled={isPending} aria-disabled={isPending}>
                                {isPending ? (
                                    <span className="flex items-center justify-center gap-2">
                                        <Loader2 className="h-4 w-4 animate-spin"/>
                                        Creating account...
                                    </span>
                                ) : (
                                    "Create Account"
                                )}
                            </Button>
                        </div>

                        <div className="text-center text-sm">
                            Already have an account?{" "}
                            <Link href="/login" className="underline underline-offset-4 hover:text-primary">
                                Log in
                            </Link>
                        </div>
                    </form>
                </CardContent>
            </Card>

            <LegalDisclaimer type="signup" />
        </motion.div>
    )
}