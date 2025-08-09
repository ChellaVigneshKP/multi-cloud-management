import {LegalSection} from "@/components/legal/legal-section"
import React from "react"

export const tabItems = [
    {
        id: "terms",
        title: "Terms of Service",
        content: (
            <div className="prose prose-sm dark:prose-invert max-w-none space-y-6">
                <p>
                    Welcome to <strong>C-Cloud</strong>. These Terms of Service govern
                    your access to and use of our platform, which allows users to manage
                    virtual machines (VMs) across multiple cloud providers.
                </p>

                <LegalSection title="1. Acceptance of Terms">
                    <p>
                        By accessing or using C-Cloud, you agree to be bound by these Terms.
                        If you do not agree, please do not use the service.
                    </p>
                </LegalSection>

                <LegalSection title="2. User Accounts">
                    <ul className="list-disc pl-5 space-y-1">
                        <li>You must create an account to use our platform.</li>
                        <li>
                            You agree to provide accurate, current, and complete information.
                        </li>
                        <li>
                            You are responsible for maintaining the confidentiality of your
                            account and password.
                        </li>
                    </ul>
                </LegalSection>

                <LegalSection title="3. Data Collection">
                    <p>
                        We collect personal and sensitive information including your email,
                        phone number, and cloud provider credentials. By using the platform,
                        you consent to our data handling practices as described in our{" "}
                        <a
                            href="/privacy"
                            className="text-primary hover:underline font-medium"
                        >
                            Privacy Policy
                        </a>
                        .
                    </p>
                </LegalSection>

                <LegalSection title="4. Credential Use">
                    <p>
                        You grant C-Cloud permission to use your cloud credentials to manage
                        VMs solely on your behalf. You remain responsible for the security
                        and legality of the credentials you provide.
                    </p>
                </LegalSection>

                <LegalSection title="5. Prohibited Conduct">
                    <ul className="list-disc pl-5 space-y-1">
                        <li>Using credentials you don&#39;t have the right to share</li>
                        <li>Attempting to reverse engineer or hack the system</li>
                        <li>Violating any applicable laws</li>
                    </ul>
                </LegalSection>

                <LegalSection title="6. Service Availability">
                    <p>
                        We strive for high availability but do not guarantee uninterrupted
                        service. We may suspend or discontinue parts of the platform without
                        notice.
                    </p>
                </LegalSection>

                <LegalSection title="7. Limitation of Liability">
                    <p>
                        C-Cloud is provided &#34;as is&#34; without warranty. We are not liable for
                        any indirect or consequential damages resulting from the use of our
                        service.
                    </p>
                </LegalSection>

                <LegalSection title="8. Governing Law">
                    <p>
                        These Terms are governed by the laws of the jurisdiction in which
                        C-Cloud operates.
                    </p>
                </LegalSection>

                <LegalSection title="9. Contact">
                    <p>
                        For questions or support, contact us at{" "}
                        <a
                            href="mailto:info@chellavignesh.com"
                            className="text-primary hover:underline font-medium"
                        >
                            info@chellavignesh.com
                        </a>
                        .
                    </p>
                </LegalSection>
            </div>
        ),
    },
    {
        id: "privacy",
        title: "Privacy Policy",
        content: (
            <div className="prose prose-sm dark:prose-invert max-w-none space-y-6">
                <p>
                    This Privacy Policy describes how <strong>C-Cloud</strong> collects,
                    uses, and discloses your information when you use our platform.
                </p>

                <LegalSection title="1. Information We Collect">
                    <ul className="list-disc pl-5 space-y-1">
                        <li>Email address and phone number</li>
                        <li>Cloud provider credentials (e.g., AWS, Azure, GCP)</li>
                        <li>Usage data including logs and VM interactions</li>
                        <li>Device and browser information</li>
                    </ul>
                </LegalSection>

                <LegalSection title="2. How We Use Your Information">
                    <ul className="list-disc pl-5 space-y-1">
                        <li>To provide and maintain the platform</li>
                        <li>To authenticate your access and secure your account</li>
                        <li>To improve user experience and debug issues</li>
                        <li>To send alerts or important updates</li>
                    </ul>
                </LegalSection>

                <LegalSection title="3. Credential Storage and Security">
                    <p>
                        Your cloud credentials are encrypted at rest using industry best
                        practices. We only access your credentials when needed to perform
                        actions on your behalf.
                    </p>
                </LegalSection>

                <LegalSection title="4. Data Sharing">
                    <p>
                        We do not sell or rent your personal data. Your data may be shared:
                    </p>
                    <ul className="list-disc pl-5 space-y-1">
                        <li>With trusted infrastructure providers (e.g., hosting services)</li>
                        <li>When required by law or to comply with legal processes</li>
                    </ul>
                </LegalSection>

                <LegalSection title="5. Data Retention">
                    <p>
                        We retain your data only as long as necessary for the purposes
                        outlined in this policy, or as required by applicable laws.
                    </p>
                </LegalSection>

                <LegalSection title="6. Your Rights">
                    <ul className="list-disc pl-5 space-y-1">
                        <li>Access the data we hold about you</li>
                        <li>Request correction or deletion of your data</li>
                        <li>Withdraw consent at any time</li>
                    </ul>
                </LegalSection>

                <LegalSection title="7. International Transfers">
                    <p>
                        Your information may be stored and processed in countries other
                        than your own. We take appropriate safeguards to ensure data
                        protection.
                    </p>
                </LegalSection>

                <LegalSection title="8. Changes to this Policy">
                    <p>
                        We may update this Privacy Policy from time to time. Changes will
                        be posted on this page with an updated revision date.
                    </p>
                </LegalSection>

                <LegalSection title="9. Contact Us">
                    <p>
                        If you have questions, contact us at{" "}
                        <a
                            href="mailto:info@chellavignesh.com"
                            className="text-primary hover:underline font-medium"
                        >
                            info@chellavignesh.com
                        </a>
                        .
                    </p>
                </LegalSection>
            </div>
        ),
    },
    {
        id: "cookies",
        title: "Cookie Policy",
        content: (
            <div className="prose prose-sm dark:prose-invert max-w-none space-y-6">
                <p>
                    This Cookie Policy explains how <strong>C-Cloud</strong> uses cookies
                    and similar tracking technologies when you access or use our website.
                </p>

                <LegalSection title="1. What Are Cookies?">
                    <p>
                        Cookies are small text files stored on your device when you visit a
                        website. They help us recognize your device and enhance your user
                        experience.
                    </p>
                </LegalSection>

                <LegalSection title="2. Types of Cookies We Use">
                    <ul className="list-disc pl-5 space-y-1">
                        <li><strong>Essential Cookies:</strong> Required for the operation of our site.</li>
                        <li><strong>Analytics Cookies:</strong> Help us understand usage and improve performance.</li>
                        <li><strong>Functionality Cookies:</strong> Remember user preferences and settings.</li>
                    </ul>
                </LegalSection>

                <LegalSection title="3. Third-Party Cookies">
                    <p>
                        We may use third-party services such as Google Analytics. These
                        services may place their own cookies to collect data about your
                        interaction with our platform.
                    </p>
                </LegalSection>

                <LegalSection title="4. How to Manage Cookies">
                    <p>
                        You can manage or delete cookies in your browser settings. Disabling
                        certain cookies may affect site functionality.
                    </p>
                </LegalSection>

                <LegalSection title="5. Updates to This Policy">
                    <p>
                        We may update this Cookie Policy from time to time. Revisions will
                        be posted on this page.
                    </p>
                </LegalSection>

                <LegalSection title="6. Contact">
                    <p>
                        For questions about our use of cookies, contact us at{" "}
                        <a
                            href="mailto:info@chellavignesh.com"
                            className="text-primary hover:underline font-medium"
                        >
                            info@chellavignesh.com
                        </a>
                        .
                    </p>
                </LegalSection>
            </div>
        ),
    },
]
