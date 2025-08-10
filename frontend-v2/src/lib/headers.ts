'use client';

import { useEffect, useState } from 'react';
import {useFingerprint} from "@/components/providers/fingerprint-provider";

export function useTimezoneHeader() {
    const [tz, setTz] = useState<string>('');
    useEffect(() => {
        try {
            const resolved = Intl.DateTimeFormat().resolvedOptions().timeZone;
            setTz(resolved || 'UTC');
        } catch {
            setTz('UTC');
        }
    }, []);
    return tz;
}

export function useFingerprintHeader() {
    const {visitorId} = useFingerprint();
    return visitorId;
}
