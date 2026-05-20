import { useTranslation, type Translations } from '../../i18n/I18nProvider'
import { UsernameInput } from '../../components/UsernameInput'
import { PasswordInput } from '../../components/PasswordInput'
import { EmailInput } from '../../components/EmailInput'
import { ProviderLoginButtons, type Provider } from './ProviderLoginButtons'
import { FlagButton } from '../../components/FlagButton'
import { ThemeButton } from '../../components/ThemeButton'
import style from './Login.module.css'
import { useReducer } from 'react'
import { authenticate, authenticateWithProvider, registerAndAuthenticate } from '../../utils/Authenticate'
import { isSuccess } from '../../utils/Either'
import { useAuthentication } from '../../utils/Authentication'
import { useLocation, useNavigate } from 'react-router'

type LoginFields = { username: string; password: string }
type RegisterFields = { username: string; password: string; email: string }
type LoginErrorKey = keyof Translations['login']['errors'] | ""
type RegisterErrorKey = keyof Translations['register']['errors'] | ""
type LoginErrors = { username: LoginErrorKey; password: LoginErrorKey }
type RegisterErrors = { username: RegisterErrorKey; email: RegisterErrorKey; password: RegisterErrorKey }

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
    const { t } = useTranslation()

    const handleLogin = async () => {
        const { username, password } = state.login
        if (!username) dispatch(setLoginError("username", "blankUsername"))
        if (!password) dispatch(setLoginError("password", "blankPassword"))
        if (!username || !password) return

        dispatch(setLoadingAction(true)) // we will use this to disable the input buttons 

        const response = await authenticate(username, password)
        if (isSuccess(response)){
            setUser(response.value)
            navigate(location.state?.source || "/", { replace: true })
        }else { // we only have one error response type for this request
            dispatch(setLoadingAction(false))
        
        }
    }

    const handleRegister = async () => {
        const { username, password, email } = state.register
        if (!username) dispatch(setRegisterError("username", "blankUsername"))
        if (!email) dispatch(setRegisterError("email", "blankEmail"))
        if (!password) dispatch(setRegisterError("password", "blankPassword"))
        if (!username || !email || !password) return

        dispatch(setLoadingAction(true)) // we will use this to disable the input buttons 

        const response = await registerAndAuthenticate(username, email, password)
        if (isSuccess(response)){
            setUser(response.value)
            navigate(location.state?.source || "/", { replace: true })
        } else {
            dispatch(setLoadingAction(false))
            // handle register erros ...
        }

    }

    const handleProviderLogin = (provider: Provider) => {
        dispatch(setLoadingAction(true))
        authenticateWithProvider(provider)
    }

    return (
        <div className={style.loginContainer}>
            <div className={style.loginTopRow}>
                <h1> {t.login.title} </h1>
                <div className={style.loginTopRowRight}>
                    <ThemeButton />
                    <FlagButton />
                </div>
            </div>
            <div className={style.loginRow}>
                <div className={style.registerCard}>
                    {t.register.register}
                    <ProviderLoginButtons func={handleProviderLogin} isLoading={state.isLoading}/>
                    <UsernameInput className="inputText" value={state.register.username} onChange={e => dispatch(setRegisterField("username", e))} error={state.registerErrors.username && t.register.errors[state.registerErrors.username]}/>
                    <EmailInput className="inputText" value={state.register.email} onChange={e => dispatch(setRegisterField("email", e))} error={state.registerErrors.email && t.register.errors[state.registerErrors.email]}/>
                    <PasswordInput className="inputText" value={state.register.password} onChange={e => dispatch(setRegisterField("password", e))} error={state.registerErrors.password && t.register.errors[state.registerErrors.password]}/>
                    <button onClick={handleRegister} disabled={state.isLoading}> {t.register.register} </button>
                </div>
                <div className={style.loginCard}>
                    {t.login.login}
                    <ProviderLoginButtons func={handleProviderLogin} isLoading={state.isLoading} />
                    <UsernameInput className="inputText" value={state.login.username} onChange={e => dispatch(setLoginField("username", e))} error={state.loginErrors.username && t.login.errors[state.loginErrors.username]}/>
                    <PasswordInput className="inputText" value={state.login.password} onChange={e => dispatch(setLoginField("password", e))} error={state.loginErrors.password && t.login.errors[state.loginErrors.password]}/>
                    <button onClick={handleLogin} disabled={state.isLoading}> {t.login.login} </button>
                </div>
            </div>

        </div>
        
    ) 
}
