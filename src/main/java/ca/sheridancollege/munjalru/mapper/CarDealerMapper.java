package ca.sheridancollege.munjalru.mapper;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CarResponse;
import ca.sheridancollege.munjalru.dto.DealerRequest;
import ca.sheridancollege.munjalru.dto.DealerResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CarDealerMapper {

    public CarResponse toCarResponse(Car car) {
        if (car == null) {
            return null;
        }
        return CarResponse.builder()
                .id(car.getId())
                .make(car.getMake())
                .model(car.getModel())
                .modelYear(car.getModelYear())
                .build();
    }

    public Car toCarEntity(CarRequest request) {
        if (request == null) {
            return null;
        }
        return Car.builder()
                .make(request.getMake())
                .model(request.getModel())
                .modelYear(request.getModelYear())
                .build();
    }

    public void updateCarEntity(Car car, CarRequest request) {
        if (car == null || request == null) {
            return;
        }
        car.setMake(request.getMake());
        car.setModel(request.getModel());
        car.setModelYear(request.getModelYear());
    }

    public DealerResponse toDealerResponse(Dealer dealer) {
        if (dealer == null) {
            return null;
        }
        List<CarResponse> carResponses = dealer.getCars() == null
                ? List.of()
                : dealer.getCars().stream().map(this::toCarResponse).toList();
        return DealerResponse.builder()
                .id(dealer.getId())
                .name(dealer.getName())
                .location(dealer.getLocation())
                .cars(carResponses)
                .build();
    }

    public Dealer toDealerEntity(DealerRequest request) {
        if (request == null) {
            return null;
        }
        return Dealer.builder()
                .name(request.getName())
                .location(request.getLocation())
                .build();
    }

    public void updateDealerEntity(Dealer dealer, DealerRequest request) {
        if (dealer == null || request == null) {
            return;
        }
        dealer.setName(request.getName());
        dealer.setLocation(request.getLocation());
    }
}
