package org.sluckframework.common.serializer;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sluckframework.common.exception.Assert;

/**
 * 链式的converter转换source to target
 * 
 * @author sunxy
 * @time 2015年8月30日 上午12:51:48
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ChainedConverter<S, T> implements ContentTypeConverter<S, T> {

    private final List<ContentTypeConverter<S, T>> delegates;
    private final Class<T> target;
    private final Class<S> source;

    /**
     * 根据给定的 converter 集合 返回 链式的 converter
     *
     * @param sourceType The source type of the converter
     * @param targetType The target type of the converter
     * @param candidates The candidates to form a chain with
     * @return A converter for the given source and target types
     *
     * @throws CannotConvertBetweenTypesException if no converter can be created using given candidates
     */
    public static <S, T> ChainedConverter<S, T> calculateChain(Class<S> sourceType, Class<T> targetType,
                                                               Collection<ContentTypeConverter<?, ?>> candidates) {
        Route<S, T> route = calculateRoute(sourceType, targetType, candidates);
        if (route == null) {
            throw new CannotConvertBetweenTypesException(format("Cannot build a converter to convert from %s to %s",
                                                                sourceType.getName(), targetType.getName()));
        }
        return new ChainedConverter<S, T>(route.asList());
    }

    /**
     * 判断是否能用给定 converter 集合 转换 source to target 
     *
     * @param sourceContentType The content type of the source object
     * @param targetContentType The content type of the target object
     * @param converters        The converters eligible for use
     * @return <code>true</code> if this Converter can convert between the given types, using the given converters.
     *         Otherwise <code>false</code>.
     */
    public static <S, T> boolean canConvert(Class<S> sourceContentType, Class<T> targetContentType,
                                            List<ContentTypeConverter<?, ?>> converters) {
        return calculateRoute(sourceContentType, targetContentType, converters) != null;
    }

    private static <S, T> Route<S, T> calculateRoute(Class<S> sourceType, Class<T> targetType,
                                               Collection<ContentTypeConverter<?, ?>> candidates) {
        return new RouteCalculator(candidates).calculateRoute(sourceType, targetType);
    }

    /**
     * 用给定的 contervers 初始化
     * @param delegates the chain of delegates to perform the conversion
     */
    public ChainedConverter(List<ContentTypeConverter<S, T>> delegates) {
        Assert.isTrue(delegates != null && !delegates.isEmpty(), "The given delegates may not be null or empty");
        Assert.isTrue(isContinuous(delegates), "The given delegates must form a continuous chain");
        this.delegates = new ArrayList<ContentTypeConverter<S, T>>(delegates);
        target = this.delegates.get(this.delegates.size() - 1).targetType();
        source = delegates.get(0).expectedSourceType();
    }

    private boolean isContinuous(List<ContentTypeConverter<S, T>> candidates) {
        Class<?> current = null;
        for (ContentTypeConverter<S, T> candidate : candidates) {
            if (current == null || current.equals(candidate.expectedSourceType())) {
                current = candidate.targetType();
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public SerializedObject<T> convert(SerializedObject<S> original) {
        SerializedObject intermediate = original;
        for (ContentTypeConverter step : delegates) {
            intermediate = step.convert(intermediate);
        }
        return intermediate;
    }

    @Override
    public T convert(S original) {
        Object intermediate = original;
        for (ContentTypeConverter step : delegates) {
            intermediate = step.convert(intermediate);
        }
        return (T) intermediate;
    }

    @Override
    public Class<S> expectedSourceType() {
        return source;
    }

    @Override
    public Class<T> targetType() {
        return target;
    }

    /**
     * 路由策略 使用 Dijkstra's算法
     */
    private static final class RouteCalculator {

        private final Collection<ContentTypeConverter<?, ?>> candidates;
        private final List<Route> routes = new LinkedList<Route>();

        private RouteCalculator(Collection<ContentTypeConverter<?, ?>> candidates) {
            this.candidates = new CopyOnWriteArrayList<ContentTypeConverter<?, ?>>(candidates);
        }

        private Route calculateRoute(Class<?> sourceType, Class<?> targetType) {
            Route match = buildInitialRoutes(sourceType, targetType);
            if (match != null) {
                return match;
            }
            while (!candidates.isEmpty() && !routes.isEmpty()) {
                Route route = getShortestRoute();
                for (ContentTypeConverter candidate : candidates) {
                    if (route.endPoint().equals(candidate.expectedSourceType())) {
                        Route newRoute = route.joinedWith(candidate);
                        candidates.remove(candidate);
                        if (targetType.equals(newRoute.endPoint())) {
                            return newRoute;
                        }
                        routes.add(newRoute);
                    }
                }
                routes.remove(route);
            }
            return null;
        }

        private Route buildInitialRoutes(Class<?> sourceType, Class<?> targetType) {
            for (ContentTypeConverter converter : candidates) {
                if (sourceType.equals(converter.expectedSourceType())) {
                    Route route = new Route(converter);
                    if (route.endPoint().equals(targetType)) {
                        return route;
                    }
                    routes.add(route);
                    candidates.remove(converter);
                }
            }
            return null;
        }

        private Route getShortestRoute() {
            // since all nodes have equal distance, the first (i.e. oldest) node is the shortest
            return routes.get(0);
        }
    }

    private static final class Route<S, T> {

        private final ContentTypeConverter<S, T>[] nodes;
        private final Class<?> endPoint;

        private Route(ContentTypeConverter initialVertex) {
            this.nodes = new ContentTypeConverter[]{initialVertex};
            endPoint = initialVertex.targetType();
        }

        private Route(ContentTypeConverter[] baseNodes, ContentTypeConverter newDestination) {
            nodes = Arrays.copyOf(baseNodes, baseNodes.length + 1);
            nodes[baseNodes.length] = newDestination;
            endPoint = newDestination.targetType();
        }

        private Route joinedWith(ContentTypeConverter newVertex) {
            Assert.isTrue(endPoint.equals(newVertex.expectedSourceType()),
                          "Cannot append a vertex if it does not start where the current Route ends");
            return new Route(nodes, newVertex);
        }

        private Class<?> endPoint() {
            return endPoint;
        }

        private List<ContentTypeConverter<S, T>> asList() {
            return Arrays.asList(nodes);
        }
    }
}
