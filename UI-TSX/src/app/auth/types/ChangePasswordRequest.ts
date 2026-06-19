// src/app/auth/types/ChangePasswordRequest.ts
export interface ChangePasswordRequest {
  id: number; // <-- Long id
  currentPassword: string;
  newPassword: string;
}
