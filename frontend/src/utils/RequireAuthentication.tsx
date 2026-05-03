import type { ReactNode } from "react";
import { useAuthentication } from "./Authentication";
import { Navigate, useLocation } from "react-router";

type RequireAuthenticationProps = {
  children: ReactNode;
};
export function RequireAuthentication({
  children,
}: RequireAuthenticationProps) {
  const [user] = useAuthentication();
  const location = useLocation();
  if (user) {
    return children;
  } else {
    return (
      <Navigate
        to="/login"
        state={{ source: location.pathname }}
        replace={true}
      />
    );
  }
}
