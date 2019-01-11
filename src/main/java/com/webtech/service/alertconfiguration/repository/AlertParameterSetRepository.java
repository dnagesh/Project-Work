package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertParameterSetRepository extends
    CassandraRepository<AlertParameterSetDTO, String> {

}
