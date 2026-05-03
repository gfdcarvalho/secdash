import type { Locale } from './I18nProvider'

export const localeConfig: Record<Locale, { icon: string; label: string }> = {
    pt: { icon: 'circle-flags:pt', label: 'Português' },
    en: { icon: 'circle-flags:gb', label: 'English' },
}
