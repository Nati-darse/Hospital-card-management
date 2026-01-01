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
  showDetailModal = false;
  detailPatient: any = null;

  // Password Change
  showPasswordModal = false;
  passwordForm: FormGroup;
  passwordLoading = false;
  passwordError = '';
  passwordSuccess = '';

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'mismatch': true };
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
    this.passwordError = '';
    this.passwordSuccess = '';
    if (!this.showPasswordModal) this.passwordForm.reset();
  }

  changePassword(): void {
    if (this.passwordForm.invalid || !this.currentUser) return;

    this.passwordLoading = true;
    this.passwordError = '';
    this.passwordSuccess = '';

    this.apiService.post('users/change-password', {
      currentPassword: this.passwordForm.value.currentPassword,
      newPassword: this.passwordForm.value.newPassword
    }).subscribe({
      next: () => {
        this.passwordLoading = false;
        this.passwordSuccess = 'Password updated successfully! Redirecting to login...';
        setTimeout(() => {
          this.authService.logout();
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.passwordLoading = false;
        this.passwordError = err.error?.message || 'Failed to change password. Please ensure your current password is correct.';
      }
    });
  }

  viewPatient(patient: any): void {
    this.detailPatient = patient;
    this.showDetailModal = true;
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
    if (!this.isAdmin) return;
    if (!confirm('Are you sure you want to delete this patient record?')) return;
    this.apiService.delete(`patients/${patient.id}`).subscribe({
      next: () => {
        this.patients = this.patients.filter(p => p.id !== patient.id);
        alert('Patient record deleted successfully.');
      },
      error: (err) => alert('Failed to delete patient.')
    });
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.detailPatient = null;
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
