import { Icon } from '@iconify/react'
import { useTranslation } from '../../i18n/I18nProvider'
import type { Locale } from '../../i18n/I18nProvider'
import { localeConfig } from '../../i18n/localeConfig'
import { UsernameInput } from '../../components/UsernameInput'
import { PasswordInput } from '../../components/PasswordInput'
import style from './Login.module.css'
import { useReducer } from 'react'
import { authenticate } from '../../utils/Authenticate'
import { isSuccess } from '../../utils/Either'
import { useAuthentication } from '../../utils/Authentication'
import { useLocation, useNavigate } from 'react-router'

type State = {
    fields: { username: string; password: string, email: string }
    isLoading: boolean
}

type Action = 
    | { type: "SET_FIELD"; field: "username" | "password" | "email"; value: string }
    | { type: "SET_LOADING"; value: boolean }

function setUsernameAction(username: string): Action {
    return { type: "SET_FIELD", field: "username", value: username}
}
function setPasswordAction(password: string): Action {
    return { type: "SET_FIELD", field: "password", value: password} 
}
function setEmailAction(email: string): Action {
    return { type: "SET_FIELD", field: "email", value: email}
}
function setLoadingAction(value: boolean): Action {
    return { type: "SET_LOADING", value: value}
}

function reducer(state: State, action: Action): State {
    switch (action.type) {
        case "SET_FIELD":
            return {
                ...state,
                fields: {...state.fields, [action.field]: action.value }
            }
        case "SET_LOADING":
            return { ...state, isLoading: action.value}
    }
}

const initialState: State = {
    fields: { username: "", password: "", email: ""},
    isLoading: false
}


export function Login() {
    const navigate = useNavigate()
    const location = useLocation()
    const [_ , setUser ] = useAuthentication()
    const [state, dispatch] = useReducer(reducer, initialState)
    const { t, locale, setLocale } = useTranslation()
    const languageToChange: Locale = locale === 'pt' ? 'en' : 'pt'

    const handleLogin = async () => {
        const { username , password } = state.fields
        if ( !username || !password ) return 

        dispatch(setLoadingAction(true))

        const response = await authenticate(username, password)
        if (isSuccess(response)){
            setUser(response.value)
            navigate(location.state?.source || "/", { replace: true });
        }
    }

    const handleRegister = async () => {

    }

    return (
        <div>
            <div className={style.loginTopRow}>
                <h2> {t.login.title} </h2>
                <button 
                    className={style.langButton} 
                    onClick={() => setLocale(languageToChange)} 
                    title={localeConfig[languageToChange].label}>
                    <Icon icon={localeConfig[languageToChange].icon} width="24" />
                </button>
            </div>
            
            
            
            <div className={style.loginRow}>
                <div className={style.registerCard}>
                    {t.register.register}
                    <UsernameInput value={state.fields.username} onChange={v => dispatch(setUsernameAction(v))} />
                    <input type="text" placeholder={t.common.email} onChange={e => dispatch(setEmailAction(e.target.value))}/>
                    <PasswordInput value={state.fields.password} onChange={v => dispatch(setPasswordAction(v))} />
                    <button
                        onClick = {handleRegister}
                    > {t.register.register} </button>
                </div>
                <div className={style.loginCard}>
                    {t.login.login}
                    <UsernameInput value={state.fields.username} onChange={v => dispatch(setUsernameAction(v))} />
                    <PasswordInput value={state.fields.password} onChange={v => dispatch(setPasswordAction(v))} />
                    <button
                        onClick = {handleLogin}
                    > {t.login.login} </button>
                </div>
            </div>

        </div>
        
    ) 
}