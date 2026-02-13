import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SessionSecurityService } from './services/session-security.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  constructor(private sessionSecurityService: SessionSecurityService) {}

  ngOnInit(): void {
    this.sessionSecurityService.start();
  }

  protected readonly title = signal('frontend');
}
