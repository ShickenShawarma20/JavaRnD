import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AppLayout } from '../components/layout/AppLayout';
import { LoginPage } from '../pages/auth/LoginPage';
import { DashboardPage } from '../pages/dashboard/DashboardPage';
import { AttendanceHistoryPage } from '../pages/attendance/AttendanceHistoryPage';
import { LeavesPage } from '../pages/leave/LeavesPage';
import { ApprovalsPage } from '../pages/approvals/ApprovalsPage';
import { EmployeeListPage } from '../pages/employee/EmployeeListPage';
import { ReportsPage } from '../pages/reports/ReportsPage';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="attendance" element={<AttendanceHistoryPage />} />
        <Route path="leaves" element={<LeavesPage />} />
        <Route path="regularization" element={<ApprovalsPage />} />
        <Route path="approvals" element={<ApprovalsPage />} />
        <Route path="employees" element={<EmployeeListPage />} />
        <Route path="reports" element={<ReportsPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};
