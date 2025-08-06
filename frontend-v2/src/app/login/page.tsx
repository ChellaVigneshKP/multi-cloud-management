import Image from "next/image";
import { LoginForm } from "@/components/login-form";

export default function LoginPage() {
    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10">
            <div className="flex w-full max-w-sm flex-col gap-6">
                <a href="#" className="flex items-center gap-3 self-center font-medium">
                    <Image
                        src="/c-cloud-logo.png"
                        alt="C-Cloud Logo"
                        width={40}
                        height={21}
                        className="rounded-md"
                    />
                    <span className="text-lg font-semibold">C-Cloud</span>
                </a>
                <LoginForm />
            </div>
        </div>
    )
}