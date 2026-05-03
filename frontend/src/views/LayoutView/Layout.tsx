import { useAuthentication } from '../../utils/Authentication'
import { RequireAuthentication } from '../../utils/RequireAuthentication'
import './Layout.css'

export function Layout() {
    const [user] = useAuthentication()
    return (
        <RequireAuthentication>
            <h1> Layout {user?.name} </h1>
        </RequireAuthentication>
    )
}