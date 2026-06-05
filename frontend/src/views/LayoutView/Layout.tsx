import { Outlet, NavLink, useMatch } from 'react-router'
import { useAuthentication } from '../../utils/Authentication'
import { RequireAuthentication } from '../../utils/RequireAuthentication'
import style from './Layout.module.css' 
import { useTranslation } from '../../i18n/I18nProvider'
import { FlagButton } from '../../components/FlagButton'
import { ThemeButton } from '../../components/ThemeButton'
import { Icon } from '@iconify/react'

export function Layout() {
    const [user] = useAuthentication()
    const { t } = useTranslation()
    const isTeamsActive = useMatch("/teams/*")

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
                    <NavLink to="/"><Icon icon="line-md:home" width="1.5em"/>{t.layout.nav.home}</NavLink>
                    <NavLink to="/teams"><Icon icon="ri:team-line" width="1.5em"/>{t.layout.nav.teams}</NavLink>
                    <div className={style.navDropDown}>
                        { isTeamsActive &&
                            (<NavLink to="/teams/create">{t.layout.nav.createTeam} </NavLink>)
                        }
                    </div>

                    <NavLink to="/repos"><Icon icon="eos-icons:repositories" width="1.5em"/>{t.layout.nav.repos}</NavLink>

                    <NavLink to="/profile"><Icon icon="line-md:account" width="1.5em"/>{t.layout.nav.profile}</NavLink>
                </div>
                <div className={style.content}>
                    <Outlet />
                </div>
            </div>
        </RequireAuthentication>
    )
}