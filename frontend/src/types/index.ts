export interface UserContext {
  userId: string;
  username: string;
  email: string;
  roles: string[];
  permissions: string[];
  employeeId?: string;
  employeeCode?: string;
  fullName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: string;
  username: string;
  email: string;
  roles: string[];
  permissions: string[];
  employeeId?: string;
  employeeCode?: string;
  fullName: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}

export type AttendanceStatus =
  | 'PRESENT'
  | 'HALF_DAY'
  | 'ABSENT'
  | 'ON_LEAVE'
  | 'HOLIDAY'
  | 'WEEK_OFF'
  | 'MISSING_PUNCH';

export interface AttendanceRecord {
  sessionId: string;
  employeeId: string;
  employeeCode: string;
  employeeName: string;
  shiftName: string;
  workDate: string;
  firstCheckIn?: string;
  lastCheckOut?: string;
  grossMinutes: number;
  unpaidBreakMinutes: number;
  netWorkMinutes: number;
  overtimeMinutes: number;
  lateEntry: boolean;
  lateEntryMinutes: number;
  earlyExit: boolean;
  earlyExitMinutes: number;
  status: AttendanceStatus;
  regularized: boolean;
}

export interface EmployeeProfile {
  id: string;
  userId: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  username: string;
  phoneNumber?: string;
  departmentId: string;
  departmentName: string;
  designationId: string;
  designationName: string;
  reportingManagerId?: string;
  reportingManagerName?: string;
  joiningDate: string;
  employmentStatus: 'ACTIVE' | 'PROBATION' | 'SUSPENDED' | 'TERMINATED' | 'RESIGNED';
}

export interface LeaveBalance {
  id: string;
  leaveType: {
    id: string;
    typeCode: string;
    typeName: string;
  };
  year: number;
  totalAllocated: number;
  used: number;
  balance: number;
}

export interface LeaveRecord {
  id: string;
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  leaveTypeCode: string;
  leaveTypeName: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  halfDay: boolean;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  reviewerName?: string;
  reviewedAt?: string;
  rejectionRemarks?: string;
  createdAt: string;
}

export interface RegularizationRecord {
  id: string;
  requestedCheckIn?: string;
  requestedCheckOut?: string;
  reason: string;
  status: string;
  rejectionReason?: string;
  createdAt: string;
}
