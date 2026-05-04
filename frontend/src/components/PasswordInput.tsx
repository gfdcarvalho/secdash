import { useTranslation } from '../i18n/I18nProvider'

interface PasswordInputProps {
    className?: string
    value: string
    onChange: (value: string) => void
}

export function PasswordInput({ className, value, onChange  }: PasswordInputProps) {
    const { t } = useTranslation()
    return (
        <input
            className={className}
            type="password"
            placeholder={t.common.password}
            value={value}
            onChange={e => onChange(e.target.value)}
        
        />
    )
}
