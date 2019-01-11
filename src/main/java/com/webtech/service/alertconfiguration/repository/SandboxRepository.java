package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxRepository extends
    CassandraRepository<SandboxDTO, UUID> {

}
