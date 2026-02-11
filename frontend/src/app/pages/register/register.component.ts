import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
    registerForm: FormGroup;
    loading = false;
    error = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.registerForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            firstName: ['', [Validators.required]],
            lastName: ['', [Validators.required]],
            role: ['PATIENT', [Validators.required]],
            phoneNumber: [''],
            dateOfBirth: [''],
            gender: [''],
            address: ['']
        });
    }

    onSubmit(): void {
        if (this.registerForm.invalid) {
            return;
        }

        this.loading = true;
        this.error = '';

        this.authService.registerPatient(this.registerForm.value).subscribe({
            next: (response) => {
                console.log('Register component: Success', response);
                this.loading = false;
                
                // Show the generated credentials in the alert
                alert(`Registration successful!\n\nYour credentials:\nUsername: ${response.username}\nPassword: Atlas@123\n\nPlease wait for admin approval before logging in.`);
                this.router.navigate(['/login']);
            },
            error: (err) => {
                console.error('Register component: Error', err);
                this.loading = false;
                this.error = err.error?.message || 'Registration failed. The email might already be in use.';
            }
        });
    }
}
