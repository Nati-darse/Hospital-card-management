import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.models';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './patients.component.html',
  styleUrl: './patients.scss'
})
export class PatientsComponent implements OnInit {
  currentUser: User | null = null;
  isAdmin = false;
  patients: any[] = [];
  doctors: any[] = [];
  loading = false;
  assigningId: number | null = null;

  // Password Change
  showPasswordModal = false;
  passwordForm: FormGroup;
  passwordLoading = false;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.currentUser?.role === 'ADMIN';
    this.loadPatients();
    this.loadDoctors();
  }

  loadDoctors(): void {
    this.apiService.get('staff').subscribe({
      next: (data: any[]) => {
        // Filter for active doctors
        this.doctors = data.filter(d => d.user?.isActive);
      },
      error: (err) => console.error('Error loading staff', err)
    });
  }

  loadPatients(): void {
    this.loading = true;
    this.apiService.get('patients').subscribe({
      next: (data: any) => {
        this.patients = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading patients', err);
        this.loading = false;
      }
    });
  }

  togglePasswordModal(): void {
    this.showPasswordModal = !this.showPasswordModal;
    if (!this.showPasswordModal) this.passwordForm.reset();
  }

  changePassword(): void {
    if (this.passwordForm.invalid || !this.currentUser) return;

    this.passwordLoading = true;
    this.apiService.put(`users/${this.currentUser.id}/password`, this.passwordForm.value).subscribe({
      next: () => {
        this.passwordLoading = false;
        alert('Password changed successfully. Please login again.');
        this.authService.logout();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.passwordLoading = false;
        console.error('Password change failed', err);
        alert('Failed to change password.');
      }
    });
  }

  viewPatient(patient: any): void {
    alert(`Patient: ${patient.user?.firstName} ${patient.user?.lastName}\nMRN: ${patient.medicalRecordNumber}\nGender: ${patient.user?.gender}\nDOB: ${patient.user?.dateOfBirth}\nAddress: ${patient.user?.address}\nDoctor: Dr. ${patient.assignedDoctor?.user?.lastName || 'N/A'}`);
  }

  appointDoctor(patient: any, doctorId: string): void {
    if (!doctorId) return;
    this.loading = true;
    const payload = { ...patient, assignedDoctorId: parseInt(doctorId) };
    this.apiService.put(`patients/${patient.id}`, payload).subscribe({
      next: () => {
        this.loading = false;
        this.assigningId = null;
        alert('Doctor assigned successfully.');
        this.loadPatients();
      },
      error: (err) => {
        this.loading = false;
        alert('Failed to assign doctor.');
      }
    });
  }

  editPatient(patient: any): void {
    this.router.navigate(['/patient-registration'], { queryParams: { editId: patient.id } });
  }

  deletePatient(patient: any): void {
    if (!confirm('Are you sure you want to delete this patient record?')) return;
    this.apiService.delete(`patients/${patient.id}`).subscribe({
      next: () => {
        this.patients = this.patients.filter(p => p.id !== patient.id);
        alert('Patient record deleted successfully.');
      },
      error: (err) => alert('Failed to delete patient.')
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
