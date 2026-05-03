import { createContext, useContext, useState, type ReactNode } from 'react'
import { pt } from './locales/pt'
import { en } from './locales/en'

const locales = { pt, en }
export type Locale = keyof typeof locales
export type Translations = typeof pt

type I18nContextType = {
    t: Translations
    locale: Locale
    setLocale: (locale: Locale) => void
}

const I18nContext = createContext<I18nContextType>(null!)

export function I18nProvider({ children }: { children: ReactNode }) {
    const [locale, setLocale] = useState<Locale>('pt')
    return (
        <I18nContext.Provider value={{ t: locales[locale], locale, setLocale }}>
            {children}
        </I18nContext.Provider>
    )
}

export function useTranslation() {
    return useContext(I18nContext)
}
