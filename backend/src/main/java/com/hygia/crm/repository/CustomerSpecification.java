package com.hygia.crm.repository;

import com.hygia.crm.entity.Customer;
import com.hygia.crm.entity.VisitLog;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {

    public static Specification<Customer> withFilters(
            Long regionId,
            String tier,
            String searchQuery,
            Boolean isProspect,
            String followup) {

        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Filter by regionId
            if (regionId != null) {
                predicates.add(cb.equal(root.get("region").get("id"), regionId));
            }

            // Filter by tier
            if (tier != null && !tier.isEmpty()) {
                predicates.add(cb.equal(root.get("tier"), tier));
            }

            // Filter by isProspect
            if (isProspect != null) {
                predicates.add(cb.equal(root.get("isProspect"), isProspect));
            }

            // Filter by follow-up (due visits)
            if ("due".equalsIgnoreCase(followup)) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var visitRoot = subquery.from(VisitLog.class);
                subquery.select(visitRoot.get("id"));
                Predicate customerMatch = cb.equal(visitRoot.get("customer"), root);
                Predicate nextFollowUpNotNull = cb.isNotNull(visitRoot.get("nextFollowUpAt"));
                Predicate nextFollowUpDue = cb.lessThanOrEqualTo(
                        visitRoot.get("nextFollowUpAt"), OffsetDateTime.now());
                subquery.where(customerMatch, nextFollowUpNotNull, nextFollowUpDue);
                predicates.add(cb.exists(subquery));
            }

            // Search query (case-insensitive contains match on nameStd and phone)
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("nameStd")), searchPattern);
                
                // Phone predicate: only match if phone is not null and contains the search query
                Predicate phonePredicate = cb.and(
                    cb.isNotNull(root.get("phone")),
                    cb.like(cb.lower(root.get("phone")), searchPattern)
                );
                
                // Match if nameStd OR phone matches
                predicates.add(cb.or(namePredicate, phonePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

