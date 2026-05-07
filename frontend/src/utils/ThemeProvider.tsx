import { useState, useEffect, type ReactNode } from 'react'
import { ThemeContext, type Theme } from './Theme'

export function ThemeProvider({ children }: { children: ReactNode }) {
    const [theme, setThemeState] = useState<Theme>('light')

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme)
    }, [theme])

    const setTheme = (t: Theme) => setThemeState(t)

    return <ThemeContext value={{ theme, setTheme }}>{children}</ThemeContext>
}
