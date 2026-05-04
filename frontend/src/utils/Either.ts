
export type Either<L,R> = { type: 'error', value: L } | { type: 'success', value: R };

export type Success<R> = { type: 'success', value: R }
export type Failure<L> = { type: 'error', value: L }

export function isSuccess<L, R>(either: Either<L, R>): either is Success<R> {
    return either.type === 'success'
}

export function isFailure<L, R>(either: Either<L, R>): either is Failure<L> {
    return either.type === 'error'
}

// to help with the return types 
export const success = <R>(value: R): Success<R> => ({ type: 'success', value })
export const failure = <L>(value: L): Failure<L> => ({ type: 'error', value })

