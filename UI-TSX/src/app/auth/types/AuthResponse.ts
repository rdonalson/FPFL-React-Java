export interface AuthResponse {
  accessToken: string;
  refreshToken: string | null;
  id: number;
  userId: string;
  email: string;
  first: string;
  last: string;
  roles: string[];
}
