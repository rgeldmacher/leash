package com.rgeldmacher.leash.processor;

import javax.lang.model.element.Element;

/**
 * @author robertgeldmacher
 */
public class AnnotatedField {
    private final String name;
    private final String type;
    private final Element element;


    public AnnotatedField(Element element) {
        this.name = element.getSimpleName().toString();
        this.type = element.asType().toString();
        this.element = element;
    }

    public String getVariableName() {
        return getVariableName(name);
    }

    private static String getVariableName(String name) {
        if (name.matches("^m[A-Z]{1}")) {
            return name.substring(1, 2).toLowerCase();
        } else if (name.matches("m[A-Z]{1}.*")) {
            return name.substring(1, 2).toLowerCase() + name.substring(2);
        }
        return name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }
}
