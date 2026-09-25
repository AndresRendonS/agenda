import {Component,EventEmitter,Input,Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {CARGOS,Person} from '../../core/models/event.models';
@Component({selector:'app-attendee-form',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="panel">
<h2>Personas que asistirán</h2><p>Registra entre una y cinco personas, de cualquier tipo de cargo.</p>
<div class="person" *ngFor="let p of people;let i=index">
 <div class="person-title"><b>Asistente {{i+1}}</b><button class="text-danger" (click)="remove(i)">Eliminar</button></div>
 <label>Tipo de cargo<select [(ngModel)]="p.role"><option *ngFor="let cargo of cargos" [value]="cargo">{{cargo}}</option></select></label>
 <div class="two">
 <label>Nombres<input [(ngModel)]="p.firstName" required maxlength="100"></label>
 <label>Apellidos<input [(ngModel)]="p.lastName" required maxlength="100"></label>
 <label>Celular<input [(ngModel)]="p.phone" type="tel" required pattern="[0-9+ ]{7,20}"></label>
 <label>Correo<input [(ngModel)]="p.email" type="email" required></label></div>
</div>
<button class="outline" [disabled]="people.length>=5" (click)="add()">+ Agregar asistente ({{people.length}}/5)</button>
<p *ngIf="error" class="notice">{{error}}</p>
<button class="primary full" [disabled]="!people.length" (click)="submit()">Continuar →</button>
</div>`})
export class AttendeeFormComponent {
 @Input() people:Person[]=[]; @Output() confirmed=new EventEmitter<Person[]>();
 cargos=CARGOS;error='';
 add(){if(this.people.length<5)this.people.push({role:'Jurídico',firstName:'',lastName:'',phone:'',email:''});}
 remove(i:number){this.people.splice(i,1);}
 submit(){const valid=this.people.length>0&&this.people.length<=5&&this.people.every(p=>p.firstName.trim()&&p.lastName.trim()&&/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(p.email)&&/^[0-9+ ]{7,20}$/.test(p.phone));if(!valid){this.error='Completa nombres, apellidos, celular y correo válido de cada asistente.';return;}this.error='';this.confirmed.emit(this.people);}
}
