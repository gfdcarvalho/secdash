
// função para fazer pedido de authenticação a api

import type { User } from "../model/user/user";
import type { UserToken } from "../model/user/userToken";
import { failure, isSuccess, success, type Either } from "./Either";
import { api, fetchApi, type ApiError } from "./fetchApi";

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