package org.app.common.base.jpa;

import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 788258042948590379L;
    protected List<SearchCriteria> criteria = new ArrayList<>(16);

    public void add(SearchCriteria criteria) {
        this.criteria.add(criteria);
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

        for (SearchCriteria criteria : this.criteria) {
            switch (criteria.getOperation()) {
                case EQUALS:
                    predicates.add(builder.equal(root.get(criteria.getKey()), criteria.getValue()));
                    break;
                case NOT_EQUALS:
                    predicates.add(builder.notEqual(root.get(criteria.getKey()), criteria.getValue()));
                    break;
                case LIKE:
                    predicates.add(builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%"));
                    break;
                case STARTS_WITH:
                    predicates.add(builder.like(root.get(criteria.getKey()), criteria.getValue() + "%"));
                    break;
                case ENDS_WITH:
                    predicates.add(builder.like(root.get(criteria.getKey()), "%" + criteria.getValue()));
                    break;
                case GREATER_THAN:
                    handleGreaterThan(root, builder, predicates, criteria);
                    break;
                case LESS_THAN:
                    handleLessThan(root, builder, predicates, criteria);
                    break;
                case GREATER_THAN_EQUAL:
                    handleGreaterThanEqual(root, builder, predicates, criteria);
                    break;
                case LESS_THAN_EQUAL:
                    handleLessThanEqual(root, builder, predicates, criteria);
                    break;
                case IN:
                    if (criteria.getValue() instanceof Collection<?>) {
                        predicates.add(root.get(criteria.getKey()).in((Collection<?>) criteria.getValue()));
                    }
                    break;
                case NOT_IN:
                    if (criteria.getValue() instanceof Collection<?>) {
                        predicates.add(builder.not(root.get(criteria.getKey()).in((Collection<?>) criteria.getValue())));
                    }
                    break;
                case IS_NULL:
                    predicates.add(builder.isNull(root.get(criteria.getKey())));
                    break;
                case IS_NOT_NULL:
                    predicates.add(builder.isNotNull(root.get(criteria.getKey())));
                    break;
                case BETWEEN:
                    if (criteria.getValue() instanceof Range) {
                        Range<?> range = (Range<?>) criteria.getValue();
                        handleBetween(root, builder, predicates, criteria, range);
                    }
                    break;
            }
        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

    private void handleGreaterThan(Root<T> root, CriteriaBuilder builder, List<Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.greaterThan(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.gt(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    private void handleLessThan(Root<T> root, CriteriaBuilder builder, List<Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.lessThan(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.lt(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    private void handleGreaterThanEqual(Root<T> root, CriteriaBuilder builder, List<Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.greaterThanOrEqualTo(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.ge(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    private void handleLessThanEqual(Root<T> root, CriteriaBuilder builder, List<Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.lessThanOrEqualTo(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.le(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    private void handleBetween(Root<T> root,
                               CriteriaBuilder builder,
                               List<Predicate> predicates,
                               SearchCriteria criteria, Range<?> range) {
        if (range.getFrom() instanceof LocalDateTime && range.getTo() instanceof LocalDateTime) {
            Path<LocalDateTime> path = root.get(criteria.getKey());
            predicates.add(
                    builder.between(
                            path,
                            (LocalDateTime) range.getFrom(),
                            (LocalDateTime) range.getTo()
                    )
            );
        } else if (range.getFrom() instanceof Integer && range.getTo() instanceof Integer) {
            Path<Integer> path = root.get(criteria.getKey());

            predicates.add(
                    builder.between(
                            path,
                            (Integer) range.getFrom(),
                            (Integer) range.getTo()
                    )
            );
        } else if (range.getFrom() instanceof Long && range.getTo() instanceof Long) {
            Path<Long> path = root.get(criteria.getKey());

            predicates.add(
                    builder.between(
                            path,
                            (Long) range.getFrom(),
                            (Long) range.getTo()
                    )
            );
        }
    }

    @Getter
    public static class Range<T> {
        private final T from;
        private final T to;

        public Range(T from, T to) {
            this.from = from;
            this.to = to;
        }
    }
}