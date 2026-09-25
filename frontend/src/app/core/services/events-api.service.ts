import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Acta, Commitment, EventItem, Person, Provider, Registration, Cargo, EventStatus } from '../models/event.models';
@Injectable({providedIn:'root'})
export class EventsApiService {
  readonly baseUrl='http://localhost:8080/api';
  constructor(private http:HttpClient){}
  events(){return this.http.get<EventItem[]>(this.baseUrl+'/events');}
  createEvent(payload:Partial<EventItem>&{slotMinutes:number}){return this.http.post<EventItem>(this.baseUrl+'/events',payload);}
  registrations(eventId:number,provider:string){return this.http.get<Registration[]>(this.baseUrl+'/events/'+eventId+'/registrations',{params:{provider}});}
  register(eventId:number,provider:string,people:Person[],slot:string){return this.http.post<Registration>(this.baseUrl+'/events/'+eventId+'/registrations',{provider,people,slot});}
  commitments(eventId:number,provider:string){return this.http.get<Commitment[]>(this.baseUrl+'/events/'+eventId+'/commitments',{params:{provider}});}
  addCommitment(eventId:number,provider:string,role:Cargo,text:string){return this.http.post<Commitment>(this.baseUrl+'/events/'+eventId+'/commitments',{provider,role,text});}
  finalize(eventId:number,provider:string){return this.http.post(this.baseUrl+'/events/'+eventId+'/commitments/finalize',{provider});}
  providers(){return this.http.get<Provider[]>(this.baseUrl+'/workflow/providers');}
  assignments(collaborator:string){return this.http.get<string[]>(this.baseUrl+'/workflow/assignments',{params:{collaborator}});}
  assign(collaborator:string,provider:string){return this.http.post(this.baseUrl+'/workflow/assignments',{collaborator,provider});}
  attendance(eventId:number,provider:string){return this.http.get<Record<string,boolean>>(this.baseUrl+'/workflow/events/'+eventId+'/attendance',{params:{provider}});}
  markAttendance(eventId:number,provider:string,email:string,attended:boolean){return this.http.put(this.baseUrl+'/workflow/events/'+eventId+'/attendance',{eventId,provider,email,attended});}
  acta(eventId:number,provider:string){return this.http.get<Acta>(this.baseUrl+'/workflow/events/'+eventId+'/actas',{params:{provider}});}
  signActa(eventId:number,provider:string){return this.http.post<Acta>(this.baseUrl+'/workflow/events/'+eventId+'/actas/sign',{provider});}
  status(eventId:number){return this.http.get<{status:EventStatus}>(this.baseUrl+'/workflow/events/'+eventId+'/status');}
  setStatus(eventId:number,status:EventStatus){return this.http.put(this.baseUrl+'/workflow/events/'+eventId+'/status',{status});}
  outbox(){return this.http.get<Array<Record<string,string>>>(this.baseUrl+'/mock/outbox');}
}
