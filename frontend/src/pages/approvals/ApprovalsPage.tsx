import React, { useState, useEffect } from 'react';
import { apiClient } from '../../api/apiClient';
import type { LeaveRecord, RegularizationRecord, ApiResponse } from '../../types';
import { Check, X, UserCheck } from 'lucide-react';

export const ApprovalsPage: React.FC = () => {
  const [leaves, setLeaves] = useState<LeaveRecord[]>([]);
  const [regularizations, setRegularizations] = useState<RegularizationRecord[]>([]);
  const [activeTab, setActiveTab] = useState<'leaves' | 'regularizations'>('leaves');
  const [loading, setLoading] = useState(false);

  const fetchApprovals = async () => {
    setLoading(true);
    try {
      const leaveRes = await apiClient.get<ApiResponse<LeaveRecord[]>>('/leaves/pending');
      setLeaves(leaveRes.data.data || []);

      const regRes = await apiClient.get<ApiResponse<RegularizationRecord[]>>('/regularizations/pending');
      setRegularizations(regRes.data.data || []);
    } catch {
      //
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApprovals();
  }, []);

  const handleLeaveAction = async (id: string, approve: boolean) => {
    const remarks = prompt(approve ? 'Optional approval note:' : 'Enter rejection reason:');
    if (!approve && !remarks) return;

    try {
      await apiClient.put(`/leaves/${id}/action`, { approve, remarks: remarks || 'Processed by Manager' });
      fetchApprovals();
    } catch (err: unknown) {
      const errorMsg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Action failed';
      alert(errorMsg);
    }
  };

  const handleRegAction = async (id: string, approve: boolean) => {
    const remarks = prompt(approve ? 'Approval remarks:' : 'Rejection remarks:');
    try {
      await apiClient.put(`/regularizations/${id}/action`, { approve, remarks: remarks || 'Reviewed' });
      fetchApprovals();
    } catch (err: unknown) {
      const errorMsg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Action failed';
      alert(errorMsg);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-800">Team Approval Center</h1>
        <p className="text-xs text-slate-500">Review and authorize subordinates' leave and punch correction requests</p>
      </div>

      <div className="flex border-b border-slate-200">
        <button
          onClick={() => setActiveTab('leaves')}
          className={`py-3 px-6 text-sm font-semibold border-b-2 transition ${
            activeTab === 'leaves'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          Leave Applications ({leaves.length})
        </button>
        <button
          onClick={() => setActiveTab('regularizations')}
          className={`py-3 px-6 text-sm font-semibold border-b-2 transition ${
            activeTab === 'regularizations'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          Attendance Regularizations ({regularizations.length})
        </button>
      </div>

      {activeTab === 'leaves' && (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-500 text-xs font-semibold border-b border-slate-200">
                <th className="py-3 px-4">Employee</th>
                <th className="py-3 px-4">Leave Category</th>
                <th className="py-3 px-4">Dates</th>
                <th className="py-3 px-4">Days</th>
                <th className="py-3 px-4">Reason</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {leaves.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-400">
                    No pending leave requests awaiting approval.
                  </td>
                </tr>
              ) : (
                leaves.map((l) => (
                  <tr key={l.id} className="hover:bg-slate-50">
                    <td className="py-3 px-4">
                      <p className="font-medium text-slate-800">{l.employeeName}</p>
                      <p className="text-xs text-slate-400">{l.employeeCode}</p>
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-700">{l.leaveTypeName}</td>
                    <td className="py-3 px-4 text-xs font-mono">{l.startDate} to {l.endDate}</td>
                    <td className="py-3 px-4 font-semibold">{l.totalDays}</td>
                    <td className="py-3 px-4 text-slate-600 max-w-xs truncate">{l.reason}</td>
                    <td className="py-3 px-4 text-right space-x-2">
                      <button
                        onClick={() => handleLeaveAction(l.id, true)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-medium"
                      >
                        <Check className="w-3.5 h-3.5" /> Approve
                      </button>
                      <button
                        onClick={() => handleLeaveAction(l.id, false)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded text-xs font-medium"
                      >
                        <X className="w-3.5 h-3.5" /> Reject
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'regularizations' && (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-500 text-xs font-semibold border-b border-slate-200">
                <th className="py-3 px-4">Requested In</th>
                <th className="py-3 px-4">Requested Out</th>
                <th className="py-3 px-4">Reason</th>
                <th className="py-3 px-4">Submitted At</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {regularizations.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-slate-400">
                    No pending attendance regularization requests.
                  </td>
                </tr>
              ) : (
                regularizations.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-50">
                    <td className="py-3 px-4 font-mono text-xs">{r.requestedCheckIn || '-'}</td>
                    <td className="py-3 px-4 font-mono text-xs">{r.requestedCheckOut || '-'}</td>
                    <td className="py-3 px-4 text-slate-700 max-w-sm truncate">{r.reason}</td>
                    <td className="py-3 px-4 text-xs text-slate-400">{new Date(r.createdAt).toLocaleString()}</td>
                    <td className="py-3 px-4 text-right space-x-2">
                      <button
                        onClick={() => handleRegAction(r.id, true)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-medium"
                      >
                        <Check className="w-3.5 h-3.5" /> Approve
                      </button>
                      <button
                        onClick={() => handleRegAction(r.id, false)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded text-xs font-medium"
                      >
                        <X className="w-3.5 h-3.5" /> Reject
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
