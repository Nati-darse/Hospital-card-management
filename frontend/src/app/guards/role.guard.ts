import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.getCurrentUser();
    const expectedRoles = route.data['roles'] as string[];

    if (!user) {
        // Not logged in at all
        router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
        return false;
    }

    if (expectedRoles && expectedRoles.includes(user.role)) {
        // Role matches
        return true;
    }

    // Logged in but unauthorized role
    console.warn(`Access denied for user ${user.username} (${user.role}) to ${state.url}`);

    // Redirect to their appropriate dashboard to avoid getting stuck
    if (user.role === 'ADMIN') {
        router.navigate(['/dashboard']);
    } else if (user.role === 'USER') { // Doctor
        router.navigate(['/doctor-portal']);
    } else if (user.role === 'PATIENT') {
        router.navigate(['/patient-portal']);
    } else {
        router.navigate(['/login']);
    }

    return false;
};
