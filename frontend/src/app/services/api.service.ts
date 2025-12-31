import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private readonly BASE_URL = '/api';

    constructor(private http: HttpClient) { }

    get(path: string): Observable<any> {
        return this.http.get(`${this.BASE_URL}/${path}`);
    }

    post(path: string, body: any): Observable<any> {
        return this.http.post(`${this.BASE_URL}/${path}`, body);
    }

    put(path: string, body: any): Observable<any> {
        return this.http.put(`${this.BASE_URL}/${path}`, body);
    }

    delete(path: string): Observable<any> {
        return this.http.delete(`${this.BASE_URL}/${path}`);
    }
}
