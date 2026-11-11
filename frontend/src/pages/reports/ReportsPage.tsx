import React, { useState } from 'react';
import { apiClient } from '../../api/apiClient';
import { FileSpreadsheet, Download, Calendar } from 'lucide-react';

export const ReportsPage: React.FC = () => {
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [downloading, setDownloading] = useState(false);

  const downloadDailyExcel = async () => {
    setDownloading(true);
    try {
      const response = await apiClient.get(`/reports/daily/excel?date=${selectedDate}`, {
        responseType: 'blob',
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Daily_Attendance_${selectedDate}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch {
      alert('Failed to generate excel report');
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-800">Reports & Payroll Export Engine</h1>
        <p className="text-xs text-slate-500">Generate formatted spreadsheets and payroll summaries for auditing</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-emerald-50 text-emerald-600 rounded-lg">
              <FileSpreadsheet className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-semibold text-slate-800">Daily Attendance Master (.xlsx)</h3>
              <p className="text-xs text-slate-500">Full punch audits, gross/net times, breaks, and overtime hours</p>
            </div>
          </div>

          <div className="pt-2">
            <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Target Work Date</label>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <button
            onClick={downloadDailyExcel}
            disabled={downloading}
            className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg shadow-sm transition disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            {downloading ? 'Compiling Excel...' : 'Export Excel Spreadsheet'}
          </button>
        </div>
      </div>
    </div>
  );
};
