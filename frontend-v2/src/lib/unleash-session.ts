import { cookies } from "next/headers";

const COOKIE_NAME = "unleash-session-id";

export async function getUnleashSessionId(): Promise<string> {
    const cookieStore = await cookies();
    return cookieStore.get(COOKIE_NAME)?.value ?? crypto.randomUUID();
}