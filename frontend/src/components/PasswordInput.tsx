import { useTranslation } from '../i18n/I18nProvider'
import style from './Input.module.css'

interface PasswordInputProps {
    className?: string
    value: string
    onChange: (value: string) => void
    error?: string
}

export function PasswordInput({ className, value, onChange, error }: PasswordInputProps) {
    const { t } = useTranslation()
    return (
        <div>
            <input
                className={className}
                type="password"
                placeholder={t.common.password}
                value={value}
                onChange={e => onChange(e.target.value)}
            />
            {error && <p className={style.error}>{error}</p>}
        </div>
    )
}
