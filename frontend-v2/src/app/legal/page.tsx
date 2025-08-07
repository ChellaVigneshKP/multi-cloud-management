import { LegalTabs } from "@/components/legal/legal-tabs"
import Image from "next/image"

export default function LegalPage() {
    return (
        <section className="max-w-6xl mx-auto px-4 py-12 animate-fade-up fade-in-50">
            <div className="mb-8 text-center">
                <h1 className="text-4xl font-bold tracking-tight mb-2">Legal</h1>
                <p className="text-muted-foreground">
                    Review our policies and terms of service
                </p>
            </div>

            <LegalTabs />

            {/* Footer or branding section */}
            <div className="mt-12 text-center text-xs text-muted-foreground space-y-2">
                <div className="flex justify-center items-center gap-2">
                    <Image
                        src="/c-cloud-logo.png"
                        alt="C-Cloud Logo"
                        width={20}
                        height={20}
                    />
                    <span>&copy; {new Date().getFullYear()} C-Cloud. All rights reserved.</span>
                </div>
            </div>
        </section>
    )
}
