"use client"

import { Button } from "@/components/ui/button"
import { FcGoogle } from "react-icons/fc"
import { AiFillApple } from "react-icons/ai"
import { useFlag } from "@unleash/nextjs/client"

export function SocialLoginButtons() {
    const showApple = useFlag("enable-apple-login")
    const showGoogle = useFlag("enable-google-login")

    if (!showApple && !showGoogle) return null

    return (
        <div className="flex flex-col gap-4">
            {showApple && (
                <Button variant="outline" className="w-full" type="button" aria-label="Sign in with Apple">
                    <AiFillApple className="mr-2 h-4 w-4" />
                    Sign in with Apple
                </Button>
            )}
            {showGoogle && (
                <Button variant="outline" className="w-full" type="button" aria-label="Sign in with Google">
                    <FcGoogle className="mr-2 h-4 w-4" />
                    Sign in with Google
                </Button>
            )}
        </div>
    )
}
