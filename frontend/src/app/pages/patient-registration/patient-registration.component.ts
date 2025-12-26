import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { forkJoin } from 'rxjs';

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
    currentUser: any;
    isAdmin = false;
    editId: string | null = null;
    isEdit = false;
    patientToEdit: any;

    constructor(
        private fb: FormBuilder,
        private apiService: ApiService,
        private authService: AuthService,
        private router: Router,
        private route: ActivatedRoute
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
            gender: [''],
            dateOfBirth: [''],
            address: [''],
            // Assignment
            assignedDoctorId: ['']
        });
    }

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        this.isAdmin = this.currentUser?.role === 'ADMIN';
        this.loadDoctors();

        this.route.queryParams.subscribe(params => {
            if (params['editId']) {
                this.editId = params['editId'];
                this.isEdit = true;
                this.loadPatientForEdit();
            }
        });
    }

    loadPatientForEdit(): void {
        this.loading = true;
        this.apiService.get(`patients/${this.editId}`).subscribe({
            next: (data: any) => {
                this.patientToEdit = data;
                this.registrationForm.patchValue({
                    username: data.user?.username,
                    email: data.user?.email,
                    firstName: data.user?.firstName,
                    lastName: data.user?.lastName,
                    phoneNumber: data.user?.phoneNumber,
                    bloodGroup: data.bloodGroup,
                    allergies: data.allergies,
                    chronicConditions: data.chronicConditions,
                    insuranceProvider: data.insuranceProvider,
                    insuranceNumber: data.insuranceNumber,
                    emergencyContactName: data.emergencyContactName,
                    emergencyContactPhone: data.emergencyContactPhone,
                    gender: data.user?.gender,
                    dateOfBirth: data.user?.dateOfBirth,
                    address: data.user?.address,
                    assignedDoctorId: data.assignedDoctorId
                });
                // In edit mode, username and password are not meant to be updated through this form usually
                this.registrationForm.get('username')?.disable();
                this.registrationForm.get('password')?.clearValidators();
                this.registrationForm.get('password')?.updateValueAndValidity();

                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to load patient data.';
                this.loading = false;
            }
        });
    }

    loadDoctors(): void {
        this.apiService.get('staff').subscribe({
            next: (data: any) => {
                // Filter for active doctors
                this.doctors = data.filter((s: any) => s.user?.isActive);
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

        if (this.isEdit) {
            this.updatePatient();
            return;
        }

        // Step 1: Register User
        const userData = {
            username: val.username,
            email: val.email,
            password: val.password,
            role: 'PATIENT' as const,
            firstName: val.firstName,
            lastName: val.lastName,
            phoneNumber: val.phoneNumber,
            gender: val.gender,
            dateOfBirth: val.dateOfBirth,
            address: val.address
        };

        this.authService.adminRegister(userData).subscribe({
            next: (userResponse: any) => {
                // Step 2: Create Patient Profile
                // Note: AuthService now creates a Patient record automatically.
                // We should find it and update it with the additional details.
                this.apiService.get(`patients/user/${userResponse.id}`).subscribe({
                    next: (patientResponse: any) => {
                        const patientData = {
                            bloodGroup: val.bloodGroup,
                            allergies: val.allergies,
                            chronicConditions: val.chronicConditions,
                            insuranceProvider: val.insuranceProvider,
                            insuranceNumber: val.insuranceNumber,
                            emergencyContactName: val.emergencyContactName,
                            emergencyContactPhone: val.emergencyContactPhone,
                            assignedDoctorId: val.assignedDoctorId
                        };

                        this.apiService.put(`patients/${patientResponse.id}`, patientData).subscribe({
                            next: () => {
                                // Step 3: Issue Card
                                const cardData = {
                                    patientId: patientResponse.id,
                                    status: 'ACTIVE'
                                };

                                this.apiService.post('cards', cardData).subscribe({
                                    next: () => {
                                        this.loading = false;
                                        this.success = 'Patient registered and card issued successfully!';
                                        this.registrationForm.reset({
                                            password: 'atlas123'
                                        });
                                    },
                                    error: (err: any) => this.handleError(err)
                                });
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

    updatePatient(): void {
        const val = this.registrationForm.value;
        const userId = this.patientToEdit.user?.id;

        const userData = {
            email: val.email,
            firstName: val.firstName,
            lastName: val.lastName,
            phoneNumber: val.phoneNumber,
            gender: val.gender,
            dateOfBirth: val.dateOfBirth,
            address: val.address
        };

        const patientData = {
            bloodGroup: val.bloodGroup,
            allergies: val.allergies,
            chronicConditions: val.chronicConditions,
            insuranceProvider: val.insuranceProvider,
            insuranceNumber: val.insuranceNumber,
            emergencyContactName: val.emergencyContactName,
            emergencyContactPhone: val.emergencyContactPhone,
            assignedDoctorId: val.assignedDoctorId
        };

        const userUpdate$ = this.apiService.put(`users/${userId}`, userData);
        const patientUpdate$ = this.apiService.put(`patients/${this.editId}`, patientData);

        forkJoin([userUpdate$, patientUpdate$]).subscribe({
            next: () => {
                this.loading = false;
                this.success = 'Patient profile updated successfully!';
                this.router.navigate(['/patients']);
            },
            error: (err: any) => this.handleError(err)
        });
    }

    private handleError(err: any): void {
        this.loading = false;
        this.error = err.error?.message || 'An error occurred during registration.';
    }
}
