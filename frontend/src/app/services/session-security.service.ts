import { Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { fromEvent, merge, Subscription } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class SessionSecurityService implements OnDestroy {
    private readonly IDLE_TIMEOUT_MS = 10 * 60 * 1000;
    private activitySub?: Subscription;
    private idleTimer: any = null;
    private started = false;

    constructor(private authService: AuthService, private router: Router) { }

    start(): void {
        if (this.started) {
            return;
        }
        this.started = true;

        const activity$ = merge(
            fromEvent(document, 'mousemove'),
            fromEvent(document, 'mousedown'),
            fromEvent(document, 'keydown'),
            fromEvent(document, 'scroll'),
            fromEvent(document, 'touchstart')
        );

        this.activitySub = activity$.subscribe(() => this.resetIdleTimer());
        this.resetIdleTimer();
    }

    private resetIdleTimer(): void {
        if (this.idleTimer) {
            clearTimeout(this.idleTimer);
        }

        this.idleTimer = setTimeout(() => {
            if (this.authService.isAuthenticated()) {
                this.authService.logout();
                this.router.navigate(['/login'], {
                    queryParams: { reason: 'session-timeout' }
                });
            }
        }, this.IDLE_TIMEOUT_MS);
    }

    ngOnDestroy(): void {
        if (this.activitySub) {
            this.activitySub.unsubscribe();
        }
        if (this.idleTimer) {
            clearTimeout(this.idleTimer);
        }
    }
}
