package com.horticontrol.export_service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportacaoSpringDataRepository
        extends JpaRepository<ExportacaoEntity, String> {
}
