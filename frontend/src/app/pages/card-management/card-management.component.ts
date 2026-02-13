import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.models';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-card-management',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './card-management.component.html',
  styleUrl: './card-management.component.scss'
})
export class CardManagementComponent implements OnInit {
  currentUser: User | null = null;
  isAdmin = false;
  cards: any[] = [];
  selectedCard: any = null;
  doctors: any[] = [];
  showReassignModal = false;
  reassignForm: FormGroup;
  selectedPatientForReassign: any = null;
  loading = false;

  // Mobile Menu
  mobileMenuOpen = false;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.reassignForm = this.fb.group({
      doctorId: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.currentUser?.role === 'ADMIN';
    this.loadCards();
  }

  loadCards(): void {
    // Load all card-holding patients with assignment status
    this.apiService.get('cards/assigned-patients').subscribe({
      next: (data: any) => {
        this.cards = data;
        const previousSelectedId = this.selectedCard?.id;
        const nextSelected = data?.find((c: any) => c.id === previousSelectedId) || data?.[0] || null;
        this.selectedCard = nextSelected;
        console.log('Assigned patients loaded:', data);
      },
      error: (err) => console.error('Error loading assigned patients', err)
    });
  }

  loadDoctors(): void {
    this.apiService.get('cards/available-doctors').subscribe({
      next: (data: any) => {
        this.doctors = data;
        console.log('Available doctors loaded:', data);
      },
      error: (err) => console.error('Error loading doctors', err)
    });
  }

  openReassignModal(patient: any): void {
    this.selectedPatientForReassign = patient;
    this.showReassignModal = true;
    this.loadDoctors();
    this.reassignForm.reset();
  }

  closeReassignModal(): void {
    this.showReassignModal = false;
    this.selectedPatientForReassign = null;
    this.reassignForm.reset();
  }

  reassignPatient(): void {
    if (this.reassignForm.invalid || !this.selectedPatientForReassign) return;
    
    this.loading = true;
    const doctorId = this.reassignForm.get('doctorId')?.value;
    
    this.apiService.post('cards/reassign-patient', {
      patientId: this.selectedPatientForReassign.id,
      doctorId: doctorId
    }).subscribe({
      next: (response: any) => {
        this.loading = false;
        alert(response.message || 'Patient reassigned successfully');
        this.closeReassignModal();
        this.loadCards(); // Refresh the list
      },
      error: (err) => {
        this.loading = false;
        alert('Failed to reassign patient: ' + (err.error?.error || err.message || 'Unknown error'));
      }
    });
  }

  unassignPatient(card: any): void {
    if (!card?.id) return;
    if (!confirm(`Detach patient from current doctor for card ${card.cardNumber}?`)) return;

    this.loading = true;
    this.apiService.post('cards/unassign-patient', { patientId: card.id }).subscribe({
      next: (response: any) => {
        this.loading = false;
        alert(response.message || 'Patient detached successfully.');
        if (this.selectedCard?.id === card.id) {
          this.selectedCard = { ...this.selectedCard, assignedDoctor: 'Unassigned', assignedDoctorId: null, department: null, status: 'Awaiting Reassignment' };
        }
        this.loadCards();
      },
      error: (err) => {
        this.loading = false;
        alert('Failed to detach patient: ' + (err.error?.error || err.message || 'Unknown error'));
      }
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  viewCardDetails(card: any): void {
    this.selectedCard = card;
  }

  closeDetail(): void {
    this.selectedCard = null;
  }

  deleteCard(card: any): void {
    if (!confirm('Are you sure you want to delete this card?')) return;
    this.apiService.delete(`cards/${card.id}`).subscribe({
      next: () => {
        this.cards = this.cards.filter(c => c.id !== card.id);
        alert('Card deleted successfully.');
      },
      error: (err) => alert('Failed to delete card.')
    });
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }
}
