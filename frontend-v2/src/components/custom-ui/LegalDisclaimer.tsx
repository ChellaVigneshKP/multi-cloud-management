import Link from "next/link"

export function LegalDisclaimer({ type = "continue" }: { type?: "continue" | "signup" }) {
    return (
        <div className="text-muted-foreground text-center text-xs *:underline *:underline-offset-4 hover:*:text-primary">
            {type === "continue"
                ? <>By clicking continue, you agree to our <Link href="/terms">Terms of Service</Link> and <Link href="/privacy">Privacy Policy</Link>.</>
                : <>By signing up, you agree to our <Link href="/terms">Terms of Service</Link> and <Link href="/privacy">Privacy Policy</Link>.</>
            }
        </div>
    )
}