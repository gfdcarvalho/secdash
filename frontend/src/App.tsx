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
import { CreateTeam } from './views/CreateTeam/CreateTeam';
import { Github } from './views/GithubView/Github';
import { Gitlab } from './views/GitlabView/Gitlab';
import { RepoDetails } from './views/RepoDetails/RepoDetails';
import { TeamDetails } from './views/TeamDetails/TeamDetails';
import { RequireAuthentication } from './utils/RequireAuthentication';
import { RepoVulnerabilities } from './views/RepoVulnerabilities/RepoVulnerabilities';

const router = createBrowserRouter ([
  {
    path: '/', element: <RequireAuthentication><Layout/></RequireAuthentication>, children: [
      { index: true, element: <Home/>, },  // "path" = '/' (home page)
      { path: '/profile', element: <Profile/>},
      { path: '/repos', element: <Repos/>},
      { path: '/repos/github', element: <Github/>},
      { path: '/repos/gitlab', element: <Gitlab/>},
      { path: '/repos/:repoId', element: <RepoDetails/>},
      { path: '/repos/:repoId/vulnerabilities', element: <RepoVulnerabilities/>},
      { path: '/teams', element: <Teams/>},
      { path: '/teams/create', element: <CreateTeam/>},
      { path: '/teams/:teamId', element: <TeamDetails/>}
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
