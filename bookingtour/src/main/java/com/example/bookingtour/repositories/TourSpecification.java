package com.example.bookingtour.repositories;

import com.example.bookingtour.dtos.request.tour.TourSearchRequest;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.TourStatus;
import com.example.bookingtour.enums.PassengerType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TourSpecification {

    public static Specification<Tour> filterTours(TourSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), TourStatus.ACTIVE));

            if (StringUtils.hasText(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().toLowerCase() + "%";

                Join<Tour, Destination> destinationJoin = root.join("destination", JoinType.LEFT);

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("tourcode")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(destinationJoin.get("name")), pattern),
                        cb.like(cb.lower(destinationJoin.get("description")), pattern)
                ));
            }

            if (request.getDestinationIds() != null && !request.getDestinationIds().isEmpty()) {
                predicates.add(root.get("destination").get("id").in(request.getDestinationIds()));
            }

            if (hasScheduleFilters(request)) {

                Subquery<Integer> scheduleSubquery = query.subquery(Integer.class);
                Root<TourSchedule> scheduleRoot = scheduleSubquery.from(TourSchedule.class);

                List<Predicate> subPredicates = new ArrayList<>();
                subPredicates.add(cb.equal(scheduleRoot.get("tour"), root));

                if (request.getDepartureLocations() != null && !request.getDepartureLocations().isEmpty()) {
                    subPredicates.add(scheduleRoot.get("departureLocation").in(request.getDepartureLocations()));
                }

                if (request.getFromDate() != null) {
                    subPredicates.add(cb.greaterThanOrEqualTo(scheduleRoot.get("departureDate"), request.getFromDate().atStartOfDay()));
                }

                if (request.getToDate() != null) {
                    subPredicates.add(cb.lessThanOrEqualTo(scheduleRoot.get("departureDate"), request.getToDate().atTime(23, 59, 59)));
                }

                if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                    Subquery<Integer> priceSubquery = scheduleSubquery.subquery(Integer.class);
                    Root<TourPricingConfig> priceRoot = priceSubquery.from(TourPricingConfig.class);

                    List<Predicate> pricePredicates = new ArrayList<>();
                    pricePredicates.add(cb.equal(priceRoot.get("schedule"), scheduleRoot));

                    pricePredicates.add(cb.equal(priceRoot.get("passengerType"), PassengerType.ADULT));

                    if (request.getMinPrice() != null) {
                        pricePredicates.add(cb.greaterThanOrEqualTo(priceRoot.get("price"), request.getMinPrice()));
                    }
                    if (request.getMaxPrice() != null) {
                        pricePredicates.add(cb.lessThanOrEqualTo(priceRoot.get("price"), request.getMaxPrice()));
                    }
                    priceSubquery.select(cb.literal(1)).where(pricePredicates.toArray(new Predicate[0]));
                    subPredicates.add(cb.exists(priceSubquery));
                }

                scheduleSubquery.select(scheduleRoot.get("tour").get("id")).where(subPredicates.toArray(new Predicate[0]));
                predicates.add(root.get("id").in(scheduleSubquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasScheduleFilters(TourSearchRequest request) {
        return (request.getDepartureLocations() != null && !request.getDepartureLocations().isEmpty())
                || request.getFromDate() != null
                || request.getToDate() != null
                || request.getMinPrice() != null
                || request.getMaxPrice() != null;
    }
}