import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "PlanPattern")
public class PlanPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "optimalmicroclimate_id", referencedColumnName = "id")
    private Microclimate microclimate;

    @OneToMany(mappedBy = "planPattern")
    private List<MicroclimatePlan> microclimatePlans;

    @Column
    private String device;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "planParameters_id", referencedColumnName = "id")
    private PlanParameters planParameters;
}


import com.example.hygimeter.dto.group.OnCreate;
import com.example.hygimeter.dto.group.OnUpdate;
import com.example.hygimeter.model.Microclimate;
import com.example.hygimeter.model.PlanParameters;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
@Schema(description = "Plan Pattern Data Transfer Object")
public class PlanPatternDTO {

    @Schema(description = "PlanPattern id")
    private Integer id;

    @Null(groups = OnCreate.class, message = "Devices must be null at fulling the form")
    @Schema(description = "Plan devices")
    private String device;

    @Null(groups = OnCreate.class, message = "Microclimate should be null while 1st time creating")
    @Valid
    @Schema(description = "Microclimate")
    private MicroclimateDTO microclimateDTO;

    @NotNull(groups = OnCreate.class, message = "Plan parameters cannot be null")
    @Valid
    @Schema(description = "Plan Parameters")
    private PlanParametersDTO planParametersDTO;
}

import com.example.hygimeter.model.PlanPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanPatternRepository extends JpaRepository<PlanPattern, Integer> {
    Optional<PlanPattern> findPlanPatternById(Integer id);
}


import com.example.hygimeter.dto.PlanPatternDTO;
import org.springframework.data.relational.core.sql.In;

import java.util.List;

public interface PlanPatternService {
    PlanPatternDTO createPlanPattern(PlanPatternDTO planPatternDTO);
    PlanPatternDTO updatePlanPattern(Integer id, PlanPatternDTO planPatternDTO);
    void deletePlanPattern(Integer id);
    PlanPatternDTO getPlanPatternById(Integer id);
    List<PlanPatternDTO> getAllPlanPatterns();
}


import com.example.hygimeter.dto.HumidityDTO;
import com.example.hygimeter.dto.MicroclimateDTO;
import com.example.hygimeter.dto.PlanParametersDTO;
import com.example.hygimeter.dto.PlanPatternDTO;
import com.example.hygimeter.dto.group.OnCreate;
import com.example.hygimeter.dto.group.OnUpdate;
import com.example.hygimeter.exception.EntityNotFoundException;
import com.example.hygimeter.exception.StatusCodes;
import com.example.hygimeter.exception.InvalidDataException;
import com.example.hygimeter.mapper.PlanPatternMapper;
import com.example.hygimeter.model.Humidity;
import com.example.hygimeter.model.Microclimate;
import com.example.hygimeter.model.PlanParameters;
import com.example.hygimeter.model.PlanPattern;
import com.example.hygimeter.repository.PlanPatternRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanPatternServiceImpl implements PlanPatternService{

    private final PlanPatternRepository planPatternRepository;
    private final PlanPatternMapper planPatternMapper;

    @Override
    public PlanPatternDTO createPlanPattern(PlanPatternDTO planPatternDTO) {
        PlanPattern planPattern = planPatternMapper.toPlanPattern(planPatternDTO);
        return planPatternMapper.toPlanPatternDTO(planPatternRepository.save(planPattern));
    }

    @Override
    public PlanPatternDTO updatePlanPattern(Integer id, PlanPatternDTO planPatternDTO) {

        // Fetch the existing plan pattern
        PlanPattern planPattern = planPatternRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(StatusCodes.ENTITY_NOT_FOUND.name(), "Plan pattern not found"));

        planPatternValidation(planPatternDTO, OnUpdate.class);

        // If everything is fine, update the plan pattern
        PlanPattern newPlanPattern = planPatternMapper.toPlanPattern(planPatternDTO);
        planPattern.setMicroclimatePlans(newPlanPattern.getMicroclimatePlans());
        planPattern.setDevice(newPlanPattern.getDevice());
        planPattern.setPlanParameters(newPlanPattern.getPlanParameters());

        return planPatternMapper.toPlanPatternDTO(planPatternRepository.save(planPattern));
    }

    @Override
    public void deletePlanPattern(Integer id) {
        PlanPattern planPattern = planPatternRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(StatusCodes.ENTITY_NOT_FOUND.name(), "Plan Pattern not found"));

        planPatternRepository.deleteById(planPattern.getId());
    }

    @Override
    public PlanPatternDTO getPlanPatternById(Integer id) {
        PlanPattern planPattern = planPatternRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(StatusCodes.ENTITY_NOT_FOUND.name(), "Plan Pattern not found"));

        return planPatternMapper.toPlanPatternDTO(planPattern);
    }

    @Override
    public List<PlanPatternDTO> getAllPlanPatterns() {
        List<PlanPattern> planPatterns = planPatternRepository.findAll();
        return planPatternMapper.toPlanPatternDTOS(planPatterns);
    }

    private void planPatternValidation(PlanPatternDTO planPatternDTO, Class<?> validationGroup) {
        MicroclimateDTO microclimate = planPatternDTO.getMicroclimateDTO();
        HumidityDTO humidity = microclimate.getHumidity();
        PlanParametersDTO planParameters = planPatternDTO.getPlanParametersDTO();

        // Assuming that an ID of null means this is a create operation
        boolean isCreateOperation = (planPatternDTO.getId() == null);

        if (isCreateOperation && validationGroup.equals(OnCreate.class)) {
            // Perform validations specific to OnCreate group
            if (planPatternDTO.getDevice() != null) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Device must be null at fulling the form");
            }
            if (microclimate != null) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Microclimate should be null while 1st time creating");
            }
            if (planParameters == null) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Plan parameters cannot be null");
            }
            if (humidity.getRelativeHumidity() != null) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Relative humidity must be null on creating plan parameters");
            }
            if (humidity.getAbsoluteHumidity() != null) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Absolute humidity must be null on creating plan parameters");
            }
        } else if (!isCreateOperation && validationGroup.equals(OnUpdate.class)) {
            // Perform validations specific to OnUpdate group
            if (microclimate.getTemperature().length() > 20) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Max size of temperature is 20 characters");
            }
            if (microclimate.getVentilation().length() > 100) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Max size of ventilation is 100 characters");
            }
            if (microclimate.getLightLevel() <= 0) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Light level must be greater than 0");
            }
            if (humidity.getRelativeHumidity() == null || humidity.getRelativeHumidity() <= 0) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Relative humidity must be not null and greater than 0 on updating parameters");
            }
            if (humidity.getAbsoluteHumidity() == null || humidity.getAbsoluteHumidity() <= 0) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Absolute humidity must be not null and greater than 0 on updating parameters");
            }
            if (planParameters.getTemperatureSked() == null || planParameters.getTemperatureSked().isEmpty()) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Temperature schedule must be not null and not empty on updating parameters");
            }
            if (planParameters.getTemperatureSked().length() > 100) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Max size of temperature schedule is 100 characters");
            }
            if (planParameters.getLightsOffTime() == null || planParameters.getLightsOffTime().getHour() > 23 || planParameters.getLightsOffTime().getHour() < 0 ) {
                throw new InvalidDataException(StatusCodes.INVALID_DATA.name(), "Time when lights go off must be not null on updating parameters");
            }
        }
    }
}
