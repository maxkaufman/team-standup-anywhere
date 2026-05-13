import { createContext, useContext } from 'react';

export interface AuthUser {
  id: string;
  email: string;
  name: string;
  avatarUrl?: string;
  role: string;
  team?: { id: string; name: string };
}

export interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  login: (token: string, refreshToken: string, user: AuthUser) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

export const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  login: () => {},
  logout: () => {},
  isAuthenticated: false,
});

export const useAuth = () => useContext(AuthContext);
