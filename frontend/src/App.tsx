import { createBrowserRouter, RouterProvider } from 'react-router';
import { AuthenticationProvider } from './utils/AuthenticationProvider'
import { ThemeProvider } from './utils/ThemeProvider';
import { Home } from './views/HomeView/Home'
import { Login } from './views/LoginView/Login'
import { Layout } from './views/LayoutView/Layout'
import './App.css'

const router = createBrowserRouter ([
  {
    path: '/', element: <Layout/>, children: [
      { index: true, element: <Home/>, },  // "path" = '/' (home page)
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
