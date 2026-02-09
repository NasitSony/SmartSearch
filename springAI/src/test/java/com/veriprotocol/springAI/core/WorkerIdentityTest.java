package com.veriprotocol.springAI.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class WorkerIdentityTest {

  @Test
  void workerIdIsGeneratedAndStablePerInstance() {
    WorkerIdentity wi = new WorkerIdentity();

    String id1 = wi.getWorkerId();
    String id2 = wi.getWorkerId();

    assertNotNull(id1);
    assertFalse(id1.isBlank());
    assertEquals(id1, id2);          // same instance -> same id
    assertDoesNotThrow(() -> java.util.UUID.fromString(id1)); // valid UUID
  }

  @Test
  void differentInstancesHaveDifferentIds() {
    WorkerIdentity a = new WorkerIdentity();
    WorkerIdentity b = new WorkerIdentity();

    assertNotEquals(a.getWorkerId(), b.getWorkerId());
  }
}
