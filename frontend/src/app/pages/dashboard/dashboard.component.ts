import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule], // Added ReactiveFormsModule
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  currentUser: any = null;
  isAdmin = false;
  stats: any = {
    patients: 0,
    appointments: 0,
    doctors: 0
  };

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
    this.loadStats();
  }

  loadStats(): void {
    // 1. Load User list for Doctors/Admins count
    this.apiService.get('users').subscribe({
      next: (users: any[]) => {
        this.stats.doctors = users.filter(u => u.role === 'USER').length;

        // 2. Load Patients
        this.apiService.get('patients').subscribe({
          next: (patients: any[]) => {
            this.stats.patients = patients.length;
          },
          error: () => {
            // Fallback: count users with PATIENT role
            this.stats.patients = users.filter(u => u.role === 'PATIENT').length;
          }
        });

        // 3. Load Appointments
        this.apiService.get('appointments').subscribe({
          next: (appointments: any[]) => {
            this.stats.appointments = appointments.length;
          },
          error: (err) => console.error('Error loading appointments', err)
        });
      },
      error: (err: any) => console.error('Error loading users for stats', err)
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

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
