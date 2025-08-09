"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import FingerprintJS, { Agent } from "@fingerprintjs/fingerprintjs"; // 👈 import Agent type

interface FingerprintContextType {
    visitorId: string | null;
    isFingerprintReady: boolean;
}

const FingerprintContext = createContext<FingerprintContextType>({
    visitorId: null,
    isFingerprintReady: false,
});

let fpPromise: Promise<Agent> | null = null;
const loadFingerprintAgent = () => {
    if (!fpPromise) {
        fpPromise = FingerprintJS.load();
    }
    return fpPromise;
};

export function FingerprintProvider({ children }: { children: React.ReactNode }) {
    const [visitorId, setVisitorId] = useState<string | null>(null);
    const [isFingerprintReady, setIsFingerprintReady] = useState(false);

    useEffect(() => {
        (async () => {
            try {
                const fp = await loadFingerprintAgent();
                const result = await fp.get();
                setVisitorId(result.visitorId);
            } catch (err) {
                console.error("FingerprintJS failed:", err);
            } finally {
                setIsFingerprintReady(true);
            }
        })();
    }, []);

    return (
        <FingerprintContext.Provider value={{ visitorId, isFingerprintReady }}>
            {children}
        </FingerprintContext.Provider>
    );
}

export function useFingerprint() {
    return useContext(FingerprintContext);
}
