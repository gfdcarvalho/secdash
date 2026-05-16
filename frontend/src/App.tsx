import { createBrowserRouter, RouterProvider } from 'react-router';
import { AuthenticationProvider } from './utils/AuthenticationProvider'
import { ThemeProvider } from './utils/ThemeProvider';
import { Home } from './views/HomeView/Home'
import { Login } from './views/LoginView/Login'
import { Layout } from './views/LayoutView/Layout'
import { Profile } from './views/ProfileView/Profile'
import { Repos } from './views/ReposView/Repos'
import { Teams } from './views/TeamsView/Teams'
import './App.css'

const router = createBrowserRouter ([
  {
    path: '/', element: <Layout/>, children: [
      { index: true, element: <Home/>, },  // "path" = '/' (home page)
      { path: '/profile', element: <Profile/>},
      { path: '/repos', element: <Repos/>},
      { path: '/teams', element: <Teams/>},
      // { path: '/repositories', element: (...) },
    ],
  },
  {
    path: '/login', element: <Login/>,
  },
])

export function App() {
    return (
    <ThemeProvider>
      <AuthenticationProvider>
        <RouterProvider router={router} />
      </AuthenticationProvider>
    </ThemeProvider>
  );
}
