import { RequireAuthentication } from '../../utils/RequireAuthentication'
import './Layout.css'

export function Layout() {
    return (
        <RequireAuthentication>
            <h1> Layout </h1>
        </RequireAuthentication>
    )
}