import { createContext, useContext } from 'react'

export type Theme = 'light' | 'dark'

export type ThemeState = {
    theme: Theme
    setTheme: (theme: Theme) => void
}

export const ThemeContext = createContext<ThemeState>({
    theme: 'light',
    setTheme: () => {},
})

export function useTheme(): [Theme, (theme: Theme) => void] {
    const { theme, setTheme } = useContext(ThemeContext)
    return [theme, setTheme]
}
