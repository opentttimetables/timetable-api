package org.openttt.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openttt.model.Tag;
import org.openttt.model.Timetable;
import org.openttt.model.TimetabledOrder;
import org.openttt.repo.DestinationRepo;
import org.openttt.repo.TagRepo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimetableDTO {
    private Integer id;
    @NotEmpty
    private String name;
    @Size(min = 2)
    private List<TimetabledOrderDTO> orders;

    private Set<TagDTO> tags;

    public List<TimetabledOrder> mapOrders(MapperService mapper, DestinationRepo destinationRepo, Timetable timetable) {
        return orders.stream()
                .map(orderDto -> {
                    TimetabledOrder timetabledOrder = mapper.map(orderDto, TimetabledOrder.class);
                    timetabledOrder.setDestination(destinationRepo.findByName(orderDto.getDestination()));
                    timetabledOrder.setTimetable(timetable);
                    return timetabledOrder;
                }).collect(Collectors.toList());
    }

    public Set<Tag> mapTags(TagRepo tagRepo) {
        return tags.stream()
            .map(tagDto -> tagRepo.findById(tagDto.getId()).orElseThrow())
            .collect(Collectors.toSet());
    }
}
