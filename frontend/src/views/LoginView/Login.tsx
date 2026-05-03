import { Icon } from '@iconify/react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Locale } from '../../i18n/I18nProvider'
import { localeConfig } from '../../i18n/localeConfig'
import style from './Login.module.css'

export function Login() {
    const { t, locale, setLocale } = useTranslation()
    const languageToChange: Locale = locale === 'pt' ? 'en' : 'pt'

    return (
        <div>
            <div className={style.loginTopRow}>
                <h2> {t.login.title} </h2>
                <button className={style.langButton} onClick={() => setLocale(languageToChange)} title={localeConfig[languageToChange].label}>
                <Icon icon={localeConfig[languageToChange].icon} width="24" />
                </button>
            </div>
            
            
            
            <div className={style.loginRow}>
                <div className={style.registerCard}>
                    {t.register.register}
                    <input type="text" placeholder= {t.common.username} />
                    <input type="text" placeholder= {t.common.email}/>
                    <input type="password" placeholder= {t.common.password}/>
                </div>
                <div className={style.loginCard}>
                    {t.login.login}
                    <input type="text" placeholder= {t.common.username}/>
                    <input type="password" placeholder= {t.common.password}/>
                </div>
            </div>

        </div>
        
    ) 
}