package co.com.nuevaeps.eventos;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Complementary in-memory workflow API. Replace with persisted, authorized services before production. */
@RestController
@RequestMapping("/api/workflow")
@CrossOrigin(origins = "http://localhost:4200")
public class WorkflowController {
  public record Provider(String code, String name, String type) {}
  public record Assignment(String collaborator, String provider) {}
  public record Attendance(String provider, Long eventId, String email, boolean attended) {}
  public record Acta(String provider, Long eventId, String filename, String status, String updatedAt) {}
  private final List<Provider> providers = List.of(
      new Provider("IPS-110010001", "IPS Salud Integral", "IPS"),
      new Provider("FAR-110010002", "Operador Farmacéutico Central", "OPERADOR_FARMACEUTICO"));
  private final Map<String, Set<String>> assignments = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Boolean>> attendance = new ConcurrentHashMap<>();
  private final Map<String, Acta> actas = new ConcurrentHashMap<>();
  private final Map<String, String> eventStates = new ConcurrentHashMap<>();
  private final List<Map<String, String>> notificationLog = Collections.synchronizedList(new ArrayList<>());
  public WorkflowController() {
    assignments.put("colaborador.demo@nuevaeps.com", ConcurrentHashMap.newKeySet());
    assignments.get("colaborador.demo@nuevaeps.com").add("IPS-110010001");
  }
  private String key(Long id, String provider) { return id + ":" + provider; }
  private void requireProvider(String code) {
    if (providers.stream().noneMatch(p -> p.code().equals(code)))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prestador no registrado");
  }
  @GetMapping("/providers") public List<Provider> providers() { return providers; }
  @GetMapping("/assignments") public Set<String> assignments(@RequestParam String collaborator) {
    return Set.copyOf(assignments.getOrDefault(collaborator, Set.of()));
  }
  @PostMapping("/assignments") public Assignment assign(@RequestBody Assignment input) {
    requireProvider(input.provider());
    if (input.collaborator() == null || input.collaborator().isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Colaborador obligatorio");
    assignments.computeIfAbsent(input.collaborator(), k -> ConcurrentHashMap.newKeySet()).add(input.provider());
    return input;
  }
  @GetMapping("/events/{eventId}/attendance") public Map<String, Boolean> attendance(
      @PathVariable Long eventId, @RequestParam String provider) {
    requireProvider(provider);
    return Map.copyOf(attendance.getOrDefault(key(eventId, provider), Map.of()));
  }
  @PutMapping("/events/{eventId}/attendance") public Attendance attendance(
      @PathVariable Long eventId, @RequestBody Attendance input) {
    requireProvider(input.provider());
    if (input.email() == null || !input.email().contains("@"))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo inválido");
    attendance.computeIfAbsent(key(eventId, input.provider()), k -> new ConcurrentHashMap<>())
      .put(input.email().toLowerCase(Locale.ROOT), input.attended());
    return input;
  }
  @GetMapping("/events/{eventId}/actas") public Acta acta(@PathVariable Long eventId, @RequestParam String provider) {
    requireProvider(provider);
    return actas.getOrDefault(key(eventId, provider), new Acta(provider, eventId, "", "NOT_UPLOADED", ""));
  }
  @PostMapping("/events/{eventId}/actas") public Acta registerActa(
      @PathVariable Long eventId, @RequestBody Map<String, String> body) {
    String provider = body.get("provider"), filename = body.get("filename");
    requireProvider(provider);
    if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".pdf"))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acta PDF obligatoria");
    Acta acta = new Acta(provider, eventId, filename, "PENDING_PROVIDER_SIGNATURE", LocalDateTime.now().toString());
    actas.put(key(eventId, provider), acta);
    notificationLog.add(Map.of("type", "ACTA", "provider", provider, "eventId", eventId.toString()));
    return acta;
  }
  @PostMapping("/events/{eventId}/actas/sign") public Acta sign(
      @PathVariable Long eventId, @RequestBody Map<String, String> body) {
    String provider = body.get("provider"); requireProvider(provider);
    Acta old = actas.get(key(eventId, provider));
    if (old == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe acta");
    Acta signed = new Acta(provider, eventId, old.filename(), "SIGNED_MOCK", LocalDateTime.now().toString());
    actas.put(key(eventId, provider), signed);
    return signed;
  }
  @GetMapping("/notifications") public List<Map<String, String>> notifications() { return List.copyOf(notificationLog); }
  @GetMapping("/events/{eventId}/status") public Map<String, String> status(@PathVariable Long eventId) {
    return Map.of("status", eventStates.getOrDefault(eventId.toString(), "DISPONIBLE"));
  }
  @PutMapping("/events/{eventId}/status") public Map<String, String> status(
      @PathVariable Long eventId, @RequestBody Map<String, String> body) {
    String state = body.get("status");
    if (!Set.of("BORRADOR", "DISPONIBLE", "FINALIZADO", "CANCELADO").contains(state))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
    eventStates.put(eventId.toString(), state);
    return Map.of("status", state);
  }
}
