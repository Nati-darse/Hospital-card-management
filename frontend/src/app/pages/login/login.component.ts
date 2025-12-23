import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {
    loginForm: FormGroup;
    loading = false;
    error = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.loginForm = this.fb.group({
            username: ['', [Validators.required]],
            password: ['', [Validators.required, Validators.minLength(6)]]
        });
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            return;
        }

        this.loading = true;
        this.error = '';

        this.authService.login(this.loginForm.value).subscribe({
            next: (response) => {
                console.log('Login component: Success', response);
                this.loading = false;
                const user = this.authService.getCurrentUser();
                if (user?.role === 'ADMIN') {
                    this.router.navigate(['/dashboard']);
                } else if (user?.role === 'PATIENT') {
                    this.router.navigate(['/patient-portal']);
                } else if (user?.role === 'USER') { // Doctor
                    this.router.navigate(['/doctor-portal']);
                } else {
                    this.router.navigate(['/dashboard']);
                }
            },
            error: (err) => {
                console.error('Login component: Error', err);
                this.loading = false;
                this.error = err.error?.message || 'Access denied. Please check your credentials.';
            }
        });
    }
}
