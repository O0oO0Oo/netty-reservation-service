package org.server.rsaga.business.infra.repository;

import org.server.rsaga.business.domain.Business;

public interface BusinessCustomRepository {
    Business findByIdOrElseThrow(Long id);
}
