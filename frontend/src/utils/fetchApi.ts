import { type Either } from "./Either"

export const BASE_URL = "/api"

type Method = 'GET' | 'POST' | 'PATCH' | 'DELETE'

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
    type: string,
    detail?: string
}

function isJson(contentType: string | null): boolean {
    return !!contentType && (contentType.includes('application/json') || contentType.includes('application/problem+json'))
}

export async function fetchApi<T>(request: Request): Promise<Either<ApiError, Response<T>>> {
    const response = await fetch(`${BASE_URL}${request.uri}`, {
        method: request.method,
        credentials: 'include',
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
            type: errorData?.type ?? response.statusText,
            detail: errorData?.detail
        }}
    }
}

export const api = {
    get: <T>(uri: string, headers?: Record<string, string>) =>
        fetchApi<T>({ method: 'GET', uri, headers }),

    post: <T>(uri: string, body?: object, headers?: Record<string, string>) =>
        fetchApi<T>({ method: 'POST', uri, body, headers }),

    patch: <T>(uri: string, body?: object, headers?: Record<string, string>) =>
        fetchApi<T>({ method: 'PATCH', uri, body, headers }),

    delete: <T>(uri: string, headers?: Record<string, string>) =>
        fetchApi<T>({ method: 'DELETE', uri, headers }),
}
