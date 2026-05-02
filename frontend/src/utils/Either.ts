
export type Either<L,R> = { type: 'error', value: L } | { type: 'success', value: R };