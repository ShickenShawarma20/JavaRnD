import React, { useState, useEffect } from 'react';
import { apiClient } from '../../api/apiClient';
import type { AttendanceRecord, ApiResponse } from '../../types';
import { Calendar, ChevronLeft, ChevronRight, Clock } from 'lucide-react';

export const AttendanceHistoryPage: React.FC = () => {
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [loading, setLoading] = useState(false);

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1;
      const res = await apiClient.get<ApiResponse<AttendanceRecord[]>>(`/attendance/my-history?year=${year}&month=${month}`);
      setRecords(res.data.data || []);
    } catch {
      //
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [currentDate]);

  const changeMonth = (delta: number) => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + delta, 1));
  };

  const totalNetMinutes = records.reduce((sum, r) => sum + r.netWorkMinutes, 0);
  const totalOvertimeMinutes = records.reduce((sum, r) => sum + r.overtimeMinutes, 0);
  const totalLateEntries = records.filter((r) => r.lateEntry).length;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-800">My Attendance History</h1>
          <p className="text-xs text-slate-500">Track and review daily work hours, punches, and overtime</p>
        </div>

        <div className="flex items-center gap-3 bg-white px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm">
          <button
            onClick={() => changeMonth(-1)}
            className="p-1 text-slate-500 hover:text-slate-800 rounded transition"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <span className="font-semibold text-sm text-slate-700 min-w-[120px] text-center">
            {currentDate.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })}
          </span>
          <button
            onClick={() => changeMonth(1)}
            className="p-1 text-slate-500 hover:text-slate-800 rounded transition"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Summary KPI Counters */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <p className="text-xs text-slate-500 uppercase font-semibold">Total Net Work Hours</p>
          <p className="text-xl font-bold text-indigo-600 mt-1">
            {Math.floor(totalNetMinutes / 60)}h {totalNetMinutes % 60}m
          </p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <p className="text-xs text-slate-500 uppercase font-semibold">Accrued Overtime</p>
          <p className="text-xl font-bold text-emerald-600 mt-1">
            {Math.floor(totalOvertimeMinutes / 60)}h {totalOvertimeMinutes % 60}m
          </p>
        </div>
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <p className="text-xs text-slate-500 uppercase font-semibold">Late Entry Incidents</p>
          <p className="text-xl font-bold text-amber-600 mt-1">{totalLateEntries}</p>
        </div>
      </div>

      {/* Monthly Attendance Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-500 text-xs font-semibold border-b border-slate-200">
                <th className="py-3 px-4">Date</th>
                <th className="py-3 px-4">Check In</th>
                <th className="py-3 px-4">Check Out</th>
                <th className="py-3 px-4">Breaks</th>
                <th className="py-3 px-4">Gross Time</th>
                <th className="py-3 px-4">Net Time</th>
                <th className="py-3 px-4">Late / Early</th>
                <th className="py-3 px-4">Overtime</th>
                <th className="py-3 px-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {records.length === 0 ? (
                <tr>
                  <td colSpan={9} className="py-8 text-center text-slate-400">
                    No attendance records for this month.
                  </td>
                </tr>
              ) : (
                records.map((r) => (
                  <tr key={r.sessionId} className="hover:bg-slate-50 transition">
                    <td className="py-3 px-4 font-medium text-slate-800">{r.workDate}</td>
                    <td className="py-3 px-4 font-mono text-xs">
                      {r.firstCheckIn ? new Date(r.firstCheckIn).toLocaleTimeString() : '-'}
                    </td>
                    <td className="py-3 px-4 font-mono text-xs">
                      {r.lastCheckOut ? new Date(r.lastCheckOut).toLocaleTimeString() : '-'}
                    </td>
                    <td className="py-3 px-4 text-xs text-slate-500">{r.unpaidBreakMinutes}m</td>
                    <td className="py-3 px-4 text-xs">{Math.floor(r.grossMinutes / 60)}h {r.grossMinutes % 60}m</td>
                    <td className="py-3 px-4 text-xs font-semibold text-indigo-600">
                      {Math.floor(r.netWorkMinutes / 60)}h {r.netWorkMinutes % 60}m
                    </td>
                    <td className="py-3 px-4 text-xs">
                      {r.lateEntry && <span className="text-amber-600 font-medium">Late: {r.lateEntryMinutes}m </span>}
                      {r.earlyExit && <span className="text-rose-600 font-medium">Early: {r.earlyExitMinutes}m</span>}
                      {!r.lateEntry && !r.earlyExit && <span className="text-slate-400">On Time</span>}
                    </td>
                    <td className="py-3 px-4 text-xs font-medium text-emerald-600">
                      {r.overtimeMinutes > 0 ? `+${r.overtimeMinutes}m` : '-'}
                    </td>
                    <td className="py-3 px-4">
                      <span className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${
                        r.status === 'PRESENT'
                          ? 'bg-emerald-100 text-emerald-800'
                          : r.status === 'HALF_DAY'
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-rose-100 text-rose-800'
                      }`}>
                        {r.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
