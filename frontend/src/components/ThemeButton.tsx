import { Icon } from '@iconify/react'
import { useTheme } from '../utils/Theme'
import style from './components.module.css'

export function ThemeButton() {
    const [theme, setTheme] = useTheme()
    return (
        <button
            className={style.flagButton}
            onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}
            title={theme === 'light' ? 'Dark mode' : 'Light mode'}>
            <Icon icon={theme === 'light' ? 'fluent-emoji-flat:waxing-crescent-moon' : 'fluent-emoji-flat:sun'} width="2em" />
        </button>
    )
}
