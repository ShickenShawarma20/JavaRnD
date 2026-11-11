import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  CalendarCheck,
  CalendarDays,
  FileCheck2,
  FileSpreadsheet,
  LogOut,
  Building2,
  UserCheck,
  CalendarClock,
  ShieldCheck,
  Bell,
  Search,
  HelpCircle,
  ChevronDown
} from 'lucide-react';

export const AppLayout: React.FC = () => {
  const { user, logout, hasAnyRole } = useAuth();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isHrOrAdmin = hasAnyRole(['ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN']);
  const isManager = hasAnyRole(['ROLE_MANAGER']);

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden font-sans text-slate-900 antialiased">
      {/* Dense Enterprise Left Navigation Shell */}
      <aside className="w-60 bg-slate-950 border-r border-slate-800 flex flex-col justify-between shrink-0">
        <div className="overflow-y-auto">
          {/* Brand Header */}
          <div className="h-14 flex items-center gap-2.5 px-4 border-b border-slate-850 bg-slate-950/80 sticky top-0 z-10">
            <div className="w-7 h-7 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-xs shadow-xs">
              JW
            </div>
            <div className="truncate">
              <h1 className="text-xs font-bold tracking-tight text-white uppercase">JAVA WORKFORCE</h1>
              <p className="text-[10px] text-slate-400 font-mono">JAVA R&D PVT. LTD.</p>
            </div>
          </div>

          {/* Navigation Sections */}
          <nav className="p-3 space-y-4">
            {/* Core Section */}
            <div>
              <p className="px-2 pb-1 text-[10px] font-semibold tracking-wider uppercase text-slate-400">
                Core Workspace
              </p>
              <div className="space-y-0.5">
                <NavLink
                  to="/dashboard"
                  className={({ isActive }) =>
                    `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition ${
                      isActive
                        ? 'bg-indigo-600 text-white font-semibold'
                        : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                    }`
                  }
                >
                  <LayoutDashboard className="w-3.5 h-3.5" />
                  Overview
                </NavLink>

                <NavLink
                  to="/attendance"
                  className={({ isActive }) =>
                    `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition ${
                      isActive
                        ? 'bg-indigo-600 text-white font-semibold'
                        : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                    }`
                  }
                >
                  <CalendarCheck className="w-3.5 h-3.5" />
                  My Attendance
                </NavLink>

                <NavLink
                  to="/leaves"
                  className={({ isActive }) =>
                    `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition ${
                      isActive
                        ? 'bg-indigo-600 text-white font-semibold'
                        : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                    }`
                  }
                >
                  <CalendarDays className="w-3.5 h-3.5" />
                  Leaves & Balances
                </NavLink>
              </div>
            </div>

            {/* Management Section */}
            {(isHrOrAdmin || isManager) && (
              <div>
                <p className="px-2 pb-1 text-[10px] font-semibold tracking-wider uppercase text-slate-400">
                  Operations & People
                </p>
                <div className="space-y-0.5">
                  <NavLink
                    to="/approvals"
                    className={({ isActive }) =>
                      `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition ${
                        isActive
                          ? 'bg-indigo-600 text-white font-semibold'
                          : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                      }`
                    }
                  >
                    <UserCheck className="w-3.5 h-3.5" />
                    Unified Approvals
                  </NavLink>

                  {isHrOrAdmin && (
                    <NavLink
                      to="/employees"
                      className={({ isActive }) =>
                        `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition ${
                          isActive
                            ? 'bg-indigo-600 text-white font-semibold'
                            : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                        }`
                      }
                    >
                      <Users className="w-3.5 h-3.5" />
                      Workforce Lifecycle
                    </NavLink>
                  )}

                  <NavLink
                    to="/regularization"
                    className={({ isActive }) =>
                      `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition ${
                        isActive
                          ? 'bg-indigo-600 text-white font-semibold'
                          : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                      }`
                    }
                  >
                    <FileCheck2 className="w-3.5 h-3.5" />
                    Attendance Exceptions
                  </NavLink>
                </div>
              </div>
            )}

            {/* Governance & Reporting */}
            {isHrOrAdmin && (
              <div>
                <p className="px-2 pb-1 text-[10px] font-semibold tracking-wider uppercase text-slate-400">
                  Audit & Analytics
                </p>
                <div className="space-y-0.5">
                  <NavLink
                    to="/reports"
                    className={({ isActive }) =>
                      `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition ${
                        isActive
                          ? 'bg-indigo-600 text-white font-semibold'
                          : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                      }`
                    }
                  >
                    <FileSpreadsheet className="w-3.5 h-3.5" />
                    Payroll & Reports
                  </NavLink>
                </div>
              </div>
            )}
          </nav>
        </div>

        {/* Sidebar Footer User Card */}
        <div className="p-3 border-t border-slate-850 bg-slate-950">
          <div className="flex items-center justify-between">
            <div className="truncate pr-2">
              <p className="text-xs font-semibold text-white truncate">{user?.fullName}</p>
              <p className="text-[10px] text-slate-400 font-mono truncate">
                {user?.roles?.[0]?.replace('ROLE_', '')}
              </p>
            </div>
            <button
              onClick={handleLogout}
              title="Sign Out"
              className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-900 rounded transition"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Application Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Header Bar */}
        <header className="h-14 bg-white border-b border-slate-200 flex items-center justify-between px-6 shrink-0">
          <div className="flex items-center gap-4">
            <div className="relative">
              <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2.5" />
              <input
                type="text"
                placeholder="Global Search (Ctrl+K)..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-indigo-500 w-64 text-slate-700"
              />
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button className="p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition relative">
              <Bell className="w-4 h-4" />
              <span className="w-2 h-2 rounded-full bg-indigo-600 absolute top-1.5 right-1.5" />
            </button>

            <div className="h-4 w-[1px] bg-slate-200" />

            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-full bg-slate-800 text-white flex items-center justify-center text-xs font-semibold">
                {user?.fullName?.charAt(0) || 'U'}
              </div>
              <span className="text-xs font-semibold text-slate-700 hidden sm:inline-block">
                {user?.fullName}
              </span>
            </div>
          </div>
        </header>

        {/* Scrollable Work Surface */}
        <main className="flex-1 overflow-y-auto p-6 bg-slate-50/60">
          <div className="max-w-7xl mx-auto space-y-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
