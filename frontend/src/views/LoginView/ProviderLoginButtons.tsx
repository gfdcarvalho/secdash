import { Icon } from '@iconify/react'
import style from './Login.module.css'



export type Provider = "google" | "github" | "gitlab"

const providers: { id: Provider; icon: string }[] = [
    { id: "google", icon: "logos:google-icon" },
    { id: "github", icon: "logos:github-icon" },
    { id: "gitlab", icon: "logos:gitlab-icon" },
]

interface ProviderLoginButtonsProps {
    func: (provider: Provider) => void
}

export function ProviderLoginButtons( {func}: ProviderLoginButtonsProps ) {
    return (
        <div className={style.providerLoginButtons}>
            {providers.map(p => (
                <button key={p.id} onClick={() => func(p.id)}>
                    <Icon icon={p.icon} width="1.5em" />
                </button>
            ))}
        </div>
    )
}