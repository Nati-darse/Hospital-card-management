import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.models';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-card-management',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './card-management.component.html',
  styleUrl: './card-management.component.scss'
})
export class CardManagementComponent implements OnInit {
  currentUser: User | null = null;
  isAdmin = false;
  cards: any[] = [];
  selectedCard: any = null;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.currentUser?.role === 'ADMIN';
    this.loadCards();
  }

  loadCards(): void {
    this.apiService.get('cards').subscribe({
      next: (data: any) => this.cards = data,
      error: (err) => console.error('Error loading cards', err)
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
}
