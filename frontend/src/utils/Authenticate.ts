
// função para fazer pedido de authenticação a api

import type { Provider } from "../views/LoginView/ProviderLoginButtons";
import type { User } from "../model/user/user";
import type { UserToken } from "../model/user/userToken";
import { failure, isSuccess, success, type Either } from "./Either";
import { api, type ApiError } from "./fetchApi";

export async function authenticate(username: string, password: string): Promise<Either<ApiError, User>> {
    const response = await api.post<UserToken>("auth/login", {username, password})
    if (isSuccess(response)){
        const userResponse = await api.get<User>("users/me")
        if (isSuccess(userResponse)){
            return success(userResponse.value.data)
        }else {
            return failure(userResponse.value)
        }
    }else{
        return failure(response.value)
    }

} 


export async function registerAndAuthenticate( username: string, email: string, password: string): Promise<Either<ApiError, User>> {
    const registerResponse = await api.post<number>("users/register", {username, password, email })
    if (isSuccess(registerResponse)){
        const loginResponse = await authenticate(username, password)
        if (isSuccess(loginResponse)) {
            return success(loginResponse.value)
        }else {
            return failure(loginResponse.value)
        }
    } else {
        return failure(registerResponse.value)
    }
}

export function authenticateWithProvider(provider: Provider): void {
    window.location.href = `/api/oauth2/authorization/${provider}`
}

export function authorizeWithProvider(provider: Provider, redirectUri: string = window.location.href): void {
    const encoded = encodeURIComponent(redirectUri)
    window.location.href = `/api/oauth2/authorization/${provider}-api?redirect_uri=${encoded}`
}