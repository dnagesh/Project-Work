package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTOPrimaryKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxRunRepository extends
    CassandraRepository<SandboxRunDTO, SandboxRunDTOPrimaryKey> {

  List<SandboxRunDTO> findAllByPrimaryKeySandboxUUID(UUID sandboxUUID);

}
