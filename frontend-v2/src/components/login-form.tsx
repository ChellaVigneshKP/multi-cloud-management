"use client"

import { useForm } from "react-hook-form"
import { z } from "zod"
import { zodResolver } from "@hookform/resolvers/zod"
import React, { useTransition } from "react"
import { useRouter } from "next/navigation"
import { motion } from "framer-motion"
import Link from "next/link"

import { FcGoogle } from "react-icons/fc"
import { AiFillApple } from "react-icons/ai"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {Divider} from "@/components/custom-ui/Divider"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"

const loginSchema = z.object({
    email: z.string().email("Please enter a valid email address"),
    password: z.string().min(6, "Password must be at least 6 characters"),
})

type LoginData = z.infer<typeof loginSchema>

interface LoginFormProps extends React.ComponentProps<typeof motion.div> {
    onLogin?: (data: LoginData) => Promise<void>
}

export function LoginForm({ className, onLogin, ...props }: LoginFormProps) {
    const router = useRouter()

    const {
        register,
        handleSubmit,
        formState: { errors },
        setError,
    } = useForm<LoginData>({
        resolver: zodResolver(loginSchema),
    })

    const [isPending, startTransition] = useTransition()

    const onSubmit = (data: LoginData) => {
        startTransition(async () => {
            try {
                if (onLogin) {
                    await onLogin(data)
                } else {
                    console.log("User Logged In:", data)
                    await new Promise((resolve) => setTimeout(resolve, 1000))
                    router.push("/dashboard") // Change as per your route
                }
            } catch (error) {
                setError("root", {
                    type: "manual",
                    message: "Invalid email or password",
                })
            }
        })
    }

    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
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
                        <div className="flex flex-col gap-4">
                            <Button variant="outline" className="w-full" type="button">
                                <AiFillApple className="mr-2 h-4 w-4" />
                                Login with Apple
                            </Button>
                            <Button variant="outline" className="w-full" type="button">
                                <FcGoogle className="mr-2 h-4 w-4" />
                                Login with Google
                            </Button>
                        </div>

                        <Divider />

                        <div className="grid gap-4">
                            <div className="grid gap-3">
                                <Label htmlFor="email">Email</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="you@example.com"
                                    autoComplete="email"
                                    {...register("email")}
                                    aria-invalid={!!errors.email}
                                />
                                {errors.email && (
                                    <p className="text-xs text-destructive">
                                        {errors.email.message}
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
                                <Input
                                    id="password"
                                    type="password"
                                    autoComplete="current-password"
                                    {...register("password")}
                                    aria-invalid={!!errors.password}
                                />
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
                                disabled={isPending}
                                aria-disabled={isPending}
                            >
                                {isPending ? (
                                    <span className="animate-pulse">Logging in...</span>
                                ) : (
                                    "Login"
                                )}
                            </Button>
                        </div>

                        <div className="text-center text-sm">
                            Don&apos;t have an account?{" "}
                            <Link href="/signup" className="underline underline-offset-4">
                                Sign up
                            </Link>
                        </div>
                    </form>
                </CardContent>
            </Card>

            <div className="text-muted-foreground text-center text-xs *:underline *:underline-offset-4 hover:*:text-primary">
                By clicking continue, you agree to our{" "}
                <Link href="/terms">Terms of Service</Link> and{" "}
                <Link href="/privacy">Privacy Policy</Link>.
            </div>
        </motion.div>
    )
}