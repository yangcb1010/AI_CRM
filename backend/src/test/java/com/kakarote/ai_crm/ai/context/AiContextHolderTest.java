package com.kakarote.ai_crm.ai.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AiContextHolderTest {

    private static final long SESSION_ID = 99001L;

    @AfterEach
    void tearDown() {
        AiContextHolder.clear();
        AiContextHolder.clearSession(SESSION_ID);
    }

    @Test
    void restoresObjectContextBySessionId() {
        AiContextHolder.setContext(SESSION_ID, 1001L, 2001L, 3001L, 4001L, 5001L, 6001L, 7001L);
        AiContextHolder.clear();

        AiContextHolder.setSessionId(SESSION_ID);

        assertEquals(1001L, AiContextHolder.getCurrentUserId());
        assertEquals(2001L, AiContextHolder.getCurrentCustomerId());
        assertEquals(3001L, AiContextHolder.getCurrentEmployeeId());
        assertEquals(4001L, AiContextHolder.getCurrentRelationId());
        assertEquals(5001L, AiContextHolder.getCurrentProductId());
        assertEquals(6001L, AiContextHolder.getCurrentProjectId());
        assertEquals(7001L, AiContextHolder.getCurrentProjectTaskId());
    }

    @Test
    void clearSessionRemovesObjectContext() {
        AiContextHolder.setContext(SESSION_ID, 1001L, 2001L, null, null, null, 6001L, null);
        AiContextHolder.clearSession(SESSION_ID);
        AiContextHolder.clear();

        AiContextHolder.setSessionId(SESSION_ID);

        assertNull(AiContextHolder.getCurrentUserId());
        assertNull(AiContextHolder.getCurrentCustomerId());
        assertNull(AiContextHolder.getCurrentProjectId());
    }
}
