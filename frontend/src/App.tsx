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
import { RepoDetails } from './views/RepoDetails/RepoDetails';
import { TeamDetails } from './views/TeamDetails/TeamDetails';
import { RequireAuthentication } from './utils/RequireAuthentication';
import { RepoVulnerabilities } from './views/RepoVulnerabilities/RepoVulnerabilities';
import { RepoSast } from './views/RepoSast/RepoSast';
import { TeamSast } from './views/TeamSast/TeamSast'
import { TeamVulnerabilitiesView } from './views/TeamVulnerabilities/TeamVulnerabilities';
import { VulnerabilityDetails } from './views/VulnerabilityDetails/VulnerabilityDetails';
import { SastDetails } from './views/SastDetails/SastDetails';

const router = createBrowserRouter ([
  {
    path: '/', element: <RequireAuthentication><Layout/></RequireAuthentication>, children: [
      { index: true, element: <Home/>, },  // "path" = '/' (home page)
      { path: '/profile', element: <Profile/>},
      { path: '/repos', element: <Repos/>},
      { path: '/repos/:repoId', element: <RepoDetails/>},
      { path: '/repos/:repoId/vulnerabilities/:platform', element: <RepoVulnerabilities/>},
      { path: '/repos/:repoId/sast/:platform', element: <RepoSast/>},
      { path: '/teams', element: <Teams/>},
      { path: '/teams/create', element: <CreateTeam/>},
      { path: '/teams/:teamId', element: <TeamDetails/>},
      { path: '/teams/:teamId/vulnerabilities', element: <TeamVulnerabilitiesView/>},
      { path: '/teams/:teamId/sast', element: <TeamSast/>},
      { path: '/vulnerability/:vulnerabilityId', element: <VulnerabilityDetails/>},
      { path: '/sast/:sastId', element: <SastDetails/>},
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
