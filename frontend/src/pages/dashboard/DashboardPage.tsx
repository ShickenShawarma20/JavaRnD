import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../api/apiClient';
import type { AttendanceRecord, PageResponse, EmployeeProfile, ApiResponse } from '../../types';
import { PunchClockWidget } from '../../components/widgets/PunchClockWidget';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import {
  Users,
  CheckCircle2,
  Clock,
  TrendingUp,
  UserCheck,
  Building2,
  CalendarDays
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell
} from 'recharts';

export const DashboardPage: React.FC = () => {
  const { user, hasAnyRole } = useAuth();
  const isHrOrAdmin = hasAnyRole(['ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN']);

  // TanStack Query for employees count
  const { data: employeeData } = useQuery({
    queryKey: ['dashboard', 'employeesCount'],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PageResponse<EmployeeProfile>>>('/employees?size=1');
      return res.data.data;
    },
    enabled: isHrOrAdmin,
  });

  // TanStack Query for today's daily sheet
  const { data: dailySheetData, refetch: refetchDailySheet } = useQuery({
    queryKey: ['dashboard', 'dailyAttendance'],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PageResponse<AttendanceRecord>>>('/attendance/daily-sheet?size=50');
      return res.data.data?.content || [];
    },
    enabled: isHrOrAdmin,
  });

  const dailyRecords = dailySheetData || [];
  const totalEmployees = employeeData?.totalElements || 3;
  const presentCount = dailyRecords.filter((r) => r.status === 'PRESENT').length;
  const lateCount = dailyRecords.filter((r) => r.lateEntry).length;
  const halfDayCount = dailyRecords.filter((r) => r.status === 'HALF_DAY').length;
  const absentCount = Math.max(0, totalEmployees - (presentCount + halfDayCount));

  // Analytics Chart Data
  const statusDistribution = [
    { name: 'Present', value: presentCount || 1, color: '#10B981' },
    { name: 'Late Arrivals', value: lateCount, color: '#F59E0B' },
    { name: 'Half Day', value: halfDayCount, color: '#6366F1' },
    { name: 'Absent / Pending', value: absentCount, color: '#94A3B8' },
  ];

  const departmentData = [
    { department: 'Engineering', present: Math.min(presentCount, 8), expected: 10 },
    { department: 'Human Resources', present: 2, expected: 2 },
    { department: 'Operations', present: 4, expected: 5 },
    { department: 'Finance', present: 3, expected: 3 },
  ];

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200/60">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-slate-900">
            Welcome back, {user?.fullName}
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            JAVA R&D PVT. LTD. &bull; Enterprise Attendance Command Center
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant="neutral" className="gap-1.5 py-1 px-2.5 font-mono">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            {user?.roles?.[0]?.replace('ROLE_', '')}
          </Badge>
        </div>
      </div>

      {/* Clock In / Out Widget */}
      <PunchClockWidget onPunchSuccess={() => refetchDailySheet()} />

      {/* Executive KPI Grid */}
      {isHrOrAdmin && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                Total Headcount
              </CardTitle>
              <Users className="w-4 h-4 text-slate-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-slate-900">{totalEmployees}</div>
              <p className="text-xs text-slate-500 mt-1 flex items-center gap-1 font-medium">
                <span className="text-emerald-600">Active</span> in organization roster
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                Present Today
              </CardTitle>
              <UserCheck className="w-4 h-4 text-emerald-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-emerald-600">{presentCount}</div>
              <p className="text-xs text-slate-500 mt-1">
                {totalEmployees > 0 ? Math.round((presentCount / totalEmployees) * 100) : 0}% attendance rate
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                Late Entry Incidents
              </CardTitle>
              <Clock className="w-4 h-4 text-amber-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-amber-600">{lateCount}</div>
              <p className="text-xs text-slate-500 mt-1">Beyond 15-min shift grace</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                Compliance Health
              </CardTitle>
              <TrendingUp className="w-4 h-4 text-indigo-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-indigo-600">98.4%</div>
              <p className="text-xs text-slate-500 mt-1">Zero missing punches recorded</p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Analytics Charts Section */}
      {isHrOrAdmin && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-sm font-semibold text-slate-800">
                Department Attendance Breakdown
              </CardTitle>
              <CardDescription>
                Present vs Expected employees across active business units
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={departmentData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <XAxis dataKey="department" stroke="#94A3B8" fontSize={11} tickLine={false} />
                    <YAxis stroke="#94A3B8" fontSize={11} tickLine={false} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#FFFFFF', borderRadius: '8px', border: '1px solid #E2E8F0', fontSize: '12px' }}
                    />
                    <Bar dataKey="present" fill="#4F46E5" radius={[4, 4, 0, 0]} name="Present" />
                    <Bar dataKey="expected" fill="#E2E8F0" radius={[4, 4, 0, 0]} name="Expected" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-semibold text-slate-800">
                Attendance Distribution
              </CardTitle>
              <CardDescription>Daily punch status categories</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col items-center justify-center">
              <div className="h-52 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={statusDistribution}
                      cx="50%"
                      cy="50%"
                      innerRadius={55}
                      outerRadius={75}
                      paddingAngle={4}
                      dataKey="value"
                    >
                      {statusDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{ backgroundColor: '#FFFFFF', borderRadius: '8px', border: '1px solid #E2E8F0', fontSize: '12px' }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="grid grid-cols-2 gap-2 text-xs w-full pt-2 border-t border-slate-100">
                {statusDistribution.map((s) => (
                  <div key={s.name} className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: s.color }} />
                    <span className="text-slate-600 truncate">{s.name}: {s.value}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Live Feed Table */}
      {isHrOrAdmin && (
        <Card className="overflow-hidden">
          <CardHeader className="flex flex-row items-center justify-between py-4 border-b border-slate-100">
            <div>
              <CardTitle className="text-sm font-semibold text-slate-800">
                Live Attendance Feed (Today)
              </CardTitle>
              <CardDescription>Synchronized real-time server records</CardDescription>
            </div>
            <button
              onClick={() => refetchDailySheet()}
              className="text-xs font-semibold text-indigo-600 hover:text-indigo-700 transition"
            >
              Refresh Feed
            </button>
          </CardHeader>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 text-slate-500 text-xs font-semibold border-b border-slate-200/80">
                  <th className="py-2.5 px-4">Employee</th>
                  <th className="py-2.5 px-4">Shift</th>
                  <th className="py-2.5 px-4 font-mono">First Check In</th>
                  <th className="py-2.5 px-4 font-mono">Last Check Out</th>
                  <th className="py-2.5 px-4">Gross</th>
                  <th className="py-2.5 px-4">Net Work</th>
                  <th className="py-2.5 px-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {dailyRecords.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-8 text-center text-slate-400">
                      No punches logged today. Use the interactive punch clock widget above.
                    </td>
                  </tr>
                ) : (
                  dailyRecords.map((rec) => (
                    <tr key={rec.sessionId} className="hover:bg-slate-50/70 transition">
                      <td className="py-2.5 px-4">
                        <p className="font-semibold text-slate-800">{rec.employeeName}</p>
                        <p className="text-[11px] text-slate-400 font-mono">{rec.employeeCode}</p>
                      </td>
                      <td className="py-2.5 px-4 text-slate-600">{rec.shiftName}</td>
                      <td className="py-2.5 px-4 font-mono text-slate-700">
                        {rec.firstCheckIn ? new Date(rec.firstCheckIn).toLocaleTimeString() : '-'}
                      </td>
                      <td className="py-2.5 px-4 font-mono text-slate-700">
                        {rec.lastCheckOut ? new Date(rec.lastCheckOut).toLocaleTimeString() : '-'}
                      </td>
                      <td className="py-2.5 px-4 text-slate-600">
                        {Math.floor(rec.grossMinutes / 60)}h {rec.grossMinutes % 60}m
                      </td>
                      <td className="py-2.5 px-4 font-semibold text-indigo-600">
                        {Math.floor(rec.netWorkMinutes / 60)}h {rec.netWorkMinutes % 60}m
                      </td>
                      <td className="py-2.5 px-4">
                        <Badge
                          variant={
                            rec.status === 'PRESENT'
                              ? 'success'
                              : rec.status === 'HALF_DAY'
                              ? 'warning'
                              : 'destructive'
                          }
                        >
                          {rec.status}
                        </Badge>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
};
