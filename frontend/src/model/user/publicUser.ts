import type { AppRole } from './user'

export type PublicUser = {
    uid: number
    name: string
    email: string
    role: AppRole
}
