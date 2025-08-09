import { useFlag } from "@unleash/nextjs/client"
import { FcGoogle } from "react-icons/fc"
import { AiFillApple } from "react-icons/ai"
import { FaFacebook, FaGithub } from "react-icons/fa"
import { SiLinkedin } from "react-icons/si"

export const providersConfig = [
    {
        key: "google",
        label: "Sign in with Google",
        icon: FcGoogle,
        flag: "enable-google-login",
        displayName: "Google"
    },
    {
        key: "apple",
        label: "Sign in with Apple",
        icon: AiFillApple,
        flag: "enable-apple-login",
        displayName: "Apple"
    },
    {
        key: "github",
        label: "Sign in with GitHub",
        icon: FaGithub,
        flag: "enable-github-login",
        displayName: "GitHub"
    },
    {
        key: "facebook",
        label: "Sign in with Facebook",
        icon: FaFacebook,
        flag: "enable-facebook-login",
        displayName: "Facebook"
    },
    {
        key: "linkedin",
        label: "Sign in with LinkedIn",
        icon: SiLinkedin,
        flag: "enable-linkedin-login",
        displayName: "LinkedIn"
    },
]

export function useEnabledProviders() {
    const googleEnabled = useFlag('enable-google-login')
    const appleEnabled = useFlag('enable-apple-login')
    const githubEnabled = useFlag('enable-github-login')
    const facebookEnabled = useFlag('enable-facebook-login')
    const linkedinEnabled = useFlag('enable-linkedin-login')

    return providersConfig.filter(provider => {
        switch (provider.key) {
            case 'google': return googleEnabled
            case 'apple': return appleEnabled
            case 'github': return githubEnabled
            case 'facebook': return facebookEnabled
            case 'linkedin': return linkedinEnabled
            default: return false
        }
    })
}

export function useSocialLoginMessage(action: 'login' | 'signup' = 'login') {
    const enabledProviders = useEnabledProviders()

    if (enabledProviders.length === 0) return null

    const actionText = action === 'login' ? 'Login' : 'Sign up'

    if (enabledProviders.length <= 2) {
        const names = enabledProviders.map(p => p.displayName)
        return `${actionText} with your ${names.join(' or ')} account`
    }

    return `${actionText} with your social account`
}