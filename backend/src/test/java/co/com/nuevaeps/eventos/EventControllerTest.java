package co.com.nuevaeps.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {
 @Autowired MockMvc mvc;
 @Test void listEvents() throws Exception {
  mvc.perform(get("/api/events")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").exists());
 }
 @Test void rejectsInvalidEventDates() throws Exception {
  mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
    .content("{\"name\":\"Prueba\",\"start\":\"2026-10-12T12:00:00\",\"end\":\"2026-10-11T12:00:00\",\"responsible\":\"Demo\",\"slotMinutes\":30}"))
    .andExpect(status().isBadRequest());
 }
 @Test void listsMockProviders() throws Exception {
  mvc.perform(get("/api/workflow/providers")).andExpect(status().isOk()).andExpect(jsonPath("$[0].code").exists());
 }
 @Test void rejectsUnknownProviderAssignment() throws Exception {
  mvc.perform(post("/api/workflow/assignments").contentType(MediaType.APPLICATION_JSON)
    .content("{\"collaborator\":\"demo@nuevaeps.com\",\"provider\":\"UNKNOWN\"}"))
    .andExpect(status().isBadRequest());
 }
}
