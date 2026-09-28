import { provideAppInitializer, inject } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { providePortalSecurity, portalSecurityInterceptor } from '@neps/portal-security-angular';
import { AppComponent } from './app/app.component';
import { AgendaSecurityService } from './app/core/security/agenda-security.service';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptors([portalSecurityInterceptor])),
    providePortalSecurity({
      applicationCode: 'AGENDA',
      authBaseUrl: '/auth',
      accessControlBaseUrl: '/access-api/api/v1',
      apiBaseUrl: '/api',
      protectedUrlPrefixes: ['/api', '/access-api'],
      mode: 'SHADOW',
      tokenProfile: 'LEGACY_V1',
      unavailablePolicy: 'DENY',
      bootstrapOnStart: false,
      preferProviderScope: true
    }),
    provideAppInitializer(() => inject(AgendaSecurityService).start())
  ]
}).catch(error => console.error('No fue posible iniciar Agenda:', error));
