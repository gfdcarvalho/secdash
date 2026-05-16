import { Outlet, NavLink } from 'react-router'
import { useAuthentication } from '../../utils/Authentication'
import { RequireAuthentication } from '../../utils/RequireAuthentication'
import style from './Layout.module.css' 
import { useTranslation } from '../../i18n/I18nProvider'
import { FlagButton } from '../../components/FlagButton'
import { ThemeButton } from '../../components/ThemeButton'

export function Layout() {
    const [user] = useAuthentication()
    const { t } = useTranslation()
    

    return (
        <RequireAuthentication>
            <div className={style.topBar}>
                <NavLink to="/" className={style.titleLink}>{t.layout.title}</NavLink>
                <div className={style.topBarRight}>
                    <NavLink to="/profile" className={style.usernameLink}> {user?.name} </NavLink>
                    <ThemeButton />
                    <FlagButton />
                </div>
            </div>
            <div className={style.body}>
                <div className={style.sideBar}>
                    <NavLink to="/">Home</NavLink>
                    <NavLink to="/teams">Teams</NavLink>
                    <NavLink to="/repos">Repos</NavLink>
                    <NavLink to="/profile">Profile</NavLink>
                </div>
                <div className={style.content}>
                    <Outlet />
                </div>
            </div>
        </RequireAuthentication>
    )
}