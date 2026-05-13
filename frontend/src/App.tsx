import { useState, useCallback, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import { ApolloProvider, useQuery } from '@apollo/client/react';
import { client } from './apollo/client';
import { AuthContext } from './hooks/useAuth';
import type { AuthUser } from './hooks/useAuth';
import ProtectedRoute from './components/ProtectedRoute';
import { ME_QUERY } from './graphql/queries';

import Login from './pages/Login';
import SignUp from './pages/SignUp';
import Dashboard from './pages/Dashboard';
import SubmitStandup from './pages/SubmitStandup';
import StandupHistory from './pages/StandupHistory';
import TeamManagement from './pages/TeamManagement';
import Profile from './pages/Profile';

function AppContent() {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  // Always fetch fresh user data from the server when we have a token.
  // This ensures team membership and other changes made after login are reflected.
  const { data: meData } = useQuery(ME_QUERY, {
    skip: !token,
    fetchPolicy: 'network-only',
  });

  useEffect(() => {
    const me = (meData as { me?: AuthUser } | undefined)?.me;
    if (me) {
      setUser(me);
      localStorage.setItem('user', JSON.stringify(me));
    }
  }, [meData]);

  const login = useCallback((newToken: string, refreshToken: string, newUser: AuthUser) => {
    localStorage.setItem('token', newToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
    client.resetStore();
  }, []);

  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAuthenticated }}>
      <BrowserRouter>
        <div className="min-h-screen bg-gray-50">
          {isAuthenticated && (
            <nav className="bg-white border-b border-gray-200 px-6 py-3">
              <div className="max-w-6xl mx-auto flex items-center justify-between">
                <div className="flex items-center gap-6">
                  <Link to="/dashboard" className="text-lg font-bold text-indigo-600">
                    TeamPulse
                  </Link>
                  <Link to="/dashboard" className="text-sm text-gray-600 hover:text-gray-900">
                    Dashboard
                  </Link>
                  <Link to="/standup" className="text-sm text-gray-600 hover:text-gray-900">
                    Standup
                  </Link>
                  <Link to="/history" className="text-sm text-gray-600 hover:text-gray-900">
                    History
                  </Link>
                  <Link to="/team" className="text-sm text-gray-600 hover:text-gray-900">
                    Team
                  </Link>
                </div>
                <div className="flex items-center gap-4">
                  <Link to="/profile" className="text-sm text-gray-600 hover:text-gray-900">
                    {user?.name}
                  </Link>
                  <button onClick={logout} className="text-sm text-red-500 hover:text-red-700">
                    Logout
                  </button>
                </div>
              </div>
            </nav>
          )}

          <div className="max-w-6xl mx-auto">
            <Routes>
              <Route path="/login" element={isAuthenticated ? <Navigate to="/dashboard" /> : <Login />} />
              <Route path="/signup" element={isAuthenticated ? <Navigate to="/dashboard" /> : <SignUp />} />
              <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
              <Route path="/standup" element={<ProtectedRoute><SubmitStandup /></ProtectedRoute>} />
              <Route path="/history" element={<ProtectedRoute><StandupHistory /></ProtectedRoute>} />
              <Route path="/team" element={<ProtectedRoute><TeamManagement /></ProtectedRoute>} />
              <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
              <Route path="*" element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'} />} />
            </Routes>
          </div>
        </div>
      </BrowserRouter>
    </AuthContext.Provider>
  );
}

export default function App() {
  return (
    <ApolloProvider client={client}>
      <AppContent />
    </ApolloProvider>
  );
}
