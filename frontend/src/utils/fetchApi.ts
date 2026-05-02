import { type Either } from "./Either"

export const BASE_URL = "http://localhost:5173/api"

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'

type Request = {
    method: Method,
    uri: string,
    headers?: Record<string, string>,
    body?: object,
}

export type Response<T> = {
    status: number,
    data: T
}

export type ApiError = {
    status: number,
    errorCode: number,
    message: string
}

function isJson(contentType: string | null): boolean {
    return !!contentType && (contentType.includes('application/json') || contentType.includes('application/problem+json'))
}

export async function fetchApi<T>(request: Request): Promise<Either<ApiError, Response<T>>> {
    const response = await fetch(`${BASE_URL}${request.uri}`, {
        method: request.method,
        headers: {
            ...(request.body ? { 'Content-Type': 'application/json' } : {}),
            ...request.headers
        },
        body: request.body ? JSON.stringify(request.body) : null
    })

    const contentType = response.headers.get('Content-Type')

    if (response.ok) {
        const data = isJson(contentType) ? await response.json() as T : undefined as T
        return { type: 'success', value: { status: response.status, data } }
    } else {
        const errorData = isJson(contentType) ? await response.json() as ApiError : null
        return { type: 'error', value: {
            status: response.status,
            errorCode: errorData?.errorCode ?? response.status,
            message: errorData?.message ?? response.statusText
        }}
    }
}
