package org.openttd.opentttimetables.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import org.modelmapper.ModelMapper;
import org.openttd.opentttimetables.model.Schedule;
import org.openttd.opentttimetables.model.ScheduledDispatch;
import org.openttd.opentttimetables.model.ScheduledOrder;
import org.openttd.opentttimetables.repo.ScheduledDispatchRepo;
import org.openttd.opentttimetables.repo.TimetableRepo;
import org.openttd.opentttimetables.rest.dto.*;
import org.openttd.opentttimetables.scheduling.ScheduleSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class ScheduledDispatchController {
    @Autowired
    private ModelMapper mapper;

    @Autowired
    private TimetableRepo timetableRepo;

    @Autowired
    private ScheduledDispatchRepo scheduledDispatchRepo;

    // TODO: check if id -> scheduled dispatch conversion can be done automatically? Spring boot surely has something here!
    @RequestMapping(path = {"/scheduled-dispatches/", "/scheduled-dispatch/"})
    public List<ScheduledDispatchDTO> getAllDispatches() {
        return Streams.stream(scheduledDispatchRepo.findAll())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @RequestMapping(path = "/scheduled-dispatch/{id}")
    public ScheduledDispatchDTO getScheduledDispatch(@PathVariable Integer id) {
        // TODO: Defensive programming! .get() is evil!
        return toDto(scheduledDispatchRepo.findById(id).get());
    }

    @RequestMapping(path = "/scheduled-dispatch/{id}/departures")
    public List<ScheduleDTO> getDeparturesForSchedule(@PathVariable Integer id) {
        ScheduledDispatch dispatch = scheduledDispatchRepo.findById(id).get();
        return generateSchedules(dispatch) // TODO hardcoded value! make query param
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @RequestMapping(path = "/scheduled-dispatch/{id}/departures-by-station")
    public List<SchedulesByStationDTO> getDeparturesForScheduleByStation(@PathVariable Integer id) {
        ScheduledDispatch dispatch = scheduledDispatchRepo.findById(id).get();
        List<Schedule> schedules = generateSchedules(dispatch).collect(Collectors.toList());
        return toByStationDtos(schedules);
    }

    private Stream<Schedule> generateSchedules(ScheduledDispatch dispatch) {
        return Stream.generate(new ScheduleSupplier(dispatch))
                .limit(5);
    }

    private ScheduledDispatchDTO toDto(ScheduledDispatch dispatch) {
        ScheduledDispatchDTO dto = mapper.map(dispatch, ScheduledDispatchDTO.class);
        dto.setTimetable(dispatch.getTimetable().getName());
        return dto;
    }

    private ScheduledDispatch fromDto(ScheduledDispatchDTO dto) {
        ScheduledDispatch scheduledDispatch = mapper.map(dto, ScheduledDispatch.class);
        scheduledDispatch.setTimetable(timetableRepo.findByName(dto.getTimetable()));
        return scheduledDispatch;
    }

    private ScheduleDTO toDto(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setOrders(
                schedule.orders()
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private ScheduledOrderDTO toDto(ScheduledOrder order) {
        ScheduledOrderDTO dto = mapper.map(order, ScheduledOrderDTO.class);
        dto.setDestination(order.getDestination().getName());
        return dto;
    }

    private List<SchedulesByStationDTO> toByStationDtos(List<Schedule> schedules) {
        List<ScheduledOrder> arbitraryOrderList = schedules.get(0).orders();
        List<SchedulesByStationDTO> dtos = Lists.newArrayListWithExpectedSize(arbitraryOrderList.size());
        for (int i = 0; i < arbitraryOrderList.size(); i++) {
            SchedulesByStationDTO byStationDto = new SchedulesByStationDTO();
            byStationDto.setStation(arbitraryOrderList.get(i).getDestination().getName());

            List<ScheduleDepartureDTO> departureDtos = Lists.newArrayListWithExpectedSize(schedules.size());
            for (Schedule schedule : schedules) {
                ScheduledOrder orderForThisRowAndColumn = schedule.orders().get(i);
                departureDtos.add(new ScheduleDepartureDTO(
                        orderForThisRowAndColumn.getArrival(),
                        orderForThisRowAndColumn.getDeparture()
                ));
            }

            byStationDto.setDepartures(departureDtos);
            dtos.add(byStationDto);
        }

        return dtos;
    }
}