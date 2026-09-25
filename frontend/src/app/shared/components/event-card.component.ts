import {Component,EventEmitter,Input,Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EventItem} from '../../core/models/event.models';
@Component({selector:'app-event-card',standalone:true,imports:[CommonModule],template:`
<article class="event-card">
  <div class="card-top"><span class="pill">● {{event.status}}</span><span class="event-number">#{{event.id}}</span></div>
  <div class="event-symbol">▦</div><h3>{{event.name}}</h3><p>{{event.description}}</p>
  <div class="meta">◷ &nbsp; {{event.start | date:'dd MMM yyyy, HH:mm'}}<br>♙ &nbsp; {{event.responsible}}</div>
  <button class="outline" (click)="open.emit(event)">Ver detalles →</button>
</article>`})
export class EventCardComponent { @Input({required:true}) event!:EventItem; @Output() open=new EventEmitter<EventItem>(); }
