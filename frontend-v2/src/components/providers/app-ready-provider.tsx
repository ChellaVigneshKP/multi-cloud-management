"use client";

import React from "react";
import { FlagProvider, useFlagsStatus } from "@unleash/nextjs/client";
import { FingerprintProvider, useFingerprint } from "./fingerprint-provider";
import { Loading } from "@/components/loading/loading";

interface AppReadyProviderProps {
    sessionId: string;
    children: React.ReactNode;
}

function InnerAppReady({ children }: { children: React.ReactNode }) {
    const { flagsReady } = useFlagsStatus();
    const { isFingerprintReady } = useFingerprint();

    const isReady = flagsReady && isFingerprintReady;

    if (!isReady) {
        return <Loading />;
    }

    return <>{children}</>;
}

export function AppReadyProvider({ sessionId, children }: AppReadyProviderProps) {
    return (
        <FlagProvider config={{ context: { sessionId } }}>
            <FingerprintProvider>
                <InnerAppReady>{children}</InnerAppReady>
            </FingerprintProvider>
        </FlagProvider>
    );
}