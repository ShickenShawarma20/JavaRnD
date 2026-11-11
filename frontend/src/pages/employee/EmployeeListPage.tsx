import React, { useState, useEffect } from 'react';
import { apiClient } from '../../api/apiClient';
import type { EmployeeProfile, PageResponse, ApiResponse } from '../../types';
import { Plus, Search, Building2, UserPlus, FileSpreadsheet } from 'lucide-react';

export const EmployeeListPage: React.FC = () => {
  const [employees, setEmployees] = useState<EmployeeProfile[]>([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchEmployees = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<PageResponse<EmployeeProfile>>>(`/employees?search=${search}&page=${page}&size=10`);
      setEmployees(res.data.data?.content || []);
      setTotalPages(res.data.data?.totalPages || 0);
    } catch {
      //
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, [search, page]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-800">Employee Directory</h1>
          <p className="text-xs text-slate-500">Manage organizational personnel, roles, and shift profiles</p>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search code, name..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="pl-9 pr-4 py-2 text-xs bg-white border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 w-64"
            />
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-500 text-xs font-semibold border-b border-slate-200">
                <th className="py-3 px-4">Emp Code</th>
                <th className="py-3 px-4">Full Name</th>
                <th className="py-3 px-4">Department</th>
                <th className="py-3 px-4">Designation</th>
                <th className="py-3 px-4">Reporting Manager</th>
                <th className="py-3 px-4">Joining Date</th>
                <th className="py-3 px-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {employees.map((emp) => (
                <tr key={emp.id} className="hover:bg-slate-50">
                  <td className="py-3 px-4 font-mono text-xs font-semibold text-indigo-600">{emp.employeeCode}</td>
                  <td className="py-3 px-4">
                    <p className="font-semibold text-slate-800">{emp.fullName}</p>
                    <p className="text-xs text-slate-400">{emp.email}</p>
                  </td>
                  <td className="py-3 px-4 text-xs font-medium text-slate-600">{emp.departmentName}</td>
                  <td className="py-3 px-4 text-xs text-slate-600">{emp.designationName}</td>
                  <td className="py-3 px-4 text-xs text-slate-500">{emp.reportingManagerName || 'Executive'}</td>
                  <td className="py-3 px-4 text-xs text-slate-500">{emp.joiningDate}</td>
                  <td className="py-3 px-4">
                    <span className="inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
                      {emp.employmentStatus}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
