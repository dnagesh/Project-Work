package com.webtech.service.alert.repository;

import com.webtech.service.alert.dto.LiveAlertAuditDTO;
import com.webtech.service.alert.dto.LiveAlertAuditDTOPrimaryKey;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveAlertAuditRepository extends
    CassandraRepository<LiveAlertAuditDTO, LiveAlertAuditDTOPrimaryKey> {

  List<LiveAlertAuditDTO> findAllByPrimaryKeyAlertId(UUID alertId);

}
