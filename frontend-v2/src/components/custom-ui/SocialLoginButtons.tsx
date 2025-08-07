"use client"

import { Button } from "@/components/ui/button"
import { Divider } from "@/components/custom-ui/Divider"
import { useFlag } from "@unleash/nextjs/client"

// Icons
import { FcGoogle } from "react-icons/fc"
import { AiFillApple } from "react-icons/ai"
import { FaFacebook, FaGithub } from "react-icons/fa"
import { SiLinkedin } from "react-icons/si"

// Config array
const providersConfig = [
    {
        key: "google",
        label: "Sign in with Google",
        icon: FcGoogle,
        flag: "enable-google-login",
    },
    {
        key: "apple",
        label: "Sign in with Apple",
        icon: AiFillApple,
        flag: "enable-apple-login",
    },
    {
        key: "github",
        label: "Sign in with GitHub",
        icon: FaGithub,
        flag: "enable-github-login",
    },
    {
        key: "facebook",
        label: "Sign in with Facebook",
        icon: FaFacebook,
        flag: "enable-facebook-login",
    },
    {
        key: "linkedin",
        label: "Sign in with LinkedIn",
        icon: SiLinkedin,
        flag: "enable-linkedin-login",
    },
]

export function SocialLoginButtons() {
    // Evaluate flags *outside* of array methods
    const flags = {
        google: useFlag("enable-google-login"),
        apple: useFlag("enable-apple-login"),
        github: useFlag("enable-github-login"),
        facebook: useFlag("enable-facebook-login"),
        linkedin: useFlag("enable-linkedin-login"),
    }

    // Merge with config
    const enabledProviders = providersConfig.filter(({ key }) => flags[key as keyof typeof flags])

    if (enabledProviders.length === 0) return null

    return (
        <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-4">
                {enabledProviders.map(({ key, label, icon: Icon }) => (
                    <Button
                        key={key}
                        variant="outline"
                        className="w-full"
                        type="button"
                        aria-label={label}
                        onClick={() => console.log(`Login with ${key}`)} // Replace it with actual logic
                    >
                        <Icon className="mr-2 h-4 w-4" />
                        {label}
                    </Button>
                ))}
            </div>
            <Divider />
        </div>
    )
}
