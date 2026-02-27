package it.asd.membership.features.addtogroup;

import java.util.UUID;

public record EnrollmentResponse(UUID enrollmentId, UUID groupId, UUID personId) {
}
