"use client";

import { FlagProvider, useFlagsStatus } from "@unleash/nextjs/client";
import React from "react";

interface Props {
    sessionId: string;
    children: React.ReactNode;
}

function InnerUnleashProvider({ children }: { children: React.ReactNode }) {
    const { flagsReady } = useFlagsStatus();

    if (!flagsReady) {
        return (
            <div className="flex h-screen items-center justify-center text-muted-foreground">
                Initializing feature flags...
            </div>
        );
    }

    return <>{children}</>;
}

export function UnleashProvider({ sessionId, children }: Props) {
    return (
        <FlagProvider config={{ context: { sessionId } }}>
            <InnerUnleashProvider>
                {children}
            </InnerUnleashProvider>
        </FlagProvider>
    );
}
