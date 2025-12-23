import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-patient-registration',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './patient-registration.component.html',
    styleUrl: './patient-registration.scss'
})
export class PatientRegistrationComponent implements OnInit {
    registrationForm: FormGroup;
    doctors: any[] = [];
    loading = false;
    error = '';
    success = '';

    constructor(
        private fb: FormBuilder,
        private apiService: ApiService,
        private authService: AuthService,
        private router: Router
    ) {
        this.registrationForm = this.fb.group({
            // User Info
            username: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            password: ['123456', [Validators.required]], // Default password for simplicity
            firstName: ['', [Validators.required]],
            lastName: ['', [Validators.required]],
            phoneNumber: [''],
            // Patient Info
            bloodGroup: [''],
            allergies: [''],
            chronicConditions: [''],
            insuranceProvider: [''],
            insuranceNumber: [''],
            emergencyContactName: [''],
            emergencyContactPhone: [''],
            // Assignment
            assignedDoctorId: ['']
        });
    }

    ngOnInit(): void {
        this.loadDoctors();
    }

    loadDoctors(): void {
        this.apiService.get('staff').subscribe({
            next: (data: any) => {
                // Filter for doctors if needed, or assume all staff in the list are doctors for now
                this.doctors = data;
            },
            error: (err: any) => console.error('Error loading doctors', err)
        });
    }

    onSubmit(): void {
        if (this.registrationForm.invalid) return;

        this.loading = true;
        this.error = '';
        this.success = '';

        const val = this.registrationForm.value;

        // Step 1: Register User
        const userData = {
            username: val.username,
            email: val.email,
            password: val.password,
            role: 'PATIENT' as const,
            firstName: val.firstName,
            lastName: val.lastName,
            phoneNumber: val.phoneNumber
        };

        this.authService.register(userData).subscribe({
            next: (userResponse: any) => {
                // Step 2: Create Patient Profile
                const patientData = {
                    userId: userResponse.id,
                    bloodGroup: val.bloodGroup,
                    allergies: val.allergies,
                    chronicConditions: val.chronicConditions,
                    insuranceProvider: val.insuranceProvider,
                    insuranceNumber: val.insuranceNumber,
                    emergencyContactName: val.emergencyContactName,
                    emergencyContactPhone: val.emergencyContactPhone
                };

                this.apiService.post('patients', patientData).subscribe({
                    next: (patientResponse: any) => {
                        // Step 3: Issue Card
                        const cardData = {
                            patientId: patientResponse.id,
                            status: 'ACTIVE'
                        };

                        this.apiService.post('cards', cardData).subscribe({
                            next: () => {
                                this.loading = false;
                                this.success = 'Patient registered and card issued successfully!';
                                setTimeout(() => this.router.navigate(['/patients']), 2000);
                            },
                            error: (err: any) => this.handleError(err)
                        });
                    },
                    error: (err: any) => this.handleError(err)
                });
            },
            error: (err: any) => this.handleError(err)
        });
    }

    private handleError(err: any): void {
        this.loading = false;
        this.error = err.error?.message || 'An error occurred during registration.';
    }
}
