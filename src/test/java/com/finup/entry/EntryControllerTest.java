package com.finup.entry;

import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntryControllerTest {

    @InjectMocks
    private EntryController entryController;

    @Mock
    private EntryService entryService;

    @Test
    void testGetByPeriod() {
        User user = User.builder().id(1L).build();
        EntryDto dto = new EntryDto(1000L, 100L, "Asset", 10L, "Group", (short) 2023, (short) 10, BigDecimal.TEN, null, null);

        when(entryService.getByPeriod(user, (short) 2023, (short) 10)).thenReturn(List.of(dto));

        ResponseEntity<List<EntryDto>> response = entryController.getByPeriod(user, (short) 2023, (short) 10);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(1000L, response.getBody().get(0).id());
    }

    @Test
    void testSaveBatch() {
        User user = User.builder().id(1L).build();
        EntryBatchRequest req = new EntryBatchRequest((short) 2023, (short) 10, List.of());
        EntryDto dto = new EntryDto(1000L, 100L, "Asset", 10L, "Group", (short) 2023, (short) 10, BigDecimal.TEN, null, null);

        when(entryService.saveBatch(user, req)).thenReturn(List.of(dto));

        ResponseEntity<List<EntryDto>> response = entryController.saveBatch(user, req);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(1000L, response.getBody().get(0).id());
    }
}
