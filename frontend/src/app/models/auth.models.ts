export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: 'ADMIN' | 'USER' | 'PATIENT';
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  address?: string;
}

export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER' | 'PATIENT';
  firstName: string;
  lastName: string;
  message: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER' | 'PATIENT';
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  address?: string;
  createdAt?: string;
}
