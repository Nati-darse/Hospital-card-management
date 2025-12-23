import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
    selector: 'app-doctor-portal',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule],
    templateUrl: './doctor-portal.component.html',
    styleUrl: './doctor-portal.component.scss'
})
export class DoctorPortalComponent implements OnInit {
    currentUser: User | null = null;
    staffProfile: any = null;
    patients: any[] = [];
    selectedPatient: any = null;

    prescriptionForm: FormGroup;
    loading = false;

    constructor(
        private authService: AuthService,
        private apiService: ApiService,
        private router: Router,
        private fb: FormBuilder
    ) {
        this.prescriptionForm = this.fb.group({
            patientId: ['', [Validators.required]],
            diagnosis: ['', [Validators.required]],
            prescriptions: ['', [Validators.required]],
            labTests: [''],
            followUpDate: ['']
        });
    }

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        if (this.currentUser) {
            this.loadDoctorData();
        }
    }

    loadDoctorData(): void {
        if (!this.currentUser) return;

        // 1. Get Staff record for this User
        this.apiService.get('staff').subscribe({
            next: (staffList: any[]) => {
                this.staffProfile = staffList.find(s => s.user?.id === this.currentUser?.id);
                if (this.staffProfile) {
                    // 2. Load assigned patients
                    this.apiService.get(`patients?doctorId=${this.staffProfile.id}`).subscribe({
                        next: (patients) => this.patients = patients
                    });
                }
            }
        });
    }

    selectPatient(patient: any): void {
        this.selectedPatient = patient;
        this.prescriptionForm.patchValue({ patientId: patient.id });
    }

    submitPrescription(): void {
        if (this.prescriptionForm.invalid) return;

        this.loading = true;
        const val = {
            ...this.prescriptionForm.value,
            doctor: { id: this.staffProfile.id },
            patient: { id: this.prescriptionForm.value.patientId },
            visitDate: new Date().toISOString().split('T')[0]
        };

        this.apiService.post('visits', val).subscribe({
            next: () => {
                this.loading = false;
                this.prescriptionForm.reset();
                this.selectedPatient = null;
                alert('Prescription saved successfully!');
            },
            error: (err) => {
                this.loading = false;
                console.error('Error saving visit', err);
            }
        });
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
