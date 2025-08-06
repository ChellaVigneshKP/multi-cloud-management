"use client"

import {useForm} from "react-hook-form"
import {z} from "zod"
import {zodResolver} from "@hookform/resolvers/zod"
import React, {useState, useTransition} from "react"
import {useRouter} from "next/navigation"
import {motion} from "framer-motion"
import Link from "next/link"
import {useFlag} from "@unleash/nextjs/client"
import {Eye, EyeOff, Loader2} from "lucide-react"
import PhoneInput from "react-phone-number-input"
import "react-phone-number-input/style.css"
import {toggleType} from "@/lib/auth-util";
import {cn} from "@/lib/utils"
import {Button} from "@/components/ui/button"
import {Input} from "@/components/ui/input"
import {Label} from "@/components/ui/label"
import {Divider} from "@/components/custom-ui/Divider"
import {Card, CardContent, CardDescription, CardHeader, CardTitle,} from "@/components/ui/card"
import {LoginFormProps, loginSchema,} from "@/components/custom-ui/auth-types"
import {DEFAULT_COUNTRY} from "@/lib/constants";
import {LegalDisclaimer} from "@/components/custom-ui/LegalDisclaimer";
import {SocialLoginButtons} from "@/components/custom-ui/SocialLoginButtons";

type LoginData = z.infer<typeof loginSchema>

export function LoginForm({className, onLogin, ...props}: LoginFormProps) {
    const router = useRouter()
    const [isPending, startTransition] = useTransition()
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [identifierType, setIdentifierType] = useState<"email" | "phone">("email")
    const [showPassword, setShowPassword] = useState(false)

    const {
        register,
        handleSubmit,
        watch,
        setValue,
        clearErrors,
        formState: {errors},
        setError,
    } = useForm<LoginData>({
        resolver: zodResolver(loginSchema),
    })

    const identifier = watch("identifier")

    const onSubmit = (data: LoginData) => {
        setIsSubmitting(true)
        startTransition(async () => {
            try {
                const {identifier, password} = data
                const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(identifier)
                const payload = isEmail
                    ? {email: identifier, password}
                    : {phone: identifier, password}

                if (onLogin) {
                    await onLogin(payload)
                } else {
                    console.log("Login payload:", payload)
                    await new Promise((resolve) => setTimeout(resolve, 1000))
                    router.push("/dashboard")
                }
            } catch (error) {
                setError("root", {
                    type: "manual",
                    message: "Invalid credentials. Please try again.",
                })
            } finally {
                setIsSubmitting(false)
            }
        })
    }

    const handleIdentifierTypeToggle = () => {
        setIdentifierType(toggleType(identifierType))
        setValue("identifier", "")
        clearErrors("identifier")
    }

    const togglePasswordVisibility = () => {
        setShowPassword((prev) => !prev)
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
                    <CardTitle className="text-2xl">Welcome back</CardTitle>
                    <CardDescription>
                        Login with your Apple or Google account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="grid gap-6">
                        <SocialLoginButtons />
                        {(showApple || showGoogle) && <Divider/>}

                        <div className="grid gap-4">
                            <div className="grid gap-3">
                                <div className="flex items-center justify-between">
                                    <Label htmlFor="identifier">
                                        {identifierType === "email" ? "Email" : "Phone"}
                                    </Label>
                                    {showPhoneSignup && (<button
                                        type="button"
                                        className="text-xs text-muted-foreground hover:text-primary"
                                        onClick={handleIdentifierTypeToggle}
                                        aria-label={`Switch to ${toggleType(identifierType)} login`}
                                    >
                                        Use {toggleType(identifierType)} instead
                                    </button>)}
                                </div>

                                {identifierType === "email" ? (
                                    <Input
                                        id="identifier"
                                        type="email"
                                        placeholder="you@example.com"
                                        autoComplete="email"
                                        {...register("identifier")}
                                        aria-invalid={!!errors.identifier}
                                    />
                                ) : (
                                    <div>
                                        <PhoneInput
                                            id="identifier"
                                            international
                                            defaultCountry={DEFAULT_COUNTRY}
                                            placeholder="Enter phone number with country code"
                                            value={identifier}
                                            onChange={(value) => {
                                                setValue("identifier", value || "", {shouldValidate: true})
                                            }}
                                            className={cn(
                                                "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm",
                                                errors.identifier && "border-destructive"
                                            )}
                                        />
                                        <p className="text-xs text-muted-foreground mt-1">
                                            Include country code (e.g., +91)
                                        </p>
                                    </div>
                                )}

                                {errors.identifier && (
                                    <p className="text-xs text-destructive">
                                        {errors.identifier.message}
                                    </p>
                                )}
                            </div>

                            <div className="grid gap-3">
                                <div className="flex items-center">
                                    <Label htmlFor="password">Password</Label>
                                    <Link
                                        href="/forgot-password"
                                        className="ml-auto text-sm underline-offset-4 hover:underline"
                                    >
                                        Forgot password?
                                    </Link>
                                </div>
                                <div className="relative">
                                    <Input
                                        id="password"
                                        type={showPassword ? "text" : "password"}
                                        autoComplete="current-password"
                                        {...register("password")}
                                        aria-invalid={!!errors.password}
                                    />
                                    <button
                                        type="button"
                                        className="absolute right-2 top-2 text-muted-foreground hover:text-primary"
                                        onClick={togglePasswordVisibility}
                                        aria-label={showPassword ? "Hide password" : "Show password"}
                                    >
                                        {showPassword ? <EyeOff className="h-5 w-5"/> : <Eye className="h-5 w-5"/>}
                                    </button>
                                </div>
                                {errors.password && (
                                    <p className="text-xs text-destructive">
                                        {errors.password.message}
                                    </p>
                                )}
                            </div>

                            {errors.root && (
                                <p className="text-xs text-destructive">
                                    {errors.root.message}
                                </p>
                            )}

                            <Button
                                type="submit"
                                className="w-full"
                                disabled={isPending || isSubmitting}
                                aria-disabled={isPending || isSubmitting}
                            >
                                {isPending || isSubmitting ? (
                                    <span className="flex items-center justify-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin"/>
                    Logging in...
                  </span>
                                ) : (
                                    "Login"
                                )}
                            </Button>
                        </div>

                        <div className="text-center text-sm">
                            Don&apos;t have an account?{" "}
                            <Link href="/signup" className="underline underline-offset-4 hover:text-primary">
                                Sign up
                            </Link>
                        </div>
                    </form>
                </CardContent>
            </Card>

            <LegalDisclaimer type="continue" />
        </motion.div>
    )
}