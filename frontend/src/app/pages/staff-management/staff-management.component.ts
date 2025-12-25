import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
  selector: 'app-staff-management',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './staff-management.component.html',
  styleUrl: './staff-management.component.scss'
})
export class StaffManagementComponent implements OnInit {
  currentUser: User | null = null;
  isAdmin = false;
  isSuperAdmin = false; // "nati"

  // Data
  staffList: any[] = []; // Admins and Doctors

  // Creation Form
  showCreateForm = false;
  staffForm: FormGroup;
  loading = false;
  error = '';
  success = '';

  // Password Change
  showPasswordModal = false;
  passwordForm: FormGroup;
  passwordLoading = false;

  constructor(
    private authService: AuthService,
    private apiService: ApiService, // Inject ApiService
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) {
    this.staffForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['atlas123', [Validators.required, Validators.minLength(6)]], // Default password
      role: ['USER', Validators.required], // Default to Doctor (USER)
      department: ['']
    });

    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.currentUser?.role === 'ADMIN';
    this.isSuperAdmin = this.currentUser?.username?.toLowerCase() === 'nati';

    this.loadStaff();

    // Check for query params to auto-open form
    this.route.queryParams.subscribe(params => {
      if (params['role']) {
        // Strict check: If user wants to create ADMIN but is NOT super admin, force USER
        if (params['role'] === 'ADMIN' && !this.isSuperAdmin) {
          this.staffForm.patchValue({ role: 'USER' });
        } else {
          this.staffForm.patchValue({ role: params['role'] });
        }
        this.showCreateForm = true;
      }
    });
  }

  loadStaff(): void {
    this.apiService.get('users').subscribe({
      next: (users: any[]) => {
        // Filter: Show Admins and Doctors (USER role), exclude Patients? 
        // The prompt says "can create an account for both new users and patients".
        // But this page defines "Staff". Usually Patients are separate. 
        // I'll filter for ADMIN and USER (Doctor).
        this.staffList = users.filter(u => u.role === 'ADMIN' || u.role === 'USER');
      },
      error: (err) => console.error('Failed to load staff', err)
    });
  }

  resetUserPassword(user: any): void {
    const newPass = prompt(`Enter new password for ${user.username}:`);
    if (!newPass) return;

    this.apiService.put(`users/${user.id}/password`, { newPassword: newPass }).subscribe({
      next: () => alert(`Password for ${user.username} updated successfully.`),
      error: (err) => alert('Failed to reset password.')
    });
  }

  viewStaffDetails(staff: any): void {
    alert(`Staff Details:\nName: ${staff.firstName} ${staff.lastName}\nUsername: ${staff.username}\nEmail: ${staff.email}\nRole: ${staff.role}\nStatus: ${staff.isActive ? 'Active' : 'Deactivated'}`);
  }

  deactivateStaff(staff: any): void {
    if (!confirm(`Are you sure you want to ${staff.isActive ? 'deactivate' : 'activate'} ${staff.firstName}?`)) return;

    const newStatus = !staff.isActive;
    this.apiService.put(`users/${staff.id}`, { isActive: newStatus }).subscribe({
      next: () => {
        staff.isActive = newStatus;
        alert(`Staff ${newStatus ? 'activated' : 'deactivated'} successfully.`);
      },
      error: (err) => alert('Failed to change status.')
    });
  }

  deleteStaff(staff: any): void {
    if (!confirm(`Are you sure you want to PERMANENTLY delete ${staff.firstName}? This cannot be undone.`)) return;

    this.apiService.delete(`users/${staff.id}`).subscribe({
      next: () => {
        this.staffList = this.staffList.filter(u => u.id !== staff.id);
        alert('Staff member deleted successfully.');
      },
      error: (err) => alert('Failed to delete staff member.')
    });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.error = '';
    this.success = '';
  }

  onSubmit(): void {
    if (this.staffForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.success = '';

    // Use adminRegister to avoid auto-login
    this.authService.adminRegister(this.staffForm.value).subscribe({
      next: (res) => {
        this.loading = false;
        this.success = `Successfully created staff member: ${res.username}`;
        this.staffForm.reset({ role: 'USER' });
        this.loadStaff(); // Refresh list immediately
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to create staff member.';
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

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
