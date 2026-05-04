import { useTranslation } from '../i18n/I18nProvider'
import style from './components.module.css'

interface EmailInputProps {
    className?: string
    value: string
    onChange: (value: string) => void
    error?: string
}

export function EmailInput({ className, value, onChange, error }: EmailInputProps) {
    const { t } = useTranslation()
    return (
        <div>
            <input
                className={className}
                type="email"
                placeholder={t.common.email}
                value={value}
                onChange={e => onChange(e.target.value)}
            />
            {error && <p className={style.error}>{error}</p>}
        </div>
    )
}
