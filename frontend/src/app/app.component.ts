import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
interface EventItem {id:number;name:string;description:string;start:string;end:string;status:string;responsible:string;slots:string[];}
interface Person {role:string;firstName:string;lastName:string;phone:string;email:string;}
interface Commitment {id:number;role:string;text:string;done:boolean;}
@Component({selector:'app-root',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./app.component.html'})
export class AppComponent implements OnInit {
 api='http://localhost:8080/api'; profile='PRESTADOR'; provider='IPS-110010001'; view='events'; events:EventItem[]=[]; selected?:EventItem;
 roles=['Jurídico','Financiero','Comercial','Técnico','Gestión Humana']; people:Person[]=[]; slot=''; notice=''; registrations:any[]=[]; commitments:Commitment[]=[]; commitmentRole='Jurídico'; commitmentText=''; actaName=''; assignedProvider='IPS-110010001';
 draft={name:'',description:'',start:'',end:'',responsible:'',slotMinutes:30,slotsText:''};
 constructor(private http:HttpClient){}
 ngOnInit(){this.load();}
 load(){this.http.get<EventItem[]>(this.api+'/events').subscribe({next:x=>this.events=x,error:()=>this.notice='No se pudo conectar con el backend. Inicia Spring Boot en el puerto 8080.'});}
 select(e:EventItem){this.selected=e;this.view='detail';this.slot=e.slots[0]||'';this.refreshDetails();}
 refreshDetails(){if(!this.selected)return;this.http.get<any[]>(this.api+'/events/'+this.selected.id+'/registrations?provider='+encodeURIComponent(this.provider)).subscribe(x=>this.registrations=x);this.http.get<Commitment[]>(this.api+'/events/'+this.selected.id+'/commitments?provider='+encodeURIComponent(this.provider)).subscribe(x=>this.commitments=x);}
 addPerson(){if(this.people.length<5)this.people.push({role:'Jurídico',firstName:'',lastName:'',phone:'',email:''});}
 removePerson(i:number){this.people.splice(i,1);}
 register(){if(!this.selected||!this.people.length)return;this.http.post(this.api+'/events/'+this.selected.id+'/registrations',{provider:this.provider,people:this.people,slot:this.slot}).subscribe({next:()=>{this.notice='Inscripción registrada. Correos simulados en el backend.';this.people=[];this.refreshDetails();},error:e=>this.notice=e.error?.message||'No fue posible confirmar.'});}
 addCommitment(){if(!this.selected||!this.commitmentText.trim())return;this.http.post(this.api+'/events/'+this.selected.id+'/commitments',{provider:this.provider,role:this.commitmentRole,text:this.commitmentText}).subscribe({next:()=>{this.commitmentText='';this.refreshDetails();},error:e=>this.notice=e.error?.message||'Error al guardar'});}
 closeCommitments(){if(!this.selected)return;this.http.post(this.api+'/events/'+this.selected.id+'/commitments/finalize',{provider:this.provider}).subscribe(()=>this.notice='Compromisos finalizados (mock).');}
 uploadActa(input:HTMLInputElement){const file=input.files?.[0];if(!file||!this.selected)return;const fd=new FormData();fd.append('file',file);fd.append('provider',this.provider);this.http.post<any>(this.api+'/events/'+this.selected.id+'/acta',fd).subscribe({next:x=>{this.actaName=x.filename;this.notice='Acta recibida. Firma del prestador pendiente (mock).';},error:()=>this.notice='Error al cargar el acta'});}
 createEvent(){this.http.post(this.api+'/events',{...this.draft,slots:this.draft.slotsText.split('\n').map(s=>s.trim()).filter(Boolean)}).subscribe({next:()=>{this.notice='Evento creado';this.view='events';this.load();},error:e=>this.notice=e.error?.message||'Error creando evento'});}
 setProfile(p:string){this.profile=p;this.view='events';this.selected=undefined;this.notice='Perfil de demostración: '+p;}
 get canManage(){return this.profile==='COLABORADOR'||this.profile==='ADMIN';}
 get isAdmin(){return this.profile==='ADMIN';}
}