import type { Locale } from './I18nProvider'

export const localeConfig: Record<Locale, { icon: string; label: string; next: Locale }> = {
    pt: { icon: 'circle-flags:pt', label: 'Português', next: 'en' },
    en: { icon: 'circle-flags:gb', label: 'English', next: 'pt' },
}
