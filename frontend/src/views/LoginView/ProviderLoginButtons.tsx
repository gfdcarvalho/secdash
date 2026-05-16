import { Icon } from '@iconify/react'
import style from './Login.module.css'



export type Provider = "google" | "github" | "gitlab"

const providers: { id: Provider; icon: string }[] = [
    { id: "google", icon: "logos:google-icon" },
    { id: "github", icon: "logos:github-icon" },
    { id: "gitlab", icon: "logos:gitlab-icon" },
]

interface ProviderLoginButtonsProps {
    className?: string
    func: (provider: Provider) => void
    isLoading?: boolean
}

export function ProviderLoginButtons( {className, func, isLoading}: ProviderLoginButtonsProps ) {
    return (
        <div className={style.providerLoginButtons}>
            {providers.map(p => (
                <button className={className}
                    key={p.id} onClick={() => func(p.id)} disabled={isLoading}>
                    <Icon icon={p.icon} width="1.5em" className={p.id === "github" ? style.githubIcon : undefined} />
                </button>
            ))}
        </div>
    )
}