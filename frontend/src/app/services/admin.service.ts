import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private readonly API_URL = '/api/admin';

    constructor(private http: HttpClient) { }

    getPendingUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.API_URL}/users/pending`);
    }

    approveUser(userId: number): Observable<string> {
        return this.http.put(`${this.API_URL}/users/${userId}/approve`, {}, { responseType: 'text' });
    }

    deactivateUser(userId: number): Observable<string> {
        return this.http.put(`${this.API_URL}/users/${userId}/deactivate`, {}, { responseType: 'text' });
    }
}
