import { cn } from "@/lib/utils"
import zxcvbn from "zxcvbn"
import React from "react";

export function PasswordStrengthMeter({ password }: { password: string }) {
    if (!password) return null

    const result = zxcvbn(password)
    const strength = ["Very Weak", "Weak", "Medium", "Strong", "Very Strong"][result.score]

    return (
        <div className="mt-1">
            <div className="flex h-1 w-full overflow-hidden rounded-full bg-muted">
                <div
                    className={cn(
                        "h-full transition-all duration-500",
                        result.score === 0 && "w-1/5 bg-red-500",
                        result.score === 1 && "w-2/5 bg-orange-500",
                        result.score === 2 && "w-3/5 bg-yellow-500",
                        result.score === 3 && "w-4/5 bg-green-500",
                        result.score === 4 && "w-full bg-emerald-500"
                    )}
                />
            </div>
            <p className="text-xs text-muted-foreground mt-1">
                Strength: <span className="font-medium">{strength}</span>
                {result.feedback.suggestions?.length > 0 && <span> – {result.feedback.suggestions.join(" ")}</span>}
            </p>
        </div>
    )
}