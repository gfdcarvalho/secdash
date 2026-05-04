import { useTranslation } from '../i18n/I18nProvider'

interface UsernameInputProps {
    className?: string
    value: string
    onChange: (value: string) => void
}

export function UsernameInput({ className, value, onChange  }: UsernameInputProps) {
    const { t } = useTranslation()
    return (
        <input
            className={className}
            type="text"
            placeholder={t.common.username}
            value={value}
            onChange={e => onChange(e.target.value)}
        />
    )
}
