import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.models';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './patients.component.html',
  styleUrl: './patients.scss'
})
export class PatientsComponent implements OnInit {
  currentUser: User | null = null;
  isAdmin = false;
  patients: any[] = [];
  filteredPatients: any[] = [];
  searchQuery = '';
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

  // Mobile Menu
  mobileMenuOpen = false;

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
        this.filteredPatients = data;
        this.onSearch();
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

  onSearch(): void {
    const query = (this.searchQuery || '').toLowerCase().trim();
    if (!query) {
      this.filteredPatients = [...this.patients];
      return;
    }

    this.filteredPatients = this.patients.filter((p: any) => {
      const fullName = `${p.user?.firstName || ''} ${p.user?.lastName || ''}`.toLowerCase();
      const mrn = (p.medicalRecordNumber || '').toLowerCase();
      const email = (p.user?.email || '').toLowerCase();
      const phone = (p.user?.phoneNumber || '').toLowerCase();
      return fullName.includes(query) || mrn.includes(query) || email.includes(query) || phone.includes(query);
    });
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

  resetPasswordToDefault(patient: any): void {
    if (!patient?.user?.id) return;
    
    if (confirm(`Are you sure you want to reset password for "${patient.user.username}" to default password "Atlas@123"?`)) {
      console.log('Resetting password to default for patient:', patient.user.username);
      
      this.apiService.post(`users/${patient.user.id}/reset-password`, {}).subscribe({
        next: (response: any) => {
          console.log('Password reset response:', response);
          alert(response.message || `Password for "${patient.user.username}" has been reset to: Atlas@123`);
        },
        error: (err) => {
          console.error('Password reset failed:', err);
          alert('Failed to reset password. Error: ' + (err.error?.message || err.message || 'Unknown error'));
        }
      });
    }
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.detailPatient = null;
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }
}
