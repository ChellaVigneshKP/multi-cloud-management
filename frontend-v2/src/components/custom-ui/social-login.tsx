"use client"

import { Button } from "@/components/ui/button"
import { Divider } from "@/components/custom-ui/divider"
import { useEnabledProviders } from "@/lib/auth-providers"

export function SocialLogin() {
    const enabledProviders = useEnabledProviders()

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
                        onClick={() => console.log(`Login with ${key}`)}
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