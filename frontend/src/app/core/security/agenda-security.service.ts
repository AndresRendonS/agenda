import { Injectable } from '@angular/core';
import { PortalAccessControlService, PortalAuthSessionService } from '@neps/portal-security-angular';
import { catchError, firstValueFrom, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AgendaSecurityService {
  constructor(public readonly session: PortalAuthSessionService, public readonly access: PortalAccessControlService) {}

  async start(): Promise<void> {
    const refreshed = await firstValueFrom(this.session.bootstrap().pipe(catchError(() => of(false))));
    if (!refreshed) {
      const token = await this.waitForPortalSession();
      await firstValueFrom(this.session.bootstrapSession(token));
      this.ackPortal();
    }
    const allowed = await firstValueFrom(this.access.initialize(true));
    if (!allowed) throw new Error('El usuario no tiene acceso a AGENDA.');
  }

  providerId(): number | null { return this.access.currentProviderId(); }
  has(permission: string): boolean { return this.access.hasPermission(permission); }

  private waitForPortalSession(): Promise<string> {
    const q = new URLSearchParams(location.search);
    const applicationId = q.get('applicationId') || sessionStorage.getItem('neps.portal.applicationId');
    const portalOrigin = q.get('portalOrigin') || sessionStorage.getItem('neps.portal.origin');
    if (applicationId) sessionStorage.setItem('neps.portal.applicationId', applicationId);
    if (portalOrigin) sessionStorage.setItem('neps.portal.origin', portalOrigin);
    if (!window.opener || window.opener.closed || !applicationId || !portalOrigin)
      return Promise.reject(new Error('Agenda debe abrirse desde Portal Prestadores o tener una sesión vigente.'));

    return new Promise((resolve, reject) => {
      const timeout = window.setTimeout(() => { cleanup(); reject(new Error('Portal no entregó la sesión de Agenda.')); }, 12000);
      const cleanup = () => { clearTimeout(timeout); removeEventListener('message', onMessage); };
      const onMessage = (event: MessageEvent) => {
        if (event.origin !== portalOrigin || event.source !== window.opener) return;
        const m: any = event.data || {};
        if (m.type !== 'NEPS_PORTAL_SESSION' || String(m.applicationCode || '').toUpperCase() !== 'AGENDA' || String(m.applicationId) !== String(applicationId) || !m.accessToken) return;
        cleanup(); resolve(m.accessToken);
      };
      addEventListener('message', onMessage);
      window.opener.postMessage({type:'NEPS_MICROFRONT_READY', applicationId, applicationCode:'AGENDA'}, portalOrigin);
    });
  }

  private ackPortal(): void {
    const applicationId = sessionStorage.getItem('neps.portal.applicationId');
    const portalOrigin = sessionStorage.getItem('neps.portal.origin');
    if (window.opener && !window.opener.closed && applicationId && portalOrigin)
      window.opener.postMessage({type:'NEPS_MICROFRONT_SESSION_ACK', applicationId, applicationCode:'AGENDA'}, portalOrigin);
  }
}
