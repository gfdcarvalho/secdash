import { useTranslation } from '../i18n/I18nProvider'
import style from './components.module.css'

interface UsernameInputProps {
    className?: string
    value: string
    onChange: (value: string) => void
    error?: string
}

export function UsernameInput({ className, value, onChange, error }: UsernameInputProps) {
    const { t } = useTranslation()
    return (
        <div>
            <input
                className={className}
                type="text"
                placeholder={t.common.username}
                value={value}
                onChange={e => onChange(e.target.value)}
            />
            {error && <p className={style.error}>{error}</p>}
        </div>
    )
}
