import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import EmotionProvider from "@/components/EmotionProvider";
import EmotionRegistry from "@/app/emotion/EmotionRegistry";
import { AuthProvider } from "@/contexts/auth-context";
import ClientLoaderWrapper from "@/contexts/ClientLoaderWrapper";
const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});
const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "C-Cloud",
  description: "C-Cloud is a cloud-native platform for building and deploying applications.",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className={`${geistSans.variable} ${geistMono.variable}`}>
      <body>
        <EmotionRegistry>
          <EmotionProvider>
            <AuthProvider>
              <ClientLoaderWrapper>
                {children}
              </ClientLoaderWrapper>
            </AuthProvider>
          </EmotionProvider>
        </EmotionRegistry>
      </body>
    </html>
  );
}