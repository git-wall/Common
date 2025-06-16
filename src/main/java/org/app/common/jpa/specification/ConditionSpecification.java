package org.app.common.jpa.specification;

import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Supplier;

public class ConditionSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 788258042948590379L;
    protected List<SearchCriteria> criteria = new ArrayList<>(16);
    private final Map<String, Join<T, ?>> joinMap = new HashMap<>();
    private boolean distinct = false;
    protected boolean hasConditions = false;

    public ConditionSpecification<T> add(SearchCriteria criteria) {
        this.criteria.add(criteria);
        this.hasConditions = true;
        return this;
    }

    public ConditionSpecification<T> setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public boolean hasConditions() {
        return this.hasConditions;
    }

    @SafeVarargs
    public static <T> ConditionSpecification<T> and(ConditionSpecification<T>... specifications) {
        return new AndSpecification<>(specifications);
    }

    @SafeVarargs
    public static <T> ConditionSpecification<T> or(ConditionSpecification<T>... specifications) {
        return new OrSpecification<>(specifications);
    }

    public ConditionSpecification<T> and(ConditionSpecification<T> other) {
        return and(this, other);
    }

    public ConditionSpecification<T> or(ConditionSpecification<T> other) {
        return or(this, other);
    }

    @Override
    public javax.persistence.criteria.Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query,
            @NonNull CriteriaBuilder builder) {
        // Set distinct if needed
        if (distinct || !hasConditions) {
            query.distinct(true);
        }

        if (!hasConditions) {
            return builder.conjunction(); // Return true if no conditions
        }

        List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

        for (SearchCriteria criteria : this.criteria) {
            if (criteria.isJoin()) {
                handleJoinCriteria(root, builder, predicates, criteria);
            } else if (criteria.isSubquery()) {
                handleSubqueryCriteria(root, query, builder, predicates, criteria);
            } else {
                handleSimpleCriteria(root, builder, predicates, criteria);
            }
        }

        return builder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
    }

    // Fluent API methods

    /**
     * Conditionally adds a criteria if the condition is true
     * @param condition the condition to check
     * @param criteriaSupplier supplier that creates the criteria
     * @return this specification for chaining
     */
    public ConditionSpecification<T> when(boolean condition, Supplier<SearchCriteria> criteriaSupplier) {
        if (condition) {
            this.add(criteriaSupplier.get());
        }
        return this;
    }

    /**
     * Conditionally adds a criteria if the value is not null
     * @param value the value to check
     * @param criteriaFunction function that creates the criteria using the value
     * @return this specification for chaining
     */
    public <V> ConditionSpecification<T> whenNotNull(V value,
                                                     java.util.function.Function<V, SearchCriteria> criteriaFunction) {
        if (value != null) {
            this.add(criteriaFunction.apply(value));
        }
        return this;
    }

    /**
     * Conditionally adds a criteria if the string is not empty
     * @param value the string to check
     * @param criteriaFunction function that creates the criteria using the string
     * @return this specification for chaining
     */
    public ConditionSpecification<T> whenNotEmpty(String value,
                                                  java.util.function.Function<String, SearchCriteria> criteriaFunction) {
        if (value != null && !value.isEmpty()) {
            this.add(criteriaFunction.apply(value));
        }
        return this;
    }

    /**
     * Adds multiple criteria at once
     * @param criteriaList list of search criteria to add
     * @return this specification for chaining
     */
    public ConditionSpecification<T> addAll(List<SearchCriteria> criteriaList) {
        if (criteriaList != null && !criteriaList.isEmpty()) {
            this.criteria.addAll(criteriaList);
            this.hasConditions = true;
        }
        return this;
    }

    protected void handleJoinCriteria(Root<T> root, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        String joinKey = criteria.getJoinAttribute();

        // Create or get the join
        Join<T, ?> join = getOrCreateJoin(root, joinKey, criteria.getJoinType());

        // Apply the predicate on the join
        switch (criteria.getOperation()) {
            case EQUALS:
                predicates.add(builder.equal(join.get(criteria.getKey()), criteria.getValue()));
                break;
            case NOT_EQUALS:
                predicates.add(builder.notEqual(join.get(criteria.getKey()), criteria.getValue()));
                break;
            case LIKE:
                predicates.add(builder.like(join.get(criteria.getKey()), "%" + criteria.getValue() + "%"));
                break;
            case STARTS_WITH:
                predicates.add(builder.like(join.get(criteria.getKey()), criteria.getValue() + "%"));
                break;
            case ENDS_WITH:
                predicates.add(builder.like(join.get(criteria.getKey()), "%" + criteria.getValue()));
                break;
            case GREATER_THAN:
                handleGreaterThanJoin(join, builder, predicates, criteria);
                break;
            case LESS_THAN:
                handleLessThanJoin(join, builder, predicates, criteria);
                break;
            case GREATER_THAN_EQUAL:
                handleGreaterThanEqualJoin(join, builder, predicates, criteria);
                break;
            case LESS_THAN_EQUAL:
                handleLessThanEqualJoin(join, builder, predicates, criteria);
                break;
            case IN:
                if (criteria.getValue() instanceof Collection<?>) {
                    predicates.add(join.get(criteria.getKey()).in((Collection<?>) criteria.getValue()));
                }
                break;
            case NOT_IN:
                if (criteria.getValue() instanceof Collection<?>) {
                    predicates.add(builder.not(join.get(criteria.getKey()).in((Collection<?>) criteria.getValue())));
                }
                break;
            case IS_NULL:
                predicates.add(builder.isNull(join.get(criteria.getKey())));
                break;
            case IS_NOT_NULL:
                predicates.add(builder.isNotNull(join.get(criteria.getKey())));
                break;
            case BETWEEN:
                if (criteria.getValue() instanceof Range) {
                    Range<?> range = (Range<?>) criteria.getValue();
                    handleBetweenJoin(join, builder, predicates, criteria, range);
                }
                break;
        }
    }

    protected Join<T, ?> getOrCreateJoin(Root<T> root, String joinKey, SearchCriteria.JoinType joinType) {
        if (joinMap.containsKey(joinKey)) {
            return joinMap.get(joinKey);
        }

        Join<T, ?> join;
        switch (joinType) {
            case LEFT:
                join = root.join(joinKey, JoinType.LEFT);
                break;
            case RIGHT:
                join = root.join(joinKey, JoinType.RIGHT);
                break;
            case INNER:
            default:
                join = root.join(joinKey, JoinType.INNER);
                break;
        }

        joinMap.put(joinKey, join);
        return join;
    }

    protected void handleSubqueryCriteria(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        Subquery<?> subquery = query.subquery(criteria.getSubqueryClass());
        Root<?> subRoot = subquery.from(criteria.getSubqueryClass());

        // Create the subquery predicate
        javax.persistence.criteria.Predicate subPredicate = null;

        switch (criteria.getOperation()) {
            case EQUALS:
                subPredicate = builder.equal(subRoot.get(criteria.getKey()), criteria.getValue());
                break;
            case NOT_EQUALS:
                subPredicate = builder.notEqual(subRoot.get(criteria.getKey()), criteria.getValue());
                break;
            case GREATER_THAN:
                if (criteria.getValue() instanceof LocalDateTime) {
                    subPredicate = builder.greaterThan(subRoot.get(criteria.getKey()),
                            (LocalDateTime) criteria.getValue());
                } else if (criteria.getValue() instanceof Timestamp) {
                    subPredicate = builder.greaterThan(subRoot.get(criteria.getKey()), (Timestamp) criteria.getValue());
                } else if (criteria.getValue() instanceof Number) {
                    subPredicate = builder.gt(subRoot.get(criteria.getKey()), (Number) criteria.getValue());
                } else {
                    subPredicate = builder.greaterThan(subRoot.get(criteria.getKey()), criteria.getValue().toString());
                }
                break;
            case LESS_THAN:
                if (criteria.getValue() instanceof LocalDateTime) {
                    subPredicate = builder.lessThan(subRoot.get(criteria.getKey()),
                            (LocalDateTime) criteria.getValue());
                } else if (criteria.getValue() instanceof Timestamp) {
                    subPredicate = builder.lessThan(subRoot.get(criteria.getKey()), (Timestamp) criteria.getValue());
                } else if (criteria.getValue() instanceof Number) {
                    subPredicate = builder.lt(subRoot.get(criteria.getKey()), (Number) criteria.getValue());
                } else {
                    subPredicate = builder.lessThan(subRoot.get(criteria.getKey()), criteria.getValue().toString());
                }
                break;
            case BETWEEN:
                if (criteria.getValue() instanceof Range) {
                    Range<?> range = (Range<?>) criteria.getValue();
                    if (range.getFrom() instanceof LocalDateTime && range.getTo() instanceof LocalDateTime) {
                        subPredicate = builder.between(subRoot.get(criteria.getKey()),
                                (LocalDateTime) range.getFrom(), (LocalDateTime) range.getTo());
                    } else if (range.getFrom() instanceof Timestamp && range.getTo() instanceof Timestamp) {
                        subPredicate = builder.between(subRoot.get(criteria.getKey()),
                                (Timestamp) range.getFrom(), (Timestamp) range.getTo());
                    }
                }
                break;
        }

        if (subPredicate != null) {
            subquery.select(subRoot.get(criteria.getSubqueryAttribute()))
                    .where(subPredicate);

            predicates.add(root.get(criteria.getMainEntityAttribute()).in(subquery));
        }
    }

    protected void handleSimpleCriteria(Root<T> root, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
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

    protected void handleGreaterThan(Root<T> root, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.greaterThan(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.greaterThan(root.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.gt(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleLessThan(Root<T> root, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.lessThan(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.lessThan(root.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.lt(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleGreaterThanEqual(Root<T> root, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(
                    builder.greaterThanOrEqualTo(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.greaterThanOrEqualTo(root.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.ge(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleLessThanEqual(Root<T> root, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.lessThanOrEqualTo(root.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.lessThanOrEqualTo(root.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.le(root.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleGreaterThanJoin(Join<T, ?> join, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.greaterThan(join.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.greaterThan(join.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.gt(join.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.greaterThan(join.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleLessThanJoin(Join<T, ?> join, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.lessThan(join.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.lessThan(join.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.lt(join.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.lessThan(join.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleGreaterThanEqualJoin(Join<T, ?> join, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(
                    builder.greaterThanOrEqualTo(join.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.greaterThanOrEqualTo(join.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.ge(join.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.greaterThanOrEqualTo(join.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleLessThanEqualJoin(Join<T, ?> join, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria) {
        if (criteria.getValue() instanceof LocalDateTime) {
            predicates.add(builder.lessThanOrEqualTo(join.get(criteria.getKey()), (LocalDateTime) criteria.getValue()));
        } else if (criteria.getValue() instanceof Timestamp) {
            predicates.add(builder.lessThanOrEqualTo(join.get(criteria.getKey()), (Timestamp) criteria.getValue()));
        } else if (criteria.getValue() instanceof Number) {
            predicates.add(builder.le(join.get(criteria.getKey()), (Number) criteria.getValue()));
        } else {
            predicates.add(builder.lessThanOrEqualTo(join.get(criteria.getKey()), criteria.getValue().toString()));
        }
    }

    protected void handleBetweenJoin(Join<T, ?> join, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria, Range<?> range) {
        if (range.getFrom() instanceof LocalDateTime && range.getTo() instanceof LocalDateTime) {
            Path<LocalDateTime> path = join.get(criteria.getKey());
            predicates.add(
                    builder.between(
                            path,
                            (LocalDateTime) range.getFrom(),
                            (LocalDateTime) range.getTo()));
        } else if (range.getFrom() instanceof Timestamp && range.getTo() instanceof Timestamp) {
            Path<Timestamp> path = join.get(criteria.getKey());
            predicates.add(
                    builder.between(
                            path,
                            (Timestamp) range.getFrom(),
                            (Timestamp) range.getTo()));
        } else if (range.getFrom() instanceof Number && range.getTo() instanceof Number) {
            // Fix for Number type between
            if (range.getFrom() instanceof Integer && range.getTo() instanceof Integer) {
                Path<Integer> path = join.get(criteria.getKey());
                predicates.add(builder.between(path, (Integer) range.getFrom(), (Integer) range.getTo()));
            } else if (range.getFrom() instanceof Long && range.getTo() instanceof Long) {
                Path<Long> path = join.get(criteria.getKey());
                predicates.add(builder.between(path, (Long) range.getFrom(), (Long) range.getTo()));
            } else if (range.getFrom() instanceof Double && range.getTo() instanceof Double) {
                Path<Double> path = join.get(criteria.getKey());
                predicates.add(builder.between(path, (Double) range.getFrom(), (Double) range.getTo()));
            } else if (range.getFrom() instanceof Float && range.getTo() instanceof Float) {
                Path<Float> path = join.get(criteria.getKey());
                predicates.add(builder.between(path, (Float) range.getFrom(), (Float) range.getTo()));
            } else {
                // Fallback to comparison
                Path<Number> path = join.get(criteria.getKey());
                predicates.add(builder.and(
                        builder.ge(path, (Number) range.getFrom()),
                        builder.le(path, (Number) range.getTo())));
            }
        } else if (range.getFrom() instanceof String && range.getTo() instanceof String) {
            Path<String> path = join.get(criteria.getKey());
            predicates.add(
                    builder.between(
                            path,
                            (String) range.getFrom(),
                            (String) range.getTo()));
        }
    }
    
    // Fix for Number type between in handleBetween method
    protected void handleBetween(Root<T> root, CriteriaBuilder builder,
            List<javax.persistence.criteria.Predicate> predicates, SearchCriteria criteria, Range<?> range) {
        if (range.getFrom() instanceof LocalDateTime && range.getTo() instanceof LocalDateTime) {
            Path<LocalDateTime> path = root.get(criteria.getKey());
            predicates.add(
                    builder.between(
                            path,
                            (LocalDateTime) range.getFrom(),
                            (LocalDateTime) range.getTo()));
        } else if (range.getFrom() instanceof Timestamp && range.getTo() instanceof Timestamp) {
            Path<Timestamp> path = root.get(criteria.getKey());
            predicates.add(
                    builder.between(
                            path,
                            (Timestamp) range.getFrom(),
                            (Timestamp) range.getTo()));
        } else if (range.getFrom() instanceof Number && range.getTo() instanceof Number) {
            // Fix for Number type between
            if (range.getFrom() instanceof Integer && range.getTo() instanceof Integer) {
                Path<Integer> path = root.get(criteria.getKey());
                predicates.add(builder.between(path, (Integer) range.getFrom(), (Integer) range.getTo()));
            } else if (range.getFrom() instanceof Long && range.getTo() instanceof Long) {
                Path<Long> path = root.get(criteria.getKey());
                predicates.add(builder.between(path, (Long) range.getFrom(), (Long) range.getTo()));
            } else if (range.getFrom() instanceof Double && range.getTo() instanceof Double) {
                Path<Double> path = root.get(criteria.getKey());
                predicates.add(builder.between(path, (Double) range.getFrom(), (Double) range.getTo()));
            } else if (range.getFrom() instanceof Float && range.getTo() instanceof Float) {
                Path<Float> path = root.get(criteria.getKey());
                predicates.add(builder.between(path, (Float) range.getFrom(), (Float) range.getTo()));
            } else {
                // Fallback to comparison
                Path<Number> path = root.get(criteria.getKey());
                predicates.add(builder.and(
                        builder.ge(path, (Number) range.getFrom()),
                        builder.le(path, (Number) range.getTo())));
            }
        } else if (range.getFrom() instanceof String && range.getTo() instanceof String) {
            Path<String> path = root.get(criteria.getKey());
            predicates.add(
                    builder.between(
                            path,
                            (String) range.getFrom(),
                            (String) range.getTo()));
        }
    }

    // Support for AND specifications
    private static class AndSpecification<T> extends ConditionSpecification<T> {
        private static final long serialVersionUID = 4636154918329065317L;
        private final ConditionSpecification<T>[] specifications;

        public AndSpecification(ConditionSpecification<T>[] specifications) {
            this.specifications = specifications;
            // Set hasConditions based on the specifications
            if (Arrays.stream(specifications).anyMatch(ConditionSpecification::hasConditions)) {
                this.hasConditions = true;
            }
        }
        
        @Override
        public javax.persistence.criteria.Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
            if (!hasConditions) {
                query.distinct(true);
                return builder.conjunction(); // Return true if no conditions
            }
            
            return builder.and(Arrays.stream(specifications)
                    .map(spec -> spec.toPredicate(root, query, builder))
                    .toArray(javax.persistence.criteria.Predicate[]::new));
        }
    }

    // Support for OR specifications
    private static class OrSpecification<T> extends ConditionSpecification<T> {
        private static final long serialVersionUID = -3252693401816828540L;
        private final ConditionSpecification<T>[] specifications;

        public OrSpecification(ConditionSpecification<T>[] specifications) {
            this.specifications = specifications;
            // Set hasConditions based on the specifications
            if (Arrays.stream(specifications).anyMatch(ConditionSpecification::hasConditions)) {
                this.hasConditions = true;
            }
        }
        
        @Override
        public javax.persistence.criteria.Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
            if (!hasConditions) {
                query.distinct(true);
                return builder.conjunction(); // Return true if no conditions
            }
            
            return builder.or(Arrays.stream(specifications)
                    .map(spec -> spec.toPredicate(root, query, builder))
                    .toArray(javax.persistence.criteria.Predicate[]::new));
        }
    }

    // Support for NOT specifications
    private static class NotSpecification<T> extends ConditionSpecification<T> {
        private static final long serialVersionUID = -6929436099257370486L;
        private final ConditionSpecification<T> specification;

        public NotSpecification(ConditionSpecification<T> specification) {
            this.specification = specification;
            this.hasConditions = specification.hasConditions();
        }

        @Override
        public javax.persistence.criteria.Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
            if (!hasConditions) {
                query.distinct(true);
                return builder.conjunction(); // Return true if no conditions
            }
            
            return builder.not(specification.toPredicate(root, query, builder));
        }
    }

    // Always true specification
    private static class AlwaysTrueSpecification<T> extends ConditionSpecification<T> {
        private static final long serialVersionUID = 1L;

        @Override
        public javax.persistence.criteria.Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
            query.distinct(true);
            return builder.conjunction();
        }
    }

    // Always false specification
    private static class AlwaysFalseSpecification<T> extends ConditionSpecification<T> {
        private static final long serialVersionUID = 1L;

        public AlwaysFalseSpecification() {
            this.hasConditions = true;
        }

        @Override
        public javax.persistence.criteria.Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
            query.distinct(true);
            return builder.disjunction();
        }
    }

    /**
     * Creates a new specification that is the negation of the given specification
     * @param spec the specification to negate
     * @return a new specification that represents the negation
     */
    public static <T> ConditionSpecification<T> not(ConditionSpecification<T> spec) {
        return new NotSpecification<>(spec);
    }

    /**
     * Creates a specification that always evaluates to true
     * @return a specification that always returns true
     */
    public static <T> ConditionSpecification<T> alwaysTrue() {
        return new AlwaysTrueSpecification<>();
    }

    /**
     * Creates a specification that always evaluates to false
     * @return a specification that always returns false
     */
    public static <T> ConditionSpecification<T> alwaysFalse() {
        return new AlwaysFalseSpecification<>();
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