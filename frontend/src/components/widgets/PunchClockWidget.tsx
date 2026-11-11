import React, { useState, useEffect } from 'react';
import { apiClient } from '../../api/apiClient';
import type { AttendanceRecord } from '../../types';
import { Clock, LogIn, LogOut, Coffee, CheckCircle2 } from 'lucide-react';

export const PunchClockWidget: React.FC<{ onPunchSuccess?: () => void }> = ({ onPunchSuccess }) => {
  const [session, setSession] = useState<AttendanceRecord | null>(null);
  const [loading, setLoading] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());

  const fetchSession = async () => {
    try {
      const res = await apiClient.get('/attendance/today');
      setSession(res.data.data);
    } catch {
      // no session yet today
    }
  };

  useEffect(() => {
    fetchSession();
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const handlePunch = async (eventType: 'CHECK_IN' | 'CHECK_OUT' | 'BREAK_START' | 'BREAK_END') => {
    setLoading(true);
    try {
      const res = await apiClient.post('/attendance/punch', {
        eventType,
        deviceInfo: navigator.userAgent,
      });
      setSession(res.data.data);
      if (onPunchSuccess) onPunchSuccess();
    } catch (err: unknown) {
      const errorMsg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Punch operation failed';
      alert(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const isCheckedIn = !!session?.firstCheckIn && !session?.lastCheckOut;
  const isCheckedOut = !!session?.lastCheckOut;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col md:flex-row items-center justify-between gap-6">
      <div className="flex items-center gap-4">
        <div className="p-3 bg-indigo-50 border border-indigo-100 rounded-xl text-indigo-600">
          <Clock className="w-8 h-8" />
        </div>
        <div>
          <div className="text-2xl font-bold tracking-tight text-slate-800">
            {currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
          </div>
          <p className="text-xs font-medium text-slate-500">
            {currentTime.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </p>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        {!isCheckedIn && !isCheckedOut && (
          <button
            onClick={() => handlePunch('CHECK_IN')}
            disabled={loading}
            className="flex items-center gap-2 px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg shadow-sm transition disabled:opacity-50"
          >
            <LogIn className="w-4 h-4" />
            Check In
          </button>
        )}

        {isCheckedIn && (
          <>
            <button
              onClick={() => handlePunch('BREAK_START')}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2.5 bg-amber-500 hover:bg-amber-600 text-white font-medium text-sm rounded-lg shadow-sm transition disabled:opacity-50"
            >
              <Coffee className="w-4 h-4" />
              Take Break
            </button>
            <button
              onClick={() => handlePunch('BREAK_END')}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2.5 bg-amber-600 hover:bg-amber-700 text-white font-medium text-sm rounded-lg shadow-sm transition disabled:opacity-50"
            >
              End Break
            </button>
            <button
              onClick={() => handlePunch('CHECK_OUT')}
              disabled={loading}
              className="flex items-center gap-2 px-6 py-2.5 bg-rose-600 hover:bg-rose-700 text-white font-medium text-sm rounded-lg shadow-sm transition disabled:opacity-50"
            >
              <LogOut className="w-4 h-4" />
              Check Out
            </button>
          </>
        )}

        {isCheckedOut && (
          <div className="flex items-center gap-2 text-sm font-semibold text-emerald-700 bg-emerald-50 px-4 py-2 rounded-lg border border-emerald-200">
            <CheckCircle2 className="w-5 h-5" />
            Completed for Today
          </div>
        )}
      </div>
    </div>
  );
};
