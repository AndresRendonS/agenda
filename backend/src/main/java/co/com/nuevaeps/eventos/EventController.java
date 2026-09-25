package co.com.nuevaeps.eventos;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
@RestController @RequestMapping("/api") @CrossOrigin(origins="http://localhost:4200")
public class EventController {
 public record Event(Long id,String name,String description,String start,String end,String status,String responsible,List<String> slots){}
 public record Person(String role,String firstName,String lastName,String phone,String email){}
 public record Registration(String provider,List<Person> people,String slot,String status){}
 public record Commitment(Long id,String provider,String role,String text,boolean done){}
 public record RegisterRequest(String provider,List<Person> people,String slot){}
 public record CommitmentRequest(String provider,String role,String text){}
 public record CreateEvent(String name,String description,String start,String end,String responsible,Integer slotMinutes,List<String> slots){}
 private final AtomicLong ids=new AtomicLong(3),commitmentIds=new AtomicLong();
 private final Map<Long,Event> events=new ConcurrentHashMap<>();
 private final Map<Long,List<Registration>> registrations=new ConcurrentHashMap<>();
 private final Map<Long,List<Commitment>> commitments=new ConcurrentHashMap<>();
 private final Map<String,String> actas=new ConcurrentHashMap<>();
 private final Map<String,String> closure=new ConcurrentHashMap<>();
 private final List<Map<String,String>> emailOutbox=new CopyOnWriteArrayList<>();
 private final Set<String> allowed=Set.of("Jurídico","Financiero","Comercial","Técnico","Gestión Humana","Representante legal");
 public EventController(){events.put(1L,new Event(1L,"Mesa de acuerdos y compromisos","Espacio para construir acuerdos entre prestadores y Nueva EPS.","2026-10-05T08:00:00","2026-10-05T17:00:00","DISPONIBLE","Laura Martínez",List.of("2026-10-05T09:00:00","2026-10-05T09:30:00","2026-10-05T10:00:00")));events.put(2L,new Event(2L,"Encuentro de gestión farmacéutica","Seguimiento de procesos con operadores farmacéuticos.","2026-10-12T08:00:00","2026-10-12T16:00:00","DISPONIBLE","Carlos Rojas",List.of("2026-10-12T08:30:00","2026-10-12T09:00:00")));events.put(3L,new Event(3L,"Jornada de relacionamiento IPS","Revisión de oportunidades y compromisos de atención.","2026-10-19T08:00:00","2026-10-19T17:00:00","DISPONIBLE","Diana Pérez",List.of("2026-10-19T11:00:00","2026-10-19T11:30:00")));}
 @GetMapping("/events") public List<Event> list(){return events.values().stream().sorted(Comparator.comparing(Event::id)).toList();}
 @PostMapping("/events") public Event create(@RequestBody CreateEvent r){if(blank(r.name())||blank(r.start())||blank(r.end())||blank(r.responsible()))bad("Nombre, fechas y responsable son obligatorios");LocalDateTime start=parse(r.start()),end=parse(r.end());if(!end.isAfter(start))bad("La fecha final debe ser posterior al inicio");int minutes=r.slotMinutes()==null?30:r.slotMinutes();if(minutes<30||minutes%30!=0)bad("La franja mínima es de 30 minutos");List<String> slots=r.slots()==null?List.of():r.slots();if(new HashSet<>(slots).size()!=slots.size())bad("Franjas duplicadas");for(String slot:slots){LocalDateTime s=parse(slot);if(s.isBefore(start)||s.plusMinutes(minutes).isAfter(end))bad("Las franjas deben estar dentro de la vigencia");}long id=ids.incrementAndGet();Event e=new Event(id,r.name(),r.description(),r.start(),r.end(),"DISPONIBLE",r.responsible(),slots);events.put(id,e);return e;}
 @GetMapping("/events/{id}") public Event event(@PathVariable Long id){return require(id);}
 @GetMapping("/events/{id}/registrations") public List<Registration> registrations(@PathVariable Long id,@RequestParam String provider){require(id);return registrations.getOrDefault(id,List.of()).stream().filter(r->r.provider().equals(provider)).toList();}
 @PostMapping("/events/{id}/registrations") public Registration register(@PathVariable Long id,@RequestBody RegisterRequest r){Event e=require(id);if(blank(r.provider())||r.people()==null||r.people().isEmpty()||r.people().size()>5)bad("Prestador y entre 1 y 5 asistentes requeridos");for(Person p:r.people()){if(!allowed.contains(p.role())||blank(p.firstName())||blank(p.lastName())||blank(p.phone())||blank(p.email())||!p.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))bad("Datos de asistente inválidos");}if(!"DISPONIBLE".equals(e.status())||LocalDateTime.now().isAfter(parse(e.end())))bad("Evento no disponible");if(!e.slots().isEmpty()&&!e.slots().contains(r.slot()))bad("Selecciona una franja válida");if(registrations.getOrDefault(id,List.of()).stream().anyMatch(existing->existing.provider().equals(r.provider())))bad("El prestador ya está inscrito");Registration reg=new Registration(r.provider(),List.copyOf(r.people()),r.slot(),"CONFIRMADA");registrations.computeIfAbsent(id,k->new CopyOnWriteArrayList<>()).add(reg);for(Person person:r.people())emailOutbox.add(Map.of("type","CONFIRMATION","email",person.email(),"event",e.name(),"slot",Objects.toString(r.slot(),"")));return reg;}
 @GetMapping("/events/{id}/commitments") public List<Commitment> commitments(@PathVariable Long id,@RequestParam String provider){require(id);return commitments.getOrDefault(id,List.of()).stream().filter(c->c.provider().equals(provider)).toList();}
 @PostMapping("/events/{id}/commitments") public Commitment commitment(@PathVariable Long id,@RequestBody CommitmentRequest r){require(id);if(blank(r.provider())||!allowed.contains(r.role())||blank(r.text()))bad("Prestador, cargo y descripción requeridos");List<Commitment> list=commitments.computeIfAbsent(id,k->new CopyOnWriteArrayList<>());if("FINALIZED".equals(closure.get(id+":"+r.provider())))bad("Compromisos ya finalizados");if(LocalDateTime.now().isBefore(parse(require(id).end())))bad("El evento aún no ha terminado");if(list.stream().filter(c->c.provider().equals(r.provider())).count()>=50)bad("Máximo 50 compromisos");Commitment c=new Commitment(commitmentIds.incrementAndGet(),r.provider(),r.role(),r.text(),false);list.add(c);return c;}
 @PostMapping("/events/{id}/commitments/finalize") public Map<String,String> finalizeCommitments(@PathVariable Long id,@RequestBody Map<String,String> request){require(id);String provider=request.getOrDefault("provider","");if(blank(provider))bad("Prestador obligatorio");closure.put(id+":"+provider,"FINALIZED");return Map.of("status","FINALIZED","provider",provider);}
 @PostMapping("/events/{id}/acta") public Map<String,String> acta(@PathVariable Long id,@RequestParam String provider,@RequestParam MultipartFile file){require(id);if(blank(provider)||file.isEmpty()||file.getOriginalFilename()==null||!file.getOriginalFilename().toLowerCase().endsWith(".pdf"))bad("Adjunta un PDF válido");if(!"FINALIZED".equals(closure.get(id+":"+provider)))bad("Finaliza compromisos antes de adjuntar acta");try {byte[] bytes=file.getBytes();if(bytes.length<5||bytes[0]!=37||bytes[1]!=80||bytes[2]!=68||bytes[3]!=70||bytes[4]!=45)bad("El archivo no es PDF");if(bytes.length>5_000_000)bad("PDF supera 5 MB");Path dir=Path.of(System.getProperty("java.io.tmpdir"),"nuevaeps-actas-mock");Files.createDirectories(dir);Files.write(dir.resolve(UUID.randomUUID()+".pdf"),bytes,StandardOpenOption.CREATE_NEW);}catch(IOException ex){throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error almacenando acta");}actas.put(id+":"+provider,file.getOriginalFilename());for(Registration reg:registrations.getOrDefault(id,List.of()))if(reg.provider().equals(provider))for(Person person:reg.people())emailOutbox.add(Map.of("type","ACTA","email",person.email(),"event",require(id).name(),"filename",file.getOriginalFilename()));return Map.of("filename",file.getOriginalFilename(),"status","PENDING_PROVIDER_SIGNATURE");}
 @GetMapping("/events/{id}/acta") public Map<String,String> actaStatus(@PathVariable Long id,@RequestParam String provider){require(id);String name=actas.get(id+":"+provider);return name==null?Map.of("status","NOT_UPLOADED"):Map.of("filename",name,"status","PENDING_PROVIDER_SIGNATURE");}
 @PostMapping("/mock/reminders") public Map<String,Object> reminders(){int count=sendReminders();return Map.of("emailsSimulated",count);}
 @Scheduled(cron="${events.reminder.cron:0 0 8 * * *}",zone="${events.reminder.zone:America/Bogota}") public void scheduledReminders(){sendReminders();}
 private int sendReminders(){int count=0;for(var entry:registrations.entrySet()){Event event=events.get(entry.getKey());if(event==null||LocalDateTime.now().isAfter(parse(event.end())))continue;for(Registration reg:entry.getValue())for(Person p:reg.people()){emailOutbox.add(Map.of("type","REMINDER","email",p.email(),"event",event.name(),"slot",Objects.toString(reg.slot(),"")));count++;}}return count;}
 @GetMapping("/mock/outbox") public List<Map<String,String>> outbox(){return List.copyOf(emailOutbox);}
 private Event require(Long id){Event e=events.get(id);if(e==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no encontrado");return e;}
 private static boolean blank(String s){return s==null||s.isBlank();}
 private static LocalDateTime parse(String s){try{return LocalDateTime.parse(s);}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Fecha ISO inválida: "+s);}}
 private static void bad(String s){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,s);}
}