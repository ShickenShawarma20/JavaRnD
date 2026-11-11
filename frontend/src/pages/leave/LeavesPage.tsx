import React, { useState, useEffect } from 'react';
import { apiClient } from '../../api/apiClient';
import type { LeaveBalance, LeaveRecord, ApiResponse, PageResponse } from '../../types';
import { Plus, Check, X, Calendar, Clock } from 'lucide-react';

export const LeavesPage: React.FC = () => {
  const [balances, setBalances] = useState<LeaveBalance[]>([]);
  const [myRequests, setMyRequests] = useState<LeaveRecord[]>([]);
  const [showApplyModal, setShowApplyModal] = useState(false);

  // Form State
  const [leaveTypeId, setLeaveTypeId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [totalDays, setTotalDays] = useState('1');
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchData = async () => {
    try {
      const balRes = await apiClient.get<ApiResponse<LeaveBalance[]>>('/leaves/balances');
      setBalances(balRes.data.data || []);
      if (balRes.data.data && balRes.data.data.length > 0 && !leaveTypeId) {
        setLeaveTypeId(balRes.data.data[0].leaveType.id);
      }

      const reqRes = await apiClient.get<ApiResponse<PageResponse<LeaveRecord>>>('/leaves/my-requests');
      setMyRequests(reqRes.data.data?.content || []);
    } catch {
      //
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleApply = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await apiClient.post('/leaves/apply', {
        leaveTypeId,
        startDate,
        endDate,
        totalDays: parseFloat(totalDays),
        reason,
      });
      setShowApplyModal(false);
      setReason('');
      fetchData();
      alert('Leave application submitted successfully!');
    } catch (err: unknown) {
      const errorMsg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to submit leave';
      alert(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-800">Leave Management</h1>
          <p className="text-xs text-slate-500">Track quotas, apply for leave, and view approval status</p>
        </div>

        <button
          onClick={() => setShowApplyModal(true)}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium text-sm rounded-lg shadow-sm transition"
        >
          <Plus className="w-4 h-4" />
          Apply for Leave
        </button>
      </div>

      {/* Leave Balances Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {balances.map((b) => (
          <div key={b.id} className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
            <span className="text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-600">
              {b.leaveType.typeCode}
            </span>
            <h4 className="text-sm font-semibold text-slate-800 mt-2">{b.leaveType.typeName}</h4>
            <div className="mt-4 flex items-baseline justify-between border-t border-slate-100 pt-3">
              <div>
                <p className="text-2xl font-bold text-indigo-600">{b.balance}</p>
                <p className="text-xs text-slate-400">Available Days</p>
              </div>
              <div className="text-right">
                <p className="text-xs text-slate-500">Used: <span className="font-semibold text-slate-700">{b.used}</span></p>
                <p className="text-xs text-slate-500">Quota: <span className="font-semibold text-slate-700">{b.totalAllocated}</span></p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Leave Applications History */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-slate-100">
          <h3 className="font-semibold text-slate-800 text-sm">My Leave Applications</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-500 text-xs font-semibold border-b border-slate-200">
                <th className="py-3 px-4">Leave Type</th>
                <th className="py-3 px-4">Period</th>
                <th className="py-3 px-4">Days</th>
                <th className="py-3 px-4">Reason</th>
                <th className="py-3 px-4">Reviewer</th>
                <th className="py-3 px-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {myRequests.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-400">
                    No leave requests found.
                  </td>
                </tr>
              ) : (
                myRequests.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-50 transition">
                    <td className="py-3 px-4 font-semibold text-slate-800">{r.leaveTypeName}</td>
                    <td className="py-3 px-4 text-xs font-mono text-slate-600">
                      {r.startDate} to {r.endDate}
                    </td>
                    <td className="py-3 px-4 font-medium">{r.totalDays}</td>
                    <td className="py-3 px-4 text-slate-600 max-w-xs truncate">{r.reason}</td>
                    <td className="py-3 px-4 text-xs text-slate-500">{r.reviewerName || 'Pending Assignment'}</td>
                    <td className="py-3 px-4">
                      <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        r.status === 'APPROVED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : r.status === 'REJECTED'
                          ? 'bg-rose-100 text-rose-800'
                          : 'bg-amber-100 text-amber-800'
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

      {/* Apply Leave Modal */}
      {showApplyModal && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg overflow-hidden">
            <div className="p-5 border-b border-slate-100 flex items-center justify-between">
              <h3 className="font-semibold text-slate-800">Submit Leave Application</h3>
              <button
                onClick={() => setShowApplyModal(false)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleApply} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Leave Category</label>
                <select
                  value={leaveTypeId}
                  onChange={(e) => setLeaveTypeId(e.target.value)}
                  required
                  className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  {balances.map((b) => (
                    <option key={b.leaveType.id} value={b.leaveType.id}>
                      {b.leaveType.typeName} ({b.balance} days remaining)
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">From Date</label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">To Date</label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Total Days</label>
                <input
                  type="number"
                  step="0.5"
                  min="0.5"
                  value={totalDays}
                  onChange={(e) => setTotalDays(e.target.value)}
                  required
                  className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Reason for Leave</label>
                <textarea
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  required
                  placeholder="Provide context or emergency details..."
                  className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                ></textarea>
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowApplyModal(false)}
                  className="px-4 py-2 text-sm text-slate-600 hover:bg-slate-100 rounded-lg font-medium transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-5 py-2 text-sm bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg shadow-sm transition disabled:opacity-50"
                >
                  {loading ? 'Submitting...' : 'Confirm & Apply'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
