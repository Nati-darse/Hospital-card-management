import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.models';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-patient-portal',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './patient-portal.component.html',
    styleUrl: './patient-portal.component.scss'
})
export class PatientPortalComponent implements OnInit {
    currentUser: User | null = null;
    patientProfile: any = null;
    card: any = null;
    visits: any[] = [];

    constructor(
        private authService: AuthService,
        private apiService: ApiService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        if (this.currentUser) {
            this.loadPatientData();
        }
    }

    loadPatientData(): void {
        if (!this.currentUser) return;

        // 1. Load Profile
        this.apiService.get(`patients/user/${this.currentUser.id}`).subscribe({
            next: (profile) => {
                this.patientProfile = profile;
                // 2. Load Card
                this.apiService.get(`cards`).subscribe({
                    next: (cards: any[]) => {
                        this.card = cards.find(c => c.patientId === profile.id);
                    }
                });
                // 3. Load Visits
                this.apiService.get(`visits/patient/${profile.id}`).subscribe({
                    next: (visits) => this.visits = (visits as any[]).reverse()
                });
            },
            error: (err) => console.error('Error loading patient data', err)
        });
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
