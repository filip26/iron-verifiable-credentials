package com.apicatalog.di.sd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Pointer {

    public static String[] toSegments(String pointer) {

        Objects.requireNonNull(pointer);

        if (pointer.isEmpty() || pointer.charAt(0) != '/') {
            throw new IllegalArgumentException("Pointer must start with '/', was " + pointer);
        }

        int length = pointer.length();
        var tokens = new ArrayList<String>();
        var builder = new StringBuilder(Math.min(length, 32));

        for (int i = 1; i < length; i++) {

            char c = pointer.charAt(i);

            if (c == '/') {
                tokens.add(builder.toString());
                builder.setLength(0);

            } else if (c == '~') {
                if (++i >= length) {
                    throw new IllegalArgumentException("Invalid escape sequence at end of pointer");
                }

                var next = pointer.charAt(i);

                builder.append(switch (next) {
                case '0' -> '~';
                case '1' -> '/';
                default -> throw new IllegalArgumentException("Invalid escape sequence: ~" + next);
                });

            } else {
                builder.append(c);
            }
        }

        tokens.add(builder.toString());

        return tokens.toArray(String[]::new);
    }

    public static Map<String, Object> select(Map<String, Object> source, Collection<String> pointers) {

        var target = cloneWithContext(source);

        boolean hasArrays = false;

        for (var pointer : pointers) {
            
            var segments = toSegments(pointer);

            Object sourceParent = source;
            Object targetParent = target;

            for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {

                var segment = segments[segmentIndex];

                if (sourceParent instanceof Map parentMap && targetParent instanceof Map targetMap) {

                    var sourceValue = parentMap.get(segment);

                    if (sourceValue == null) {
                        throw new IllegalArgumentException();
                    }

                    if ((segmentIndex + 1) == segments.length) {
                        targetMap.put(segment, sourceValue); // TODO clone

                    } else {
                        var targetValue = targetMap.get(segment);

                        if (targetValue == null) {
                            if (sourceValue instanceof Map sourceMap) {
                                targetValue = cloneWithContext(sourceMap);

                            } else if (sourceValue instanceof Collection sourceCol) {
                                targetValue = new Object[sourceCol.size()];
                                hasArrays = true;

                            } else {
                                throw new IllegalArgumentException();
                            }
                            targetMap.put(segment, targetValue);

                        } else if (targetValue instanceof Collection targetCol) {
                            targetValue = new Object[targetCol.size()];
                            targetMap.put(segment, targetValue);
                            hasArrays = true;
                        }
                        targetParent = targetValue;
                    }
                    sourceParent = sourceValue;

                } else if (sourceParent instanceof Collection parentCollection
                        && targetParent instanceof Object[] targetArray) {

                    if (segment.startsWith("0") && segment.length() > 1) {
                        throw new IllegalArgumentException();
                    }

                    var index = Integer.parseInt(segment);

                    if (index >= parentCollection.size()) {
                        throw new IllegalArgumentException();
                    }

                    var sourceValue = extractElement(parentCollection, index);

                    if ((segmentIndex + 1) == segments.length) {
                        targetArray[index] = sourceValue; // TODO clone

                    } else {
                        var targetValue = targetArray[index];

                        if (targetValue == null) {
                            if (sourceValue instanceof Map sourceMap) {
                                targetValue = cloneWithContext(sourceMap);

                            } else if (sourceValue instanceof Collection sourceCol) {
                                targetValue = new Object[sourceCol.size()];

                            } else {
                                throw new IllegalArgumentException();
                            }
                            targetArray[index] = targetValue;

                        } else if (targetValue instanceof Collection targetCol) {
                            targetValue = new Object[targetCol.size()];
                            targetArray[index] = targetValue;
                            hasArrays = true;
                        }
                        targetParent = targetValue;
                    }
                    sourceParent = sourceValue;

                } else {
                    throw new IllegalStateException();
                }
            }
        }

        if (hasArrays) {
            dense(target);
        }

        return target;
    }

    private static final Object extractElement(Collection<?> collection, int index) {
        if (collection instanceof List<?> list) {
            return list.get(index);
        }
        int currentIndex = 0;
        for (Object item : collection) {
            if (currentIndex++ == index) {
                return item;
            }
        }
        throw new IllegalArgumentException();
    }

    private static final Map<String, Object> cloneWithContext(Map<?, ?> source) {
        var clone = HashMap.<String, Object>newHashMap(3);

        var id = source.get("id");
        if (id instanceof String str && !str.startsWith("_:")) {
            clone.put("id", str);
        }

        var type = source.get("type");
        if (type != null) {
            clone.put("type", type);
        }

        var context = source.get("@context");
        if (context != null) {
            clone.put("@context", context);
        }

        return clone;
    }

    private static final Object deepClone(Object source) {
        return switch (source) {
        case Map<?, ?> map -> {
            var clone = HashMap.<String, Object>newHashMap(map.size());
            for (var entry : map.entrySet()) {
                clone.put(String.valueOf(entry.getKey()), deepClone(entry.getValue()));
            }
            yield clone;
        }
        case Collection<?> col -> {
            var clone = new ArrayList<>(col.size());
            for (var item : col) {
                clone.add(deepClone(item));
            }
            yield clone;
        }
        case Object[] arr -> {
            var clone = new Object[arr.length];
            for (var i = 0; i < arr.length; i++) {
                clone[i] = deepClone(arr[i]);
            }
            yield clone;
        }
        default -> source;
        };
    }

    private static final void dense(Map<?, Object> data) {
        for (var entry : data.entrySet()) {

            var value = entry.getValue();

            if (value instanceof Object[] array) {
                entry.setValue(toDenseList(array));

            } else if (value instanceof Map map) {
                dense(map);
            }
        }
    }

    private static final Collection<Object> toDenseList(Object[] array) {
        var list = new ArrayList<>(array.length);

        for (var element : array) {

            if (element instanceof Map map) {
                dense(map);

            } else if (element instanceof Object[] embeddedArray) {
                list.add(toDenseList(embeddedArray));

            } else if (element != null) {
                list.add(element);
            }
        }

        if (list.size() < array.length) {
            list.trimToSize();
        }

        return list;
    }
}
