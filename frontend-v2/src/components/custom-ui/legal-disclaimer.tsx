import Link from "next/link"
import Image from "next/image"

export function LegalDisclaimer({ type = "continue" }: { type?: "continue" | "signup" }) {
    return (
        <div className="mt-6 space-y-2 text-center text-xs text-muted-foreground">
            <div className="*:underline *:underline-offset-4 hover:*:text-primary">
                {type === "continue" ? (
                    <>
                        By clicking continue, you agree to our{" "}
                        <Link href="/legal">Terms of Service and Privacy Policy</Link>.
                    </>
                ) : (
                    <>
                        By signing up, you agree to our{" "}
                        <Link href="/legal">Terms of Service and Privacy Policy</Link>.
                    </>
                )}
            </div>

            <div className="flex justify-center items-center gap-2 mt-2">
                <Image
                    src="/c-cloud-logo.png"
                    alt="C-Cloud Logo"
                    width={16}
                    height={16}
                />
                <span className="text-xs text-muted-foreground">&copy; {new Date().getFullYear()} C-Cloud. All rights reserved.</span>
            </div>
        </div>
    )
}