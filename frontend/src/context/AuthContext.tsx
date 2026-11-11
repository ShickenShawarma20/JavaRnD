import React, { createContext, useContext, useState, useEffect } from 'react';
import type { UserContext } from '../types';

interface AuthContextType {
  user: UserContext | null;
  token: string | null;
  login: (userData: UserContext, accessToken: string, refreshToken: string) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserContext | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const savedToken = localStorage.getItem('eams_access_token');
    const savedUser = localStorage.getItem('eams_user');
    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch {
        localStorage.clear();
      }
    }
  }, []);

  const login = (userData: UserContext, accessToken: string, refreshToken: string) => {
    setUser(userData);
    setToken(accessToken);
    localStorage.setItem('eams_access_token', accessToken);
    localStorage.setItem('eams_refresh_token', refreshToken);
    localStorage.setItem('eams_user', JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('eams_access_token');
    localStorage.removeItem('eams_refresh_token');
    localStorage.removeItem('eams_user');
  };

  const hasRole = (role: string) => {
    return user?.roles?.includes(role) ?? false;
  };

  const hasAnyRole = (roles: string[]) => {
    return user?.roles?.some((r) => roles.includes(r)) ?? false;
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, hasRole, hasAnyRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
