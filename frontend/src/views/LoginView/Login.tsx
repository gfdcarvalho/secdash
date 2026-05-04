import { Icon } from '@iconify/react'
import { useTranslation, type Translations } from '../../i18n/I18nProvider'
import { localeConfig } from '../../i18n/localeConfig'
import { UsernameInput } from '../../components/UsernameInput'
import { PasswordInput } from '../../components/PasswordInput'
import style from './Login.module.css'
import { useReducer } from 'react'
import { authenticate } from '../../utils/Authenticate'
import { isSuccess } from '../../utils/Either'
import { useAuthentication } from '../../utils/Authentication'
import { useLocation, useNavigate } from 'react-router'

type LoginFields = { username: string; password: string }
type RegisterFields = { username: string; password: string; email: string }
type LoginErrorKey = keyof Translations['login']['errors'] | ""
type LoginErrors = { username: LoginErrorKey; password: LoginErrorKey }
type RegisterErrors = { username: LoginErrorKey; email: LoginErrorKey; password: LoginErrorKey }

type State = {
    login: LoginFields
    register: RegisterFields
    loginErrors: LoginErrors
    registerErrors: RegisterErrors
    isLoading: boolean
}

type Action =
    | { type: "SET_LOGIN_FIELD"; field: keyof LoginFields; value: string }
    | { type: "SET_REGISTER_FIELD"; field: keyof RegisterFields; value: string }
    | { type: "SET_LOGIN_ERROR"; field: keyof LoginErrors; value: string }
    | { type: "SET_REGISTER_ERROR"; field: keyof RegisterErrors; value: string }
    | { type: "SET_LOADING"; value: boolean }

function setLoginField(field: keyof LoginFields, value: string): Action {
    return { type: "SET_LOGIN_FIELD", field, value }
}

function setRegisterField(field: keyof RegisterFields, value: string): Action {
    return { type: "SET_REGISTER_FIELD", field, value }
}

function setLoginError(field: keyof LoginErrors, value: string): Action {
    return { type: "SET_LOGIN_ERROR", field, value }
}

function setRegisterError(field: keyof RegisterErrors, value: string): Action {
    return { type: "SET_REGISTER_ERROR", field, value }
}

function setLoadingAction(value: boolean): Action {
    return { type: "SET_LOADING", value }
}

function reducer(state: State, action: Action): State {
    switch (action.type) {
        case "SET_LOGIN_FIELD":
            return { 
                ...state, 
                login: { ...state.login, [action.field]: action.value }, 
                loginErrors: { ...state.loginErrors, [action.field]: ""}
            }
        case "SET_REGISTER_FIELD":
            return { ...state, 
                register: { ...state.register, [action.field]: action.value }, 
                registerErrors: { ...state.registerErrors, [action.field]: ""}
            }
        case "SET_LOGIN_ERROR":
            return { ...state, loginErrors: { ...state.loginErrors, [action.field]: action.value } }
        case "SET_REGISTER_ERROR":
            return { ...state, registerErrors: { ...state.registerErrors, [action.field]: action.value } }
        case "SET_LOADING":
            return { ...state, isLoading: action.value }
    }
}

const initialState: State = {
    login: { username: "", password: "" },
    register: { username: "", password: "", email: "" },
    loginErrors: { username: "", password: "" },
    registerErrors: { username: "", password: "", email: "" },
    isLoading: false
}


export function Login() {
    const navigate = useNavigate()
    const location = useLocation()
    const [_ , setUser ] = useAuthentication()
    const [state, dispatch] = useReducer(reducer, initialState)
    const { t, locale, setLocale } = useTranslation()
    const languageToChange = localeConfig[locale].next

    const handleLogin = async () => {
        const { username, password } = state.login
        if (!username) dispatch(setLoginError("username", "blankUsername"))
        if (!password) dispatch(setLoginError("password", "blankPassword"))
        if (!username || !password) return

        dispatch(setLoadingAction(true))

        const response = await authenticate(username, password)
        if (isSuccess(response)){
            setUser(response.value)
            navigate(location.state?.source || "/", { replace: true })
        }else { // we only have one error response type for this request
            
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
                    <UsernameInput value={state.register.username} onChange={e => dispatch(setRegisterField("username", e))}/>
                    <input type="text" placeholder={t.common.email} onChange={e => dispatch(setRegisterField("email", e.target.value))}/>
                    <PasswordInput value={state.register.password} onChange={e => dispatch(setRegisterField("password", e))} />
                    <button onClick={handleRegister}> {t.register.register} </button>
                </div>
                <div className={style.loginCard}>
                    {t.login.login}
                    <UsernameInput value={state.login.username} onChange={e => dispatch(setLoginField("username", e))} error={state.loginErrors.username && t.login.errors[state.loginErrors.username]}/>
                    <PasswordInput value={state.login.password} onChange={e => dispatch(setLoginField("password", e))} error={state.loginErrors.password && t.login.errors[state.loginErrors.password]}/>
                    <button onClick={handleLogin}> {t.login.login} </button>
                </div>
            </div>

        </div>
        
    ) 
}