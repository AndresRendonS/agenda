export type UserProfile = 'PRESTADOR' | 'COLABORADOR' | 'ADMIN';
export type EventStatus = 'BORRADOR' | 'DISPONIBLE' | 'FINALIZADO' | 'CANCELADO';
export type Cargo = 'Jurídico' | 'Financiero' | 'Comercial' | 'Técnico' | 'Gestión Humana' | 'Representante legal';
export const CARGOS: Cargo[] = ['Jurídico','Financiero','Comercial','Técnico','Gestión Humana','Representante legal'];
export interface EventItem { id:number; name:string; description:string; start:string; end:string; status:EventStatus; responsible:string; slots:string[]; }
export interface Person { role:Cargo; firstName:string; lastName:string; phone:string; email:string; }
export interface Registration { provider:string; people:Person[]; slot:string; status:string; }
export interface Commitment { id:number; provider:string; role:Cargo; text:string; done:boolean; }
export interface Provider { code:string; name:string; type:'IPS'|'OPERADOR_FARMACEUTICO'; }
export interface Acta { provider:string; eventId:number; filename:string; status:string; updatedAt:string; }
