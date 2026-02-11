import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
    selector: 'app-doctor-patients',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
    templateUrl: './doctor-patients.component.html',
    styleUrl: './doctor-patients.component.scss'
})
export class DoctorPatientsComponent implements OnInit {
    currentUser: User | null = null;
    staffProfile: any = null;
    allPatients: any[] = [];
    patients: any[] = [];
    searchQuery = '';
    selectedPatient: any = null;
    loading = false;

    // Password Update
    showPasswordForm = false;
    passwordForm: FormGroup;
    passwordError = '';
    passwordSuccess = '';

    // Referral
    showReferralForm = false;
    departments: string[] = ['Cardiology', 'Neurology', 'Surgery', 'Pediatrics', 'Orthopedics', 'General Medicine'];
    referralSearchDepartment = '';
    referralDoctors: any[] = [];
    referralLoading = false;
    referralForm: FormGroup;

    // Patient Update
    showUpdateForm = false;
    updateForm: FormGroup;

    // Prescription
    showPrescriptionForm = false;
    prescriptionForm: FormGroup;
    patientPrescriptions: any[] = [];

    // Case Management
    showCaseForm = false;
    caseForm: FormGroup;
    patientCases: any[] = [];

    constructor(
        private authService: AuthService,
        private apiService: ApiService,
        private router: Router,
        private fb: FormBuilder
    ) {
        this.updateForm = this.fb.group({
            phoneNumber: [''],
            address: [''],
            emergencyContact: ['']
        });

        this.passwordForm = this.fb.group({
            currentPassword: ['', [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, { validator: this.passwordMatchValidator });

        this.prescriptionForm = this.fb.group({
            medication: ['', [Validators.required]],
            dosage: [''],
            instructions: ['']
        });

        this.caseForm = this.fb.group({
            diagnosis: ['', [Validators.required]],
            labTests: [''],
            prescription: [''],
            followUpDate: [''],
            additionalComments: ['']
        });

        this.referralForm = this.fb.group({
            department: ['', [Validators.required]],
            referredDoctorId: ['', [Validators.required]],
            referralReason: ['', [Validators.required]]
        });
    }

    passwordMatchValidator(g: FormGroup) {
        return g.get('newPassword')?.value === g.get('confirmPassword')?.value
            ? null : { 'mismatch': true };
    }

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        if (this.currentUser) {
            this.loadDoctorProfile();
        }
    }

    loadDoctorProfile(): void {
        this.loading = true;
        this.apiService.get('staff/me').subscribe({
            next: (profile: any) => {
                this.staffProfile = profile;
                this.loadPatients();
            },
            error: () => this.loading = false
        });
    }

    loadPatients(): void {
        if (!this.staffProfile) return;
        this.apiService.get(`patients?doctorId=${this.staffProfile.id}`).subscribe({
            next: (assignedPatients: any[]) => {
                this.patients = assignedPatients;
                this.allPatients = assignedPatients;
                this.loading = false;
            },
            error: () => this.loading = false
        });
    }

    onSearch(): void {
        if (!this.searchQuery.trim()) {
            this.patients = this.allPatients;
            return;
        }

        const query = this.searchQuery.toLowerCase().trim();
        this.patients = this.allPatients.filter(p => {
            const mrnMatch = p.medicalRecordNumber?.toLowerCase().includes(query) || false;
            const firstNameMatch = p.user?.firstName?.toLowerCase().includes(query) || false;
            const lastNameMatch = p.user?.lastName?.toLowerCase().includes(query) || false;
            return mrnMatch || firstNameMatch || lastNameMatch;
        });
    }

    selectPatient(patient: any): void {
        this.selectedPatient = patient;
        this.showUpdateForm = false;
        this.showReferralForm = false;
        this.showPrescriptionForm = false;
        this.showCaseForm = false;

        if (patient.user) {
            this.updateForm.patchValue({
                phoneNumber: patient.user.phoneNumber,
                address: patient.user.address,
                emergencyContact: patient.emergencyContactPhone
            });
        }
        this.loadPatientPrescriptions(patient.id);
        this.loadPatientCases();
    }

    loadPatientPrescriptions(patientId: number): void {
        this.apiService.get(`prescriptions/patient/${patientId}`).subscribe({
            next: (list: any[]) => this.patientPrescriptions = list
        });
    }

    togglePrescriptionForm(): void {
        this.showPrescriptionForm = !this.showPrescriptionForm;
        this.showUpdateForm = false;
        this.showReferralForm = false;
        if (this.showPrescriptionForm) {
            this.prescriptionForm.reset();
        }
    }

    addPrescription(): void {
        if (this.prescriptionForm.invalid || !this.selectedPatient || !this.staffProfile) return;
        this.loading = true;
        const payload = {
            ...this.prescriptionForm.value,
            patientId: this.selectedPatient.id,
            doctorId: this.staffProfile.id
        };

        this.apiService.post('prescriptions', payload).subscribe({
            next: () => {
                alert('Prescription added successfully!');
                this.showPrescriptionForm = false;
                this.loadPatientPrescriptions(this.selectedPatient.id);
                this.loading = false;
            },
            error: () => this.loading = false
        });
    }

    toggleUpdateForm(): void {
        this.showUpdateForm = !this.showUpdateForm;
        this.showReferralForm = false;
    }

    toggleReferralForm(): void {
        this.showReferralForm = !this.showReferralForm;
        this.showUpdateForm = false;
        this.showPrescriptionForm = false;
        this.showCaseForm = false;
        if (!this.showReferralForm) {
            this.referralForm.reset();
            this.referralDoctors = [];
        }
    }

    updatePatientInfo(): void {
        if (this.updateForm.invalid || !this.selectedPatient) return;
        this.loading = true;
        const payload = { ...this.selectedPatient, ...this.updateForm.value };
        this.apiService.put(`patients/${this.selectedPatient.id}`, payload).subscribe({
            next: () => {
                this.loading = false;
                alert('Patient information updated successfully.');
                this.showUpdateForm = false;
                this.loadPatients();
            },
            error: () => this.loading = false
        });
    }

    searchDoctors(): void {
        if (!this.referralSearchDepartment) return;
        this.referralLoading = true;
        this.apiService.get(`staff/search?department=${this.referralSearchDepartment}`).subscribe({
            next: (docs: any[]) => {
                this.referralDoctors = docs.filter(d => d.id !== this.staffProfile.id);
                this.referralLoading = false;
            },
            error: () => this.referralLoading = false
        });
    }

    togglePasswordForm(): void {
        this.showPasswordForm = !this.showPasswordForm;
        this.passwordError = '';
        this.passwordSuccess = '';
        if (this.showPasswordForm) {
            this.passwordForm.reset();
        }
    }

    updatePassword(): void {
        if (this.passwordForm.invalid) return;
        this.loading = true;
        this.passwordError = '';
        this.passwordSuccess = '';

        this.apiService.post('users/change-password', {
            currentPassword: this.passwordForm.value.currentPassword,
            newPassword: this.passwordForm.value.newPassword
        }).subscribe({
            next: () => {
                this.loading = false;
                this.passwordSuccess = 'Password updated successfully!';
                setTimeout(() => this.showPasswordForm = false, 2000);
            },
            error: (err) => {
                this.loading = false;
                this.passwordError = err.error?.message || 'Failed to update password.';
            }
        });
    }

    // Case Management Methods
    toggleCaseForm(): void {
        this.showCaseForm = !this.showCaseForm;
        if (!this.showCaseForm) {
            this.caseForm.reset();
        }
    }

    addCase(): void {
        if (this.caseForm.invalid || !this.selectedPatient) return;

        this.loading = true;
        const caseData = {
            ...this.caseForm.value,
            patient: { id: this.selectedPatient.id },
            doctor: { id: this.currentUser?.id },
            visitDate: new Date().toISOString().split('T')[0],
            status: 'Active'
        };

        console.log('Adding medical case:', caseData);

        // Use correct backend endpoint: /api/visits (not /api/medical-visits)
        this.apiService.post('visits', caseData).subscribe({
            next: (response) => {
                this.loading = false;
                alert('Medical case added successfully.');
                this.caseForm.reset();
                this.showCaseForm = false;
                this.loadPatientCases();
            },
            error: (err) => {
                console.error('Failed to add medical case, trying alternative endpoint:', err);
                // Try alternative endpoint (without api prefix since ApiService already adds it)
                this.apiService.post('visits', caseData).subscribe({
                    next: (response) => {
                        this.loading = false;
                        alert('Medical case added successfully.');
                        this.caseForm.reset();
                        this.showCaseForm = false;
                        this.loadPatientCases();
                    },
                    error: (err2) => {
                        this.loading = false;
                        alert('Failed to add medical case. The backend service may be temporarily unavailable. Please try again later.');
                        console.error('Medical case creation failed:', err2);
                    }
                });
            }
        });
    }

    loadPatientCases(): void {
        if (!this.selectedPatient) return;
        
        // Use correct backend endpoint: /api/visits/patient/{id} (not /api/medical-visits/patient/{id})
        this.apiService.get(`visits/patient/${this.selectedPatient.id}`).subscribe({
            next: (cases) => {
                this.patientCases = cases;
            },
            error: (err) => {
                console.error('Failed to load patient cases, trying alternative endpoint:', err);
                // Fallback to different endpoint pattern (without api prefix since ApiService already adds it)
                this.apiService.get(`visits/patient/${this.selectedPatient.id}`).subscribe({
                    next: (cases) => {
                        this.patientCases = cases;
                    },
                    error: (err2) => {
                        console.error('Patient cases loading failed:', err2);
                        this.patientCases = [];
                    }
                });
            }
        });
    }

    // Referral Methods
    onDepartmentChange(): void {
        const department = this.referralForm.value.department;
        console.log('Department changed to:', department);
        if (department) {
            this.referralLoading = true;
            // Try different endpoint patterns
            this.apiService.get(`staff/department/${department}`).subscribe({
                next: (doctors: any[]) => {
                    console.log('Doctors loaded:', doctors);
                    // Filter out current doctor
                    this.referralDoctors = doctors.filter((d: any) => d.id !== this.currentUser?.id);
                    this.referralLoading = false;
                },
                error: (err) => {
                    console.error('Failed to load doctors by department, trying search endpoint:', err);
                    // Fallback to search endpoint
                    this.apiService.get(`staff/search?department=${department}`).subscribe({
                        next: (doctors: any[]) => {
                            console.log('Doctors loaded via search:', doctors);
                            // Filter out current doctor
                            this.referralDoctors = doctors.filter((d: any) => d.id !== this.currentUser?.id);
                            this.referralLoading = false;
                        },
                        error: (err2) => {
                            console.error('Search endpoint failed, trying direct staff endpoint:', err2);
                            // Try with direct staff endpoint (without api prefix since ApiService already adds it)
                            this.apiService.get(`staff/search?department=${department}`).subscribe({
                                next: (doctors: any[]) => {
                                    console.log('Doctors loaded via direct search:', doctors);
                                    // Filter out current doctor
                                    this.referralDoctors = doctors.filter((d: any) => d.id !== this.currentUser?.id);
                                    this.referralLoading = false;
                                },
                                error: (err3) => {
                                    console.error('All staff endpoints failed:', err3);
                                    this.referralLoading = false;
                                    this.referralDoctors = [];
                                }
                            });
                        }
                    });
                }
            });
        } else {
            this.referralDoctors = [];
        }
    }

    referPatient(): void {
        // Referral functionality is not yet implemented in the backend
        alert('Referral feature is currently under development. Please contact the administration directly for patient referrals.');
        this.referralForm.reset();
        this.showReferralForm = false;
        this.referralLoading = false;
        
        // Future implementation when backend is ready:
        /*
        if (this.referralForm.invalid || !this.selectedPatient) return;

        this.referralLoading = true;
        const referralData = {
            patient: { id: this.selectedPatient.id },
            referringDoctor: { id: this.currentUser?.id },
            referredDoctor: { id: this.referralForm.value.referredDoctorId },
            department: this.referralForm.value.department,
            reason: this.referralForm.value.referralReason,
            status: 'Pending',
            referralDate: new Date().toISOString().split('T')[0]
        };

        console.log('Sending referral data:', referralData);

        this.apiService.post('referrals', referralData).subscribe({
            next: (response) => {
                this.referralLoading = false;
                alert('Referral sent successfully! The doctor will be notified.');
                this.referralForm.reset();
                this.showReferralForm = false;
                this.createNotificationForReferredDoctor(referralData);
            },
            error: (err) => {
                console.error('Referral error, trying alternative endpoint:', err);
                // Try alternative endpoint (without api prefix since ApiService already adds it)
                this.apiService.post('referrals', referralData).subscribe({
                    next: (response) => {
                        this.referralLoading = false;
                        alert('Referral sent successfully! The doctor will be notified.');
                        this.referralForm.reset();
                        this.showReferralForm = false;
                        this.createNotificationForReferredDoctor(referralData);
                    },
                    error: (err2) => {
                        this.referralLoading = false;
                        alert('Failed to send referral. The backend service may be temporarily unavailable. Please try again later.');
                        console.error('Referral failed:', err2);
                    }
                });
            }
        });
        */
    }

    createNotificationForReferredDoctor(referralData: any): void {
        const notification = {
            user: { id: referralData.referredDoctor.id },
            title: 'New Patient Referral',
            message: `You have been referred a new patient: ${this.selectedPatient.user?.firstName} ${this.selectedPatient.user?.lastName}`,
            type: 'referral',
            isRead: false,
            createdAt: new Date().toISOString()
        };

        console.log('Creating notification:', notification);

        this.apiService.post('notifications', notification).subscribe({
            next: () => {
                console.log('Notification sent to referred doctor');
            },
            error: (err) => {
                console.error('Failed to send notification, trying alternative endpoint:', err);
                // Try alternative endpoint (without api prefix since ApiService already adds it)
                this.apiService.post('notifications', notification).subscribe({
                    next: () => {
                        console.log('Notification sent via alternative endpoint');
                    },
                    error: (err2) => {
                        console.error('Notification creation failed:', err2);
                    }
                });
            }
        });
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
