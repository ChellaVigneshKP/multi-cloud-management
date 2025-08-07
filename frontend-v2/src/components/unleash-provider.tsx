"use client";

import { FlagProvider, useFlagsStatus } from "@unleash/nextjs/client";
import React from "react";
import {Loading} from '@/components/loading/loading'

interface Props {
    sessionId: string;
    children: React.ReactNode;
}

function InnerUnleashProvider({ children }: { children: React.ReactNode }) {
    const { flagsReady } = useFlagsStatus();

    if (!flagsReady) {
        return (
            <Loading/>
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
