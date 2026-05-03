import { useContext } from 'react';
import { createContext } from 'react';
import { type User } from '../model/user/user.ts'

// isto vai ter o hook de useAuthentication

export type AuthenticationState = {
  /** read the currently authenticated user, or `undefined` if not authenticated user exists */
  user: User | undefined;
  /** set the currently authenticated user or logout (set `undefined`) */
  setUser: (user: User| undefined) => void;
};

export const AuthenticationContext = createContext<AuthenticationState>({ user: undefined, setUser: () => {} });

/**
 * Custom hook to access the authentication state
 * @returns a pair with the currently authentication user and a function to set it
 */
// Custom Hook
export function useAuthentication(): [User | undefined, (value: User | undefined) => void] {
  const state: AuthenticationState = useContext(AuthenticationContext);

  return [
    state.user,
    state.setUser
  ]
}

