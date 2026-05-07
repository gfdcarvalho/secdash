import { Icon } from '@iconify/react'
import { useTranslation } from '../i18n/I18nProvider'
import { localeConfig } from '../i18n/localeConfig'
import style from './components.module.css'

export function FlagButton() {
    const { locale, setLocale } = useTranslation()
    const next = localeConfig[locale].next
    return (
        <button
            className={style.flagButton}
            onClick={() => setLocale(next)}
            title={localeConfig[next].label}>
            <Icon icon={localeConfig[next].icon} width="2em" />
        </button>
    )
}
