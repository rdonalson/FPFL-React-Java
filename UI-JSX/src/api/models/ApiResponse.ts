export interface ApiResponse<T> {
  correlationId: string;
  data: T;
  message: string;
  status: number;
  timestamp: string;
}

