import { useState, useEffect, type ReactNode } from "react";
import { AuthenticationContext, type AuthenticationState} from "./Authentication";
import { type User } from '../model/user/user'
import { fetchApi } from "./fetchApi";

type AuthenticationProviderProp = {
  children: ReactNode,
}
export function AuthenticationProvider({ children }: AuthenticationProviderProp) {
  const [observedUser, setUser] = useState<User | undefined>(undefined);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchApi<User>({ uri: "/users/me", method: "GET" }).then(response => {
      if (response.type === "success") setUser(response.value.data);
      setLoading(false);
    });
  }, []);

  if (loading) return null;

  // console.log(`provider ${observedUser}`);
  const value: AuthenticationState  = {
    user: observedUser,
    setUser: (user) => setUser(user),
  };
  return <AuthenticationContext value={value}>{children}</AuthenticationContext>;
}
