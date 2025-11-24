package com.configfy.configfyapi.mapper;

import com.configfy.configfyapi.domain.dto.EnvironmentDTO;
import com.configfy.configfyapi.domain.model.Environment;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EnvironmentMapper {

    EnvironmentDTO toDTO(Environment environment);

    Environment toEntity(EnvironmentDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(EnvironmentDTO dto, @MappingTarget Environment entity);
}
