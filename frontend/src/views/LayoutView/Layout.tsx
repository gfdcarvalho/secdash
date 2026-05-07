import { Outlet } from 'react-router'
import { useAuthentication } from '../../utils/Authentication'
import { RequireAuthentication } from '../../utils/RequireAuthentication'
import style from './Layout.module.css' 
import { useTranslation } from '../../i18n/I18nProvider'
import { FlagButton } from '../../components/FlagButton'

export function Layout() {
    const [user] = useAuthentication()
    const { t } = useTranslation()
    

    return (
        <RequireAuthentication>
            <div className={style.topBar}>
                <h1> {t.layout.title} </h1>
                <div className={style.topBarRight}>
                    <h3> {user?.name} </h3>
                    <FlagButton />
                </div>
            </div>
            <Outlet />
        </RequireAuthentication>
    )
}