import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly API_URL = 'http://localhost:8080/api/auth';
    private readonly TOKEN_KEY = 'jwt_token';
    private readonly USER_KEY = 'current_user';

    private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient) { }

    login(credentials: LoginRequest): Observable<AuthResponse> {
        console.log('Attempting login for:', credentials.username);
        return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials)
            .pipe(
                tap(response => {
                    console.log('Login successful for:', response.username);
                    this.handleAuthResponse(response);
                })
            );
    }

    registerPatient(userData: RegisterRequest): Observable<AuthResponse> {
        console.log('Attempting patient registration for:', userData.username);
        // Patient registration does not return a token immediately (pending approval)
        return this.http.post<AuthResponse>(`${this.API_URL}/register-patient`, userData);
    }

    // Admin version: Does NOT login the newly created user
    adminRegister(userData: RegisterRequest): Observable<any> {
        console.log('Admin creating account for:', userData.username);
        return this.http.post<any>(`http://localhost:8080/api/admin/users`, userData);
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        this.currentUserSubject.next(null);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    getCurrentUser(): User | null {
        return this.currentUserSubject.value;
    }

    private handleAuthResponse(response: AuthResponse): void {
        // Store token
        localStorage.setItem(this.TOKEN_KEY, response.token);

        // Create user object and store
        const user: User = {
            id: response.id,
            username: response.username,
            email: response.email,
            role: response.role,
            firstName: response.firstName,
            lastName: response.lastName
        };

        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUserSubject.next(user);
    }

    private getUserFromStorage(): User | null {
        const userJson = localStorage.getItem(this.USER_KEY);
        return userJson ? JSON.parse(userJson) : null;
    }
}
